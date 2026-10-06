package com.projects.spring_workflow_test.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import com.projects.spring_workflow_test.student.StudentEmailAlreadyExistsException;
import com.projects.spring_workflow_test.student.StudentNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.MapBindingResult;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;

class GlobalExceptionHandlerTest {

	private GlobalExceptionHandler handler;
	private HttpServletRequest request;

	@BeforeEach
	void setUp() {
		handler = new GlobalExceptionHandler();
		request = mock(HttpServletRequest.class);
		when(request.getRequestURI()).thenReturn("/api/students/3");
	}

	@Test
	void handlesStudentNotFound() {
		var response = handler.handleStudentNotFound(new StudentNotFoundException(3L), request);

		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		assertErrorBody(response.getBody(), HttpStatus.NOT_FOUND, "Student with id 3 was not found");
	}

	@Test
	void handlesDuplicateEmailAndDatabaseConstraintConflicts() {
		var duplicateEmail = handler.handleConflict(
				new StudentEmailAlreadyExistsException("ada@example.com"), request);
		var constraintViolation = handler.handleConflict(
				new DataIntegrityViolationException("duplicate key"), request);

		assertErrorBody(duplicateEmail.getBody(), HttpStatus.CONFLICT,
				"A student with email ada@example.com already exists");
		assertErrorBody(constraintViolation.getBody(), HttpStatus.CONFLICT,
				"A student with this email already exists");
	}

	@Test
	void returnsValidationErrorsForInvalidFields() {
		MapBindingResult bindingResult = new MapBindingResult(new HashMap<>(), "studentRequest");
		bindingResult.rejectValue("name", "NotBlank", "Name is required");
		bindingResult.rejectValue("email", "Email", "Email must be valid");
		MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

		var response = handler.handleValidation(exception, request);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertEquals("Request validation failed", response.getBody().message());
		assertEquals(Map.of("name", "Name is required", "email", "Email must be valid"),
				response.getBody().validationErrors());
	}

	@Test
	void handlesMalformedRequestBodies() {
		var response = handler.handleUnreadableRequest(request);

		assertErrorBody(response.getBody(), HttpStatus.BAD_REQUEST, "Request body is missing or malformed");
	}

	@Test
	void handlesUnknownRoutes() {
		var response = handler.handleRouteNotFound(request);

		assertErrorBody(response.getBody(), HttpStatus.NOT_FOUND, "Endpoint not found");
	}

	@Test
	void handlesUnsupportedMethods() {
		var response = handler.handleMethodNotAllowed(new HttpRequestMethodNotSupportedException("PATCH"), request);

		assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
		assertEquals("/api/students/3", response.getBody().path());
	}

	@Test
	void handlesUnexpectedErrorsWithoutExposingTheirDetails() {
		var response = handler.handleUnexpected(new IllegalStateException("internal detail"), request);

		assertErrorBody(response.getBody(), HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
		assertFalse(response.getBody().message().contains("internal detail"));
	}

	private void assertErrorBody(ApiError body, HttpStatus status, String message) {
		assertNotNull(body.timestamp());
		assertEquals(status.value(), body.status());
		assertEquals(status.getReasonPhrase(), body.error());
		assertEquals(message, body.message());
		assertEquals("/api/students/3", body.path());
	}
}
