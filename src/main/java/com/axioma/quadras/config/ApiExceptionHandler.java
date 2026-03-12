package com.axioma.quadras.config;

import com.axioma.quadras.domain.exception.ApplicationException;
import com.axioma.quadras.domain.model.ApiError;
import jakarta.validation.ConstraintViolationException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.OffsetDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiError> handleMethodArgumentNotValidException(
			MethodArgumentNotValidException ex,
			HttpServletRequest request
	) {
		HttpStatus status = HttpStatus.BAD_REQUEST;
		String message = ex.getBindingResult()
				.getFieldErrors()
				.stream()
				.map(error -> error.getField() + ": " + error.getDefaultMessage())
				.collect(Collectors.joining("; "));
		ApiError body = new ApiError(
				OffsetDateTime.now(),
				status.value(),
				status.getReasonPhrase(),
				message,
				request.getRequestURI()
		);
		return ResponseEntity.status(status).body(body);
	}

	@ExceptionHandler({ConstraintViolationException.class, MethodArgumentTypeMismatchException.class, IllegalArgumentException.class})
	public ResponseEntity<ApiError> handleBadRequestExceptions(Exception ex, HttpServletRequest request) {
		HttpStatus status = HttpStatus.BAD_REQUEST;
		ApiError body = new ApiError(
				OffsetDateTime.now(),
				status.value(),
				status.getReasonPhrase(),
				ex.getMessage(),
				request.getRequestURI()
		);
		return ResponseEntity.status(status).body(body);
	}

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
