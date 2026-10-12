package com.projects.spring_workflow_test.student;

import java.time.Instant;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "students")
public class Student {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false, unique = true)
	private String email;

	@Column(name = "date_of_birth")
	private LocalDate dateOfBirth;

	@Column(length = 100)
	private String nationality;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@Column(nullable = false)
	private Instant updatedAt;

	protected Student() {
	}

	public Student(String name, String email, LocalDate dateOfBirth, String nationality) {
		this.name = name;
		this.email = email;
		this.dateOfBirth = dateOfBirth;
		this.nationality = nationality;
		this.createdAt = Instant.now();
		this.updatedAt = createdAt;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}

	public LocalDate getDateOfBirth() {
		return dateOfBirth;
	}

	public String getNationality() {
		return nationality;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void update(String name, String email, LocalDate dateOfBirth, String nationality) {
		this.name = name;
		this.email = email;
		this.dateOfBirth = dateOfBirth;
		this.nationality = nationality;
		this.updatedAt = Instant.now();
	}

	@PrePersist
	private void auditCreation() {
		Instant now = Instant.now();
		if (createdAt == null) {
			createdAt = now;
		}
		updatedAt = now;
	}

}
