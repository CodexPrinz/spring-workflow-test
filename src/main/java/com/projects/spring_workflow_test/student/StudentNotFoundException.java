package com.projects.spring_workflow_test.student;

public class StudentNotFoundException extends RuntimeException {

	public StudentNotFoundException(Long id) {
		super("Student with id " + id + " was not found");
	}
}
