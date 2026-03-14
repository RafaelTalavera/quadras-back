package com.axioma.quadras.config;

import com.axioma.quadras.domain.model.AppUser;
import com.axioma.quadras.domain.model.AppUserRole;
import com.axioma.quadras.service.ApplicationUserDetailsService;
import com.axioma.quadras.service.AuthenticatedUserPrincipal;
import com.axioma.quadras.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtService jwtService;
	private final ApplicationUserDetailsService userDetailsService;
	private final AuthenticationEntryPoint authenticationEntryPoint;

	public JwtAuthenticationFilter(
			JwtService jwtService,
			ApplicationUserDetailsService userDetailsService,
			AuthenticationEntryPoint authenticationEntryPoint
	) {
		this.jwtService = jwtService;
		this.userDetailsService = userDetailsService;
		this.authenticationEntryPoint = authenticationEntryPoint;
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		final String path = request.getRequestURI();
		return "/api/v1/auth/login".equals(path) || "/api/v1/system/health".equals(path);
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain
	) throws ServletException, IOException {
		final String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (authorization == null || !authorization.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}

		final String token = authorization.substring(7).trim();
		try {
			final Jwt jwt = jwtService.decode(token);
			final String username = AppUser.normalizeUsername(jwt.getSubject());
			final AppUserRole role = AppUserRole.fromClaim(jwt.getClaimAsString("role"));
			final AuthenticatedUserPrincipal principal =
					(AuthenticatedUserPrincipal) userDetailsService.loadUserByUsername(username);

			if (principal.getRole() != role) {
				throw new BadCredentialsException("JWT role does not match persisted user role.");
			}

			if (SecurityContextHolder.getContext().getAuthentication() == null) {
				final UsernamePasswordAuthenticationToken authentication =
						new UsernamePasswordAuthenticationToken(principal, token, principal.getAuthorities());
				authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(authentication);
			}

			filterChain.doFilter(request, response);
		} catch (JwtException | AuthenticationException | IllegalArgumentException ex) {
			SecurityContextHolder.clearContext();
			authenticationEntryPoint.commence(
					request,
					response,
					new BadCredentialsException("Invalid JWT token.", ex)
			);
		}
	}
}
