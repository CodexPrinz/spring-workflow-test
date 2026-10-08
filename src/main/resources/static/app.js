const apiUrl = "/api/students";
const studentList = document.querySelector("#student-list");
const studentCount = document.querySelector("#student-count");
const resultsCount = document.querySelector("#results-count");
const emptyState = document.querySelector("#empty-state");
const searchInput = document.querySelector("#search-input");
const notice = document.querySelector("#notice");
const formPanel = document.querySelector("#form-panel");
const studentForm = document.querySelector("#student-form");
const formTitle = document.querySelector("#form-title");
const formError = document.querySelector("#form-error");
const nameInput = document.querySelector("#name-input");
const emailInput = document.querySelector("#email-input");
const saveButton = document.querySelector("#save-button");

let students = [];
let editingStudentId = null;

function showNotice(message, isError = false) {
	notice.textContent = message;
	notice.classList.toggle("error", isError);
	notice.hidden = false;
}

function clearNotice() {
	notice.hidden = true;
	notice.textContent = "";
	notice.classList.remove("error");
}

async function request(path, options = {}) {
	let response;
	try {
		response = await fetch(path, {
			...options,
			headers: {
				...(options.body ? { "Content-Type": "application/json" } : {}),
				...options.headers
			}
		});
	} catch {
		throw new Error("Could not reach the server. Check that the application is running and try again.");
	}

	if (response.ok) {
		return response.status === 204 ? null : response.json();
	}

	let error;
	try {
		error = await response.json();
	} catch {
		throw new Error(`Request failed (${response.status}). Please try again.`);
	}

	const validationMessages = Object.values(error.validationErrors || {});
	const message = [error.message, ...validationMessages].filter(Boolean).join(". ");
	throw new Error(message || `Request failed (${response.status}). Please try again.`);
}

function initials(name) {
	return name
		.trim()
		.split(/\s+/)
		.slice(0, 2)
		.map((part) => Array.from(part)[0] || "")
		.join("")
		.toUpperCase();
}

function formatDate(value) {
	const date = new Date(value);
	if (Number.isNaN(date.getTime())) {
		return "—";
	}
	return new Intl.DateTimeFormat(undefined, {
		month: "short",
		day: "numeric",
		year: "numeric"
	}).format(date);
}

function createActionButton(label, student, action, extraClass = "") {
	const button = document.createElement("button");
	button.className = `row-button ${extraClass}`.trim();
	button.type = "button";
	button.textContent = label;
	button.setAttribute("aria-label", `${label} ${student.name}`);
	button.addEventListener("click", () => action(student));
	return button;
}

function renderStudents() {
	const query = searchInput.value.trim().toLocaleLowerCase();
	const visibleStudents = students.filter((student) =>
		`${student.name} ${student.email}`.toLocaleLowerCase().includes(query)
	);

	studentList.replaceChildren();
	for (const student of visibleStudents) {
		const row = document.createElement("tr");
		const studentCell = document.createElement("td");
		const studentIdentity = document.createElement("div");
		const avatar = document.createElement("span");
		const name = document.createElement("span");
		const emailCell = document.createElement("td");
		const dateCell = document.createElement("td");
		const actionsCell = document.createElement("td");
		const actions = document.createElement("div");

		studentIdentity.className = "student-cell";
		avatar.className = "avatar";
		avatar.setAttribute("aria-hidden", "true");
		avatar.textContent = initials(student.name);
		name.textContent = student.name;
		studentIdentity.append(avatar, name);
		studentCell.append(studentIdentity);

		emailCell.textContent = student.email;
		dateCell.className = "date-cell";
		dateCell.textContent = formatDate(student.createdAt);
		actions.className = "row-actions";
		actions.append(
			createActionButton("Edit", student, openEditForm),
			createActionButton("Delete", student, deleteStudent, "delete")
		);
		actionsCell.append(actions);
		row.append(studentCell, emailCell, dateCell, actionsCell);
		studentList.append(row);
	}

	studentCount.textContent = String(students.length);
	resultsCount.textContent = `Showing ${visibleStudents.length} of ${students.length} students`;
	emptyState.hidden = visibleStudents.length > 0;
}

async function loadStudents({ clear = true } = {}) {
	if (clear) {
		clearNotice();
	}
	resultsCount.textContent = "Loading students…";
	try {
		const result = await request(apiUrl);
		if (!Array.isArray(result)) {
			throw new Error("The server returned an unexpected student list.");
		}
		students = result;
		renderStudents();
		return true;
	} catch (error) {
		students = [];
		renderStudents();
		resultsCount.textContent = "Student list could not be loaded";
		showNotice(error.message, true);
		return false;
	}
}

function openForm(student = null) {
	clearNotice();
	formError.hidden = true;
	formError.textContent = "";
	editingStudentId = student?.id ?? null;
	formTitle.textContent = student ? "Edit student" : "Add a student";
	saveButton.textContent = student ? "Save changes" : "Save student";
	nameInput.value = student?.name ?? "";
	emailInput.value = student?.email ?? "";
	formPanel.hidden = false;
	formPanel.scrollIntoView({ behavior: "smooth", block: "nearest" });
	nameInput.focus();
}

function openEditForm(student) {
	openForm(student);
}

function closeForm() {
	formPanel.hidden = true;
	studentForm.reset();
	formError.hidden = true;
	editingStudentId = null;
}

async function saveStudent(event) {
	event.preventDefault();
	formError.hidden = true;
	saveButton.disabled = true;
	const payload = {
		name: nameInput.value.trim(),
		email: emailInput.value.trim()
	};
	const isEditing = editingStudentId !== null;
	const path = isEditing ? `${apiUrl}/${encodeURIComponent(editingStudentId)}` : apiUrl;
	const method = isEditing ? "PUT" : "POST";

	try {
		await request(path, { method, body: JSON.stringify(payload) });
		closeForm();
		const refreshed = await loadStudents({ clear: false });
		if (refreshed) {
			showNotice(isEditing ? "Student updated successfully." : "Student added successfully.");
		}
	} catch (error) {
		formError.textContent = error.message;
		formError.hidden = false;
	} finally {
		saveButton.disabled = false;
	}
}

async function deleteStudent(student) {
	if (!window.confirm(`Delete ${student.name} from the student directory? This cannot be undone.`)) {
		return;
	}

	clearNotice();
	try {
		await request(`${apiUrl}/${encodeURIComponent(student.id)}`, { method: "DELETE" });
		if (await loadStudents({ clear: false })) {
			showNotice(`${student.name} was deleted.`);
		}
	} catch (error) {
		showNotice(error.message, true);
	}
}

document.querySelector("#add-student-button").addEventListener("click", () => openForm());
document.querySelector("#close-form-button").addEventListener("click", closeForm);
document.querySelector("#cancel-button").addEventListener("click", closeForm);
document.querySelector("#refresh-button").addEventListener("click", loadStudents);
searchInput.addEventListener("input", renderStudents);
studentForm.addEventListener("submit", saveStudent);

loadStudents();
