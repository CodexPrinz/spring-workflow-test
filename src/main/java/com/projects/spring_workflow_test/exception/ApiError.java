package com.projects.spring_workflow_test.exception;

import java.time.Instant;
import java.util.Map;

public record ApiError(
		Instant timestamp,
		int status,
		String error,
		String message,
		String path,
		Map<String, String> validationErrors) {
}
