package com.projects.spring_workflow_test.student;

public class StudentEmailAlreadyExistsException extends RuntimeException {

	public StudentEmailAlreadyExistsException(String email) {
		super("A student with email " + email + " already exists");
	}
}
