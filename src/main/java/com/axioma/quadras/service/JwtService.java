package com.axioma.quadras.service;

import com.axioma.quadras.config.JwtProperties;
import com.axioma.quadras.domain.dto.AuthTokenDto;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class JwtService {

	private final JwtEncoder jwtEncoder;
	private final JwtDecoder jwtDecoder;
	private final JwtProperties jwtProperties;

	public JwtService(JwtEncoder jwtEncoder, JwtDecoder jwtDecoder, JwtProperties jwtProperties) {
		this.jwtEncoder = jwtEncoder;
		this.jwtDecoder = jwtDecoder;
		this.jwtProperties = jwtProperties;
	}

	public AuthTokenDto issueToken(AuthenticatedUserPrincipal principal) {
		final Instant issuedAt = Instant.now();
		final Instant expiresAt = issuedAt.plusSeconds(jwtProperties.expirationSeconds());
		final JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer(jwtProperties.issuer())
				.issuedAt(issuedAt)
				.expiresAt(expiresAt)
				.subject(principal.getUsername())
				.claim("role", principal.getRole().name())
				.build();
		final JwsHeader header = JwsHeader.with(MacAlgorithm.HS256)
				.type("JWT")
				.build();
		final String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
		return new AuthTokenDto(
				token,
				"Bearer",
				jwtProperties.expirationSeconds(),
				principal.getUsername(),
				principal.getRole().name()
		);
	}

	public Jwt decode(String token) {
		return jwtDecoder.decode(token);
	}
}
