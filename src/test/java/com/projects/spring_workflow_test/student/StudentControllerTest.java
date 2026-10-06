package com.projects.spring_workflow_test.student;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;

import com.projects.spring_workflow_test.student.dto.StudentRequest;
import com.projects.spring_workflow_test.student.dto.StudentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StudentControllerTest {

	@Mock
	private StudentService studentService;

	private StudentController studentController;

	@BeforeEach
	void setUp() {
		studentController = new StudentController(studentService);
	}

	@Test
	void delegatesListingToService() {
		List<StudentResponse> expected = List.of(new StudentResponse(
				1L, "Ada", "ada@example.com", Instant.EPOCH, Instant.EPOCH));
		when(studentService.getAllStudents()).thenReturn(expected);

		assertEquals(expected, studentController.getAllStudents());

		verify(studentService).getAllStudents();
	}

	@Test
	void delegatesAddingToServiceAndReturnsResponse() {
		StudentRequest request = new StudentRequest("Ada", "ada@example.com");
		StudentResponse expected = new StudentResponse(
				1L, "Ada", "ada@example.com", Instant.EPOCH, Instant.EPOCH);
		when(studentService.addStudent(request)).thenReturn(expected);

		assertEquals(expected, studentController.addStudent(request));

		verify(studentService).addStudent(request);
	}

	@Test
	void delegatesEditingToServiceAndReturnsResponse() {
		StudentRequest request = new StudentRequest("Ada", "ada@example.com");
		StudentResponse expected = new StudentResponse(
				1L, "Ada", "ada@example.com", Instant.EPOCH, Instant.EPOCH);
		when(studentService.editStudent(1L, request)).thenReturn(expected);

		assertEquals(expected, studentController.editStudent(1L, request));

		verify(studentService).editStudent(1L, request);
	}

	@Test
	void delegatesDeletionToService() {
		studentController.deleteStudent(1L);

		verify(studentService).deleteStudent(1L);
	}
}
