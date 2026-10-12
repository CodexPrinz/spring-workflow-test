package com.projects.spring_workflow_test.student.dto;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

@Schema(name = "StudentRequest", description = "Fields required to create or update a student.")
public record StudentRequest(
		@Schema(description = "Student's full name", example = "Ada Lovelace", maxLength = 255,
				requiredMode = Schema.RequiredMode.REQUIRED)
		@NotBlank(message = "Name is required") @Size(max = 255, message = "Name must be at most 255 characters") String name,
		@Schema(description = "Unique email address", example = "ada@example.com", maxLength = 255,
				requiredMode = Schema.RequiredMode.REQUIRED)
		@NotBlank(message = "Email is required") @Email(message = "Email must be valid")
		@Size(max = 255, message = "Email must be at most 255 characters") String email,
		@Schema(description = "Date of birth in ISO-8601 format; must be in the past", example = "1815-12-10",
				type = "string", format = "date", requiredMode = Schema.RequiredMode.REQUIRED)
		@NotNull(message = "Date of birth is required") @Past(message = "Date of birth must be in the past")
		LocalDate dateOfBirth,
		@Schema(description = "Student's nationality", example = "United Kingdom", maxLength = 100,
				requiredMode = Schema.RequiredMode.REQUIRED)
		@NotBlank(message = "Nationality is required")
		@Size(max = 100, message = "Nationality must be at most 100 characters") String nationality) {
}
