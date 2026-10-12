package com.projects.spring_workflow_test.student.dto;

import java.time.Instant;

public record StudentResponse(Long id, String name, String email, Instant createdAt, Instant updatedAt) {
}
