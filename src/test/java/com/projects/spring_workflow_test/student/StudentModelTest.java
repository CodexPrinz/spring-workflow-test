package com.projects.spring_workflow_test.student;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.LocalDate;

import com.projects.spring_workflow_test.student.dto.StudentRequest;
import com.projects.spring_workflow_test.student.dto.StudentResponse;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;

class StudentModelTest {

	@Test
	void updatesStudentFields() {
		LocalDate oldDateOfBirth = LocalDate.of(1990, 1, 1);
		Student student = new Student("Old Name", "old@example.com", oldDateOfBirth, "Canadian");
		Instant createdAt = student.getCreatedAt();
		Instant previousUpdatedAt = student.getUpdatedAt();
		LocalDate newDateOfBirth = LocalDate.of(1995, 2, 2);

		student.update("New Name", "new@example.com", newDateOfBirth, "British");

		assertEquals("New Name", student.getName());
		assertEquals("new@example.com", student.getEmail());
		assertEquals(newDateOfBirth, student.getDateOfBirth());
		assertEquals("British", student.getNationality());
		assertEquals(createdAt, student.getCreatedAt());
		assertTrue(!student.getUpdatedAt().isBefore(previousUpdatedAt));
	}

	@Test
	void requestAndResponseDtosExposeTheirOwnData() {
		LocalDate dateOfBirth = LocalDate.of(1815, 12, 10);
		StudentRequest request = new StudentRequest("Ada", "ada@example.com", dateOfBirth, "British");
		Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");
		Instant updatedAt = Instant.parse("2026-01-02T00:00:00Z");
		StudentResponse response = new StudentResponse(
				3L, request.name(), request.email(), request.dateOfBirth(), request.nationality(), createdAt, updatedAt);

		assertEquals("Ada", request.name());
		assertEquals("ada@example.com", request.email());
		assertEquals(dateOfBirth, request.dateOfBirth());
		assertEquals("British", request.nationality());
		assertEquals(3L, response.id());
		assertEquals(request.name(), response.name());
		assertEquals(request.email(), response.email());
		assertEquals(dateOfBirth, response.dateOfBirth());
		assertEquals("British", response.nationality());
		assertEquals(createdAt, response.createdAt());
		assertEquals(updatedAt, response.updatedAt());
	}

	@Test
	void initializesAuditTimestampsWhenStudentIsCreated() {
		Student student = new Student("Ada", "ada@example.com", LocalDate.of(1815, 12, 10), "British");

		assertNotNull(student.getCreatedAt());
		assertEquals(student.getCreatedAt(), student.getUpdatedAt());
	}

	@Test
	void validatesRequiredNationalityAndPastDateOfBirth() {
		try (var validatorFactory = Validation.buildDefaultValidatorFactory()) {
			var validator = validatorFactory.getValidator();
			StudentRequest request = new StudentRequest(
					"Ada", "ada@example.com", LocalDate.now().plusDays(1), " ");

			var violations = validator.validate(request);

			assertEquals(2, violations.size());
			assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("dateOfBirth")));
			assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("nationality")));
		}
	}

	@Test
	void studentExceptionsDescribeTheirErrors() {
		assertEquals("Student with id 3 was not found", new StudentNotFoundException(3L).getMessage());
		assertEquals("A student with email ada@example.com already exists",
				new StudentEmailAlreadyExistsException("ada@example.com").getMessage());
	}
}
