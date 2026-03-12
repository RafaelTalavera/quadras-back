package com.axioma.quadras.config;

import com.axioma.quadras.domain.exception.ApplicationException;
import com.axioma.quadras.domain.model.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;

@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(ApplicationException.class)
	public ResponseEntity<ApiError> handleApplicationException(ApplicationException ex, HttpServletRequest request) {
		HttpStatus status = ex.getStatus();
		ApiError body = new ApiError(
				OffsetDateTime.now(),
				status.value(),
				status.getReasonPhrase(),
				ex.getMessage(),
				request.getRequestURI()
		);
		return ResponseEntity.status(status).body(body);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiError> handleUnexpectedException(Exception ex, HttpServletRequest request) {
		HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
		ApiError body = new ApiError(
				OffsetDateTime.now(),
				status.value(),
				status.getReasonPhrase(),
				"Unexpected error",
				request.getRequestURI()
		);
		return ResponseEntity.status(status).body(body);
	}
}
