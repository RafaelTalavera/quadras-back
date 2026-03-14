package com.axioma.quadras.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.OffsetDateTime;

@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

	@Override
	public void handle(
			HttpServletRequest request,
			HttpServletResponse response,
			AccessDeniedException accessDeniedException
	) throws IOException, ServletException {
		final HttpStatus status = HttpStatus.FORBIDDEN;
		response.setStatus(status.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.getWriter().write(buildJsonBody(status, request.getRequestURI()));
	}

	private String buildJsonBody(HttpStatus status, String path) {
		return """
				{"timestamp":"%s","status":%d,"error":"%s","message":"%s","path":"%s"}
				""".formatted(
				OffsetDateTime.now(),
				status.value(),
				escapeJson(status.getReasonPhrase()),
				escapeJson("The current role is not allowed to access this resource."),
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
