package com.projects.spring_workflow_test.student;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.projects.spring_workflow_test.student.dto.StudentRequest;
import com.projects.spring_workflow_test.student.dto.StudentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

	@Mock
	private StudentRepository studentRepository;

	private StudentService studentService;

	@BeforeEach
	void setUp() {
		studentService = new StudentService(studentRepository);
	}

	@Test
	void returnsAllStudentsAsResponseDtos() {
		when(studentRepository.findAll()).thenReturn(List.of(new Student("Ada", "ada@example.com")));

		List<StudentResponse> result = studentService.getAllStudents();

		assertEquals(1, result.size());
		assertEquals("Ada", result.getFirst().name());
		assertEquals("ada@example.com", result.getFirst().email());
		assertNotNull(result.getFirst().createdAt());
		assertNotNull(result.getFirst().updatedAt());
	}

	@Test
	void findsStudentById() {
		when(studentRepository.findById(7L)).thenReturn(Optional.of(new Student("Ada", "ada@example.com")));

		StudentResponse result = studentService.getStudentById(7L);

		assertEquals("Ada", result.name());
		assertEquals("ada@example.com", result.email());
	}

	@Test
	void findsStudentByUsername() {
		when(studentRepository.findByName("Ada")).thenReturn(Optional.of(new Student("Ada", "ada@example.com")));

		StudentResponse result = studentService.getStudentByName("Ada");

		assertEquals("Ada", result.name());
		assertEquals("ada@example.com", result.email());
		verify(studentRepository).findByName("Ada");
	}

	@Test
	void throwsWhenStudentUsernameDoesNotExist() {
		when(studentRepository.findByName("Ada")).thenReturn(Optional.empty());

		assertThrows(StudentNotFoundException.class, () -> studentService.getStudentByName("Ada"));
	}

	@Test
	void stripsHtmlMarkupFromStudentNames() {
		StudentRequest request = new StudentRequest("<script>alert(1)</script>Ada", "ada@example.com");
		when(studentRepository.existsByEmail(request.email())).thenReturn(false);
		when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> invocation.getArgument(0));

		StudentResponse result = studentService.addStudent(request);

		assertEquals("alert(1)Ada", result.name());
	}

	@Test
	void addsStudentWhenEmailIsAvailable() {
		StudentRequest request = new StudentRequest("Ada", "ada@example.com");
		when(studentRepository.existsByEmail(request.email())).thenReturn(false);
		when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> invocation.getArgument(0));

		StudentResponse result = studentService.addStudent(request);

		assertEquals("Ada", result.name());
		assertEquals("ada@example.com", result.email());
		assertNotNull(result.createdAt());
		assertNotNull(result.updatedAt());
		verify(studentRepository).save(any(Student.class));
	}

	@Test
	void rejectsAddingStudentWithDuplicateEmail() {
		StudentRequest request = new StudentRequest("Ada", "ada@example.com");
		when(studentRepository.existsByEmail(request.email())).thenReturn(true);

		assertThrows(StudentEmailAlreadyExistsException.class, () -> studentService.addStudent(request));

		verify(studentRepository, never()).save(any(Student.class));
	}

	@Test
	void editsExistingStudent() {
		Student student = new Student("Old Name", "old@example.com");
		Instant createdAt = student.getCreatedAt();
		Instant previousUpdatedAt = student.getUpdatedAt();
		when(studentRepository.findById(7L)).thenReturn(Optional.of(student));
		when(studentRepository.existsByEmailAndIdNot("new@example.com", 7L)).thenReturn(false);
		when(studentRepository.save(student)).thenReturn(student);

		StudentResponse result = studentService.editStudent(7L, new StudentRequest("New Name", "new@example.com"));

		assertEquals("New Name", result.name());
		assertEquals("new@example.com", result.email());
		assertEquals(createdAt, result.createdAt());
		assertEquals(student.getUpdatedAt(), result.updatedAt());
		assertTrue(!result.updatedAt().isBefore(previousUpdatedAt));
	}

	@Test
	void rejectsEditingStudentToAnotherStudentsEmail() {
		when(studentRepository.findById(7L)).thenReturn(Optional.of(new Student("Ada", "ada@example.com")));
		when(studentRepository.existsByEmailAndIdNot("taken@example.com", 7L)).thenReturn(true);

		assertThrows(StudentEmailAlreadyExistsException.class, () -> studentService.editStudent(
				7L, new StudentRequest("Ada", "taken@example.com")));

		verify(studentRepository, never()).save(any(Student.class));
	}

	@Test
	void throwsWhenEditingStudentDoesNotExist() {
		when(studentRepository.findById(7L)).thenReturn(Optional.empty());

		assertThrows(StudentNotFoundException.class, () -> studentService.editStudent(
				7L, new StudentRequest("Ada", "ada@example.com")));

		verify(studentRepository, never()).save(any(Student.class));
	}

	@Test
	void deletesExistingStudent() {
		Student student = new Student("Ada", "ada@example.com");
		when(studentRepository.findById(7L)).thenReturn(Optional.of(student));

		studentService.deleteStudent(7L);

		verify(studentRepository).delete(student);
	}

	@Test
	void throwsWhenDeletingStudentDoesNotExist() {
		when(studentRepository.findById(7L)).thenReturn(Optional.empty());

		assertThrows(StudentNotFoundException.class, () -> studentService.deleteStudent(7L));

		verify(studentRepository, never()).delete(any(Student.class));
	}
}
