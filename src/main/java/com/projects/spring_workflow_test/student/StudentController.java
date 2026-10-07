package com.projects.spring_workflow_test.student;

import java.util.List;

import com.projects.spring_workflow_test.student.dto.StudentRequest;
import com.projects.spring_workflow_test.student.dto.StudentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/students")
@Tag(name = "Students", description = "Operations for managing student records")
@Validated
public class StudentController {

	private final StudentService studentService;

	public StudentController(StudentService studentService) {
		this.studentService = studentService;
	}

	@GetMapping
	@Operation(summary = "List students", description = "Returns all students, including their creation and last update timestamps.")
	public List<StudentResponse> getAllStudents() {
		return studentService.getAllStudents();
	}

	@GetMapping("/{id}")
	@Operation(summary = "Find a student by ID", description = "Returns the student with the specified ID.")
	public StudentResponse getStudentById(
			@Parameter(description = "ID of the student to find") @PathVariable Long id) {
		return studentService.getStudentById(id);
	}

	@GetMapping("/name/{name}")
	@Operation(summary = "Find a student by name", description = "Finds a student by their name.")
	public StudentResponse getStudentByName(
			@Parameter(description = "Student name") @PathVariable String name) {
		return studentService.getStudentByName(name);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Add a student", description = "Creates a student using the supplied name and unique email address.")
	public StudentResponse addStudent(@Valid @RequestBody StudentRequest request) {
		return studentService.addStudent(request);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Edit a student", description = "Updates a student's name and email address. The email must be unique.")
	public StudentResponse editStudent(
			@Parameter(description = "ID of the student to update") @PathVariable Long id,
			@Valid @RequestBody StudentRequest request) {
		return studentService.editStudent(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Delete a student", description = "Permanently deletes the student with the specified ID.")
	public void deleteStudent(@Parameter(description = "ID of the student to delete") @PathVariable Long id) {
		studentService.deleteStudent(id);
	}
}
