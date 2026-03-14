package com.axioma.quadras.config;

import com.axioma.quadras.domain.model.AppUserRole;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableConfigurationProperties({JwtProperties.class, DemoUserProperties.class})
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(
			HttpSecurity http,
			JwtAuthenticationFilter jwtAuthenticationFilter,
			RestAuthenticationEntryPoint authenticationEntryPoint,
			RestAccessDeniedHandler accessDeniedHandler
	) throws Exception {
		return http
				.csrf(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable)
				.formLogin(AbstractHttpConfigurer::disable)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.exceptionHandling(exceptions -> exceptions
						.authenticationEntryPoint(authenticationEntryPoint)
						.accessDeniedHandler(accessDeniedHandler)
				)
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers("/api/v1/system/health", "/api/v1/auth/**").permitAll()
						.requestMatchers("/api/v1/reservations/**").hasRole(AppUserRole.OPERATOR.name())
						.requestMatchers("/api/v1/users/**").authenticated()
						.anyRequest().authenticated()
				)
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
				.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(
			org.springframework.security.core.userdetails.UserDetailsService userDetailsService,
			PasswordEncoder passwordEncoder
	) {
		final DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
		provider.setPasswordEncoder(passwordEncoder);
		return new ProviderManager(provider);
	}

	@Bean
	public JwtEncoder jwtEncoder(JwtProperties jwtProperties) {
		return new NimbusJwtEncoder(new ImmutableSecret<>(signingKey(jwtProperties)));
	}

	@Bean
	public JwtDecoder jwtDecoder(JwtProperties jwtProperties) {
		return NimbusJwtDecoder.withSecretKey(signingKey(jwtProperties))
				.macAlgorithm(MacAlgorithm.HS256)
				.build();
	}

	private SecretKey signingKey(JwtProperties jwtProperties) {
		return new SecretKeySpec(
				jwtProperties.secret().getBytes(StandardCharsets.UTF_8),
				"HmacSHA256"
		);
	}
}
