package com.projects.spring_workflow_test.student.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record StudentRequest(
		@NotBlank(message = "Name is required") String name,
		@NotBlank(message = "Email is required") @Email(message = "Email must be valid") String email) {
}
