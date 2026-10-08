package com.projects.spring_workflow_test.student;

import java.util.List;
import java.util.function.Supplier;

import com.projects.spring_workflow_test.student.dto.StudentRequest;
import com.projects.spring_workflow_test.student.dto.StudentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

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

	@Transactional(readOnly = true)
	public StudentResponse getStudentById(Long id) {
		return timed("getStudentById", () -> toResponse(findStudent(id)));
	}

	@Transactional(readOnly = true)
	public StudentResponse getStudentByName(String name) {
		return timed("getStudentByName", () -> {
			String safeName = sanitize(name);
			Student student = studentRepository.findByName(safeName)
					.orElseThrow(() -> new StudentNotFoundException(safeName));
			return toResponse(student);
		});
	}

	@Transactional
	public StudentResponse addStudent(StudentRequest request) {
		return timed("addStudent", () -> {
			String sanitizedEmail = sanitize(request.email());
			if (studentRepository.existsByEmail(sanitizedEmail)) {
				throw new StudentEmailAlreadyExistsException(sanitizedEmail);
			}
			return toResponse(studentRepository.save(new Student(sanitize(request.name()), sanitizedEmail)));
		});
	}

	@Transactional
	public StudentResponse editStudent(Long id, StudentRequest request) {
		return timed("editStudent", () -> {
			Student student = findStudent(id);
			String sanitizedEmail = sanitize(request.email());
			if (studentRepository.existsByEmailAndIdNot(sanitizedEmail, id)) {
				throw new StudentEmailAlreadyExistsException(sanitizedEmail);
			}
			student.update(sanitize(request.name()), sanitizedEmail);
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
				student.getId(), sanitize(student.getName()), sanitize(student.getEmail()),
				student.getCreatedAt(), student.getUpdatedAt());
	}

	private String sanitize(String value) {
		if (value == null) {
			return null;
		}

		String sanitized = value;
		for (int i = 0; i < 2; i++) {
			sanitized = HtmlUtils.htmlUnescape(sanitized);
			sanitized = sanitized.replaceAll("(?is)<[^>]*>", "");
		}
		return sanitized.trim();
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
