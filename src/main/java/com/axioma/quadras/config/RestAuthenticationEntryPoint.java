package com.axioma.quadras.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.OffsetDateTime;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

	@Override
	public void commence(
			HttpServletRequest request,
			HttpServletResponse response,
			AuthenticationException authException
	) throws IOException, ServletException {
		final HttpStatus status = HttpStatus.UNAUTHORIZED;
		final String message = resolveMessage(authException);
		response.setStatus(status.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.getWriter().write(buildJsonBody(
				status,
				message,
				request.getRequestURI()
		));
	}

	private String resolveMessage(AuthenticationException authException) {
		if (authException != null && "Invalid JWT token.".equals(authException.getMessage())) {
			return authException.getMessage();
		}
		return "Authentication is required to access this resource.";
	}

	private String buildJsonBody(HttpStatus status, String message, String path) {
		return """
				{"timestamp":"%s","status":%d,"error":"%s","message":"%s","path":"%s"}
				""".formatted(
				OffsetDateTime.now(),
				status.value(),
				escapeJson(status.getReasonPhrase()),
				escapeJson(message),
				escapeJson(path)
		);
	}

	private String escapeJson(String value) {
		if (value == null) {
			return "";
		}
		return value
				.replace("\\", "\\\\")
				.replace("\"", "\\\"");
	}
}
