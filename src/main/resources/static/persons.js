const form = document.getElementById("person-form");
const tbody = document.getElementById("persons-tbody");
const messageEl = document.getElementById("form-message");
const formTitle = document.getElementById("form-title");
const saveBtn = document.getElementById("save-btn");
const cancelBtn = document.getElementById("cancel-btn");

let editingId = null;

function loadPersons() {
  const persons = window.pmStore.listPersons();
  renderTable(persons);
}

function renderTable(persons) {
  tbody.innerHTML = "";

  if (!persons.length) {
    const row = document.createElement("tr");
    row.className = "empty-row";
    row.innerHTML = '<td colspan="5">Noch keine Personen vorhanden.</td>';
    tbody.appendChild(row);
    return;
  }

  for (const person of persons) {
    const row = document.createElement("tr");
    row.innerHTML = `
      <td>${escapeHtml(person.firstName)} ${escapeHtml(person.lastName)}</td>
      <td>${escapeHtml(person.email ?? "-")}</td>
      <td>${escapeHtml(person.phone ?? "-")}</td>
      <td>${escapeHtml(person.notes ?? "-")}</td>
      <td class="actions-cell">
        <button class="tiny edit" data-action="edit" data-id="${person.id}">Bearbeiten</button>
        <button class="tiny delete" data-action="delete" data-id="${person.id}">Loeschen</button>
      </td>
    `;
    tbody.appendChild(row);
  }
}

form.addEventListener("submit", (event) => {
  event.preventDefault();
  clearMessage();

  const payload = {
    firstName: form.firstName.value.trim(),
    lastName: form.lastName.value.trim(),
    email: normalize(form.email.value),
    phone: normalize(form.phone.value),
    notes: normalize(form.notes.value),
  };

  if (!payload.firstName || !payload.lastName) {
    showMessage("Vorname und Nachname sind Pflichtfelder.", "error");
    return;
  }

  if (editingId) {
    window.pmStore.updatePerson(editingId, payload);
    showMessage("Person aktualisiert.", "success");
  } else {
    window.pmStore.createPerson(payload);
    showMessage("Person angelegt.", "success");
  }
  resetForm();
  loadPersons();
});

tbody.addEventListener("click", (event) => {
  const target = event.target;
  if (!(target instanceof HTMLButtonElement)) {
    return;
  }

  const action = target.dataset.action;
  const id = target.dataset.id;
  if (!action || !id) {
    return;
  }

  if (action === "delete") {
    if (!window.confirm("Person wirklich löschen?")) {
      return;
    }
    const deleted = window.pmStore.deletePerson(id);
    if (!deleted) {
      showMessage("Loeschen fehlgeschlagen.", "error");
      return;
    }
    showMessage("Person gelöscht.", "success");
    if (editingId === id) {
      resetForm();
    }
    loadPersons();
    return;
  }

  if (action === "edit") {
    const person = window.pmStore.listPersons().find((entry) => entry.id === id);
    if (!person) {
      showMessage("Person nicht gefunden.", "error");
      return;
    }
    editingId = id;
    formTitle.textContent = "Person bearbeiten";
    saveBtn.textContent = "Aenderungen speichern";
    cancelBtn.classList.remove("hidden");
    form.firstName.value = person.firstName ?? "";
    form.lastName.value = person.lastName ?? "";
    form.email.value = person.email ?? "";
    form.phone.value = person.phone ?? "";
    form.notes.value = person.notes ?? "";
    window.scrollTo({ top: 0, behavior: "smooth" });
  }
});

cancelBtn.addEventListener("click", () => {
  resetForm();
  clearMessage();
});

function resetForm() {
  editingId = null;
  form.reset();
  formTitle.textContent = "Neue Person";
  saveBtn.textContent = "Person speichern";
  cancelBtn.classList.add("hidden");
}

function normalize(value) {
  const trimmed = value.trim();
  return trimmed.length ? trimmed : null;
}

function showMessage(text, type) {
  messageEl.textContent = text;
  messageEl.classList.remove("success", "error");
  messageEl.classList.add(type);
}

function clearMessage() {
  messageEl.textContent = "";
  messageEl.classList.remove("success", "error");
}

function escapeHtml(value) {
  return String(value)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

loadPersons();
window.addEventListener("storage", loadPersons);
