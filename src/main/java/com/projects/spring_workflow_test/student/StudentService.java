package com.projects.spring_workflow_test.student;

import java.util.List;
import java.util.function.Supplier;

import com.projects.spring_workflow_test.student.dto.StudentRequest;
import com.projects.spring_workflow_test.student.dto.StudentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentService {

	private static final Logger log = LoggerFactory.getLogger(StudentService.class);

	private final StudentRepository studentRepository;

	public StudentService(StudentRepository studentRepository) {
		this.studentRepository = studentRepository;
	}

	@Transactional(readOnly = true)
	public List<StudentResponse> getAllStudents() {
		return timed("getAllStudents", () -> studentRepository.findAll().stream()
				.map(this::toResponse)
				.toList());
	}

	@Transactional
	public StudentResponse addStudent(StudentRequest request) {
		return timed("addStudent", () -> {
			if (studentRepository.existsByEmail(request.email())) {
				throw new StudentEmailAlreadyExistsException(request.email());
			}
			return toResponse(studentRepository.save(new Student(request.name(), request.email())));
		});
	}

	@Transactional
	public StudentResponse editStudent(Long id, StudentRequest request) {
		return timed("editStudent", () -> {
			Student student = findStudent(id);
			if (studentRepository.existsByEmailAndIdNot(request.email(), id)) {
				throw new StudentEmailAlreadyExistsException(request.email());
			}
			student.update(request.name(), request.email());
			return toResponse(studentRepository.save(student));
		});
	}

	@Transactional
	public void deleteStudent(Long id) {
		timed("deleteStudent", () -> {
			studentRepository.delete(findStudent(id));
			return null;
		});
	}

	private Student findStudent(Long id) {
		return studentRepository.findById(id).orElseThrow(() -> new StudentNotFoundException(id));
	}

	private StudentResponse toResponse(Student student) {
		return new StudentResponse(
				student.getId(), student.getName(), student.getEmail(), student.getCreatedAt(), student.getUpdatedAt());
	}

	private <T> T timed(String operation, Supplier<T> action) {
		long start = System.nanoTime();
		log.info("Starting student service operation: {}", operation);
		try {
			return action.get();
		}
		finally {
			double elapsedMilliseconds = (System.nanoTime() - start) / 1_000_000.0;
			log.info("Finished student service operation: {} in {} ms", operation, elapsedMilliseconds);
		}
	}
}
