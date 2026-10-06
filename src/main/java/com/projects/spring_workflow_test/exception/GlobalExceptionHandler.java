package com.projects.spring_workflow_test.exception;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

import com.projects.spring_workflow_test.student.StudentEmailAlreadyExistsException;
import com.projects.spring_workflow_test.student.StudentNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(StudentNotFoundException.class)
	public ResponseEntity<ApiError> handleStudentNotFound(
			StudentNotFoundException exception, HttpServletRequest request) {
		return error(HttpStatus.NOT_FOUND, exception.getMessage(), request, Map.of());
	}

	@ExceptionHandler({StudentEmailAlreadyExistsException.class, DataIntegrityViolationException.class})
	public ResponseEntity<ApiError> handleConflict(Exception exception, HttpServletRequest request) {
		String message = exception instanceof StudentEmailAlreadyExistsException
				? exception.getMessage()
				: "A student with this email already exists";
		return error(HttpStatus.CONFLICT, message, request, Map.of());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiError> handleValidation(
			MethodArgumentNotValidException exception, HttpServletRequest request) {
		Map<String, String> validationErrors = exception.getBindingResult().getFieldErrors().stream()
				.collect(Collectors.toMap(
						FieldError::getField,
						error -> error.getDefaultMessage() == null ? "Invalid value" : error.getDefaultMessage(),
						(first, second) -> first));
		return error(HttpStatus.BAD_REQUEST, "Request validation failed", request, validationErrors);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiError> handleUnreadableRequest(HttpServletRequest request) {
		return error(HttpStatus.BAD_REQUEST, "Request body is missing or malformed", request, Map.of());
	}

	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<ApiError> handleRouteNotFound(HttpServletRequest request) {
		return error(HttpStatus.NOT_FOUND, "Endpoint not found", request, Map.of());
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ApiError> handleMethodNotAllowed(
			HttpRequestMethodNotSupportedException exception, HttpServletRequest request) {
		return error(HttpStatus.METHOD_NOT_ALLOWED, exception.getMessage(), request, Map.of());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiError> handleUnexpected(Exception exception, HttpServletRequest request) {
		log.error("Unexpected error handling request {}", request.getRequestURI(), exception);
		return error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request, Map.of());
	}

	private ResponseEntity<ApiError> error(
			HttpStatus status, String message, HttpServletRequest request, Map<String, String> validationErrors) {
		ApiError body = new ApiError(
				Instant.now(), status.value(), status.getReasonPhrase(), message, request.getRequestURI(),
				validationErrors);
		return ResponseEntity.status(status).body(body);
	}
}
