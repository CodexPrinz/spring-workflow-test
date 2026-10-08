package com.projects.spring_workflow_test.student;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;

import com.projects.spring_workflow_test.student.dto.StudentRequest;
import com.projects.spring_workflow_test.student.dto.StudentResponse;
import org.junit.jupiter.api.Test;

class StudentModelTest {

	@Test
	void updatesStudentFields() {
		Student student = new Student("Old Name", "old@example.com");
		Instant createdAt = student.getCreatedAt();
		Instant previousUpdatedAt = student.getUpdatedAt();

		student.update("New Name", "new@example.com");

		assertEquals("New Name", student.getName());
		assertEquals("new@example.com", student.getEmail());
		assertEquals(createdAt, student.getCreatedAt());
		assertTrue(!student.getUpdatedAt().isBefore(previousUpdatedAt));
	}

	@Test
	void requestAndResponseDtosExposeTheirOwnData() {
		StudentRequest request = new StudentRequest("Ada", "ada@example.com");
		Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");
		Instant updatedAt = Instant.parse("2026-01-02T00:00:00Z");
		StudentResponse response = new StudentResponse(3L, request.name(), request.email(), createdAt, updatedAt);

		assertEquals("Ada", request.name());
		assertEquals("ada@example.com", request.email());
		assertEquals(3L, response.id());
		assertEquals(request.name(), response.name());
		assertEquals(request.email(), response.email());
		assertEquals(createdAt, response.createdAt());
		assertEquals(updatedAt, response.updatedAt());
	}

	@Test
	void initializesAuditTimestampsWhenStudentIsCreated() {
		Student student = new Student("Ada", "ada@example.com");

		assertNotNull(student.getCreatedAt());
		assertEquals(student.getCreatedAt(), student.getUpdatedAt());
	}

	@Test
	void studentExceptionsDescribeTheirErrors() {
		assertEquals("Student with id 3 was not found", new StudentNotFoundException(3L).getMessage());
		assertEquals("A student with email ada@example.com already exists",
				new StudentEmailAlreadyExistsException("ada@example.com").getMessage());
	}
}
