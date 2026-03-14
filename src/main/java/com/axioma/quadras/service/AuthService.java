package com.axioma.quadras.service;

import com.axioma.quadras.domain.dto.AuthTokenDto;
import com.axioma.quadras.domain.dto.CurrentUserDto;
import com.axioma.quadras.domain.dto.LoginRequestDto;
import com.axioma.quadras.domain.exception.ApplicationException;
import com.axioma.quadras.domain.model.AppUser;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;

	public AuthService(AuthenticationManager authenticationManager, JwtService jwtService) {
		this.authenticationManager = authenticationManager;
		this.jwtService = jwtService;
	}

	public AuthTokenDto login(LoginRequestDto input) {
		try {
			final Authentication authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(
							AppUser.normalizeUsername(input.username()),
							input.password()
					)
			);
			final AuthenticatedUserPrincipal principal = requirePrincipal(authentication.getPrincipal());
			return jwtService.issueToken(principal);
		} catch (AuthenticationException ex) {
			throw new ApplicationException(HttpStatus.UNAUTHORIZED, "Invalid username or password.");
		}
	}

	public CurrentUserDto currentUser(AuthenticatedUserPrincipal principal) {
		return new CurrentUserDto(principal.getUsername(), principal.getRole().name());
	}

	private AuthenticatedUserPrincipal requirePrincipal(Object principal) {
		if (principal instanceof AuthenticatedUserPrincipal authenticatedUserPrincipal) {
			return authenticatedUserPrincipal;
		}
		throw new BadCredentialsException("Authenticated principal is invalid.");
	}
}
