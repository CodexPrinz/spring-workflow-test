package com.projects.spring_workflow_test.student.dto;

import java.time.Instant;
import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "StudentResponse", description = "Student details returned by the API.")
public record StudentResponse(
		@Schema(description = "Generated student identifier", example = "1") Long id,
		@Schema(description = "Student's full name", example = "Ada Lovelace") String name,
		@Schema(description = "Unique email address", example = "ada@example.com") String email,
		@Schema(description = "Date of birth in ISO-8601 format; null for records created before this field was added",
				example = "1815-12-10", type = "string", format = "date", nullable = true)
		LocalDate dateOfBirth,
		@Schema(description = "Student's nationality; null for records created before this field was added",
				example = "United Kingdom", nullable = true)
		String nationality,
		@Schema(description = "Creation timestamp", example = "2026-01-01T00:00:00Z") Instant createdAt,
		@Schema(description = "Last update timestamp", example = "2026-01-02T00:00:00Z") Instant updatedAt) {
}
