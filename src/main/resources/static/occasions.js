const form = document.getElementById("occasion-form");
const tbody = document.getElementById("occasions-tbody");
const messageEl = document.getElementById("form-message");
const formTitle = document.getElementById("form-title");
const saveBtn = document.getElementById("save-btn");
const cancelBtn = document.getElementById("cancel-btn");

let editingId = null;

function loadOccasions() {
  const occasions = window.pmStore.listOccasions();
  renderTable(occasions);
}

function renderTable(occasions) {
  tbody.innerHTML = "";

  if (!occasions.length) {
    const row = document.createElement("tr");
    row.className = "empty-row";
    row.innerHTML = '<td colspan="6">Noch keine Anlässe vorhanden.</td>';
    tbody.appendChild(row);
    return;
  }

  for (const occasion of occasions) {
    const fixedBadge = occasion.fixed ? " <small>(fix)</small>" : "";
    const row = document.createElement("tr");
    row.innerHTML = `
      <td>${escapeHtml(occasion.title)}${fixedBadge}</td>
      <td>${escapeHtml(occasion.occasionType ?? "-")}</td>
      <td>${escapeHtml(formatDate(occasion.occasionDate))}</td>
      <td>${escapeHtml(occasion.personName ?? "-")}</td>
      <td>${escapeHtml(occasion.notes ?? "-")}</td>
      <td class="actions-cell">
        <button class="tiny edit" data-action="edit" data-id="${occasion.id}">Bearbeiten</button>
        <button class="tiny delete" data-action="delete" data-id="${occasion.id}" ${occasion.fixed ? "disabled" : ""}>Loeschen</button>
      </td>
    `;
    tbody.appendChild(row);
  }
}

form.addEventListener("submit", (event) => {
  event.preventDefault();
  clearMessage();

  const payload = {
    title: form.title.value.trim(),
    occasionType: normalize(form.occasionType.value),
    occasionDate: normalize(form.occasionDate.value),
    personName: normalize(form.personName.value),
    notes: normalize(form.notes.value),
  };

  if (!payload.title) {
    showMessage("Titel ist Pflicht.", "error");
    return;
  }

  if (editingId) {
    window.pmStore.updateOccasion(editingId, payload);
    showMessage("Anlass aktualisiert.", "success");
  } else {
    window.pmStore.createOccasion(payload);
    showMessage("Anlass angelegt.", "success");
  }

  resetForm();
  loadOccasions();
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
    if (!window.confirm("Anlass wirklich löschen?")) {
      return;
    }
    const ok = window.pmStore.deleteOccasion(id);
    if (!ok) {
      showMessage("Feste Anlässe können nicht gelöscht werden.", "error");
      return;
    }
    showMessage("Anlass gelöscht.", "success");
    if (editingId === id) {
      resetForm();
    }
    loadOccasions();
    return;
  }

  if (action === "edit") {
    const occasion = window.pmStore.listOccasions().find((entry) => entry.id === id);
    if (!occasion) {
      showMessage("Anlass nicht gefunden.", "error");
      return;
    }

    editingId = id;
    formTitle.textContent = "Anlass bearbeiten";
    saveBtn.textContent = "Aenderungen speichern";
    cancelBtn.classList.remove("hidden");
    form.title.value = occasion.title ?? "";
    form.occasionType.value = occasion.occasionType ?? "";
    form.occasionDate.value = occasion.occasionDate ?? "";
    form.personName.value = occasion.personName ?? "";
    form.notes.value = occasion.notes ?? "";
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
  formTitle.textContent = "Neuer Anlass";
  saveBtn.textContent = "Anlass speichern";
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

function formatDate(value) {
  if (!value) {
    return "-";
  }
  const [year, month, day] = value.split("-");
  return `${day}.${month}.${year}`;
}

function escapeHtml(value) {
  return String(value)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

loadOccasions();
window.addEventListener("storage", loadOccasions);
