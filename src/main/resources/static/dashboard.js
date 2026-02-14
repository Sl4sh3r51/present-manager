function loadDashboard() {
  const personsCount = window.pmStore.listPersons().length;
  const occasionsCount = window.pmStore.listOccasions().length;
  document.getElementById("person-count").textContent = String(personsCount);
  document.getElementById("occasion-count").textContent = String(occasionsCount);
}

loadDashboard();
window.addEventListener("storage", loadDashboard);
