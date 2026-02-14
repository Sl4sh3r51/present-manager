const PM_STORAGE = {
  persons: "pm_persons_v1",
  occasions: "pm_occasions_v1",
};

const FIXED_OCCASION_TYPES = ["Geburtstag", "Weihnachten"];

function readJson(key, fallback = []) {
  try {
    const raw = localStorage.getItem(key);
    return raw ? JSON.parse(raw) : fallback;
  } catch (_error) {
    return fallback;
  }
}

function writeJson(key, value) {
  localStorage.setItem(key, JSON.stringify(value));
}

function uid() {
  return crypto.randomUUID();
}

function nowIso() {
  return new Date().toISOString();
}

function seedOccasions() {
  const current = readJson(PM_STORAGE.occasions, []);
  if (current.length > 0) {
    return;
  }

  const year = new Date().getFullYear();
  const seeded = [
    {
      id: uid(),
      title: "Geburtstag (Standard)",
      occasionType: "Geburtstag",
      occasionDate: `${year}-01-01`,
      personName: "",
      notes: "Vordefinierter Anlass",
      fixed: true,
      createdAt: nowIso(),
      updatedAt: nowIso(),
    },
    {
      id: uid(),
      title: "Weihnachten",
      occasionType: "Weihnachten",
      occasionDate: `${year}-12-24`,
      personName: "",
      notes: "Vordefinierter Anlass",
      fixed: true,
      createdAt: nowIso(),
      updatedAt: nowIso(),
    },
  ];
  writeJson(PM_STORAGE.occasions, seeded);
}

seedOccasions();

window.pmStore = {
  getFixedOccasionTypes() {
    return [...FIXED_OCCASION_TYPES];
  },

  listPersons() {
    return readJson(PM_STORAGE.persons, []);
  },
  createPerson(payload) {
    const persons = readJson(PM_STORAGE.persons, []);
    const entry = {id: uid(), ...payload, createdAt: nowIso(), updatedAt: nowIso()};
    persons.push(entry);
    writeJson(PM_STORAGE.persons, persons);
    return entry;
  },
  updatePerson(id, payload) {
    const persons = readJson(PM_STORAGE.persons, []);
    const index = persons.findIndex((person) => person.id === id);
    if (index === -1) {
      return null;
    }
    persons[index] = {...persons[index], ...payload, updatedAt: nowIso()};
    writeJson(PM_STORAGE.persons, persons);
    return persons[index];
  },
  deletePerson(id) {
    const persons = readJson(PM_STORAGE.persons, []);
    const next = persons.filter((person) => person.id !== id);
    writeJson(PM_STORAGE.persons, next);
    return next.length !== persons.length;
  },

  listOccasions() {
    return readJson(PM_STORAGE.occasions, []);
  },
  createOccasion(payload) {
    const occasions = readJson(PM_STORAGE.occasions, []);
    const entry = {id: uid(), fixed: false, ...payload, createdAt: nowIso(), updatedAt: nowIso()};
    occasions.push(entry);
    writeJson(PM_STORAGE.occasions, occasions);
    return entry;
  },
  updateOccasion(id, payload) {
    const occasions = readJson(PM_STORAGE.occasions, []);
    const index = occasions.findIndex((occasion) => occasion.id === id);
    if (index === -1) {
      return null;
    }
    occasions[index] = {...occasions[index], ...payload, updatedAt: nowIso()};
    writeJson(PM_STORAGE.occasions, occasions);
    return occasions[index];
  },
  deleteOccasion(id) {
    const occasions = readJson(PM_STORAGE.occasions, []);
    const target = occasions.find((occasion) => occasion.id === id);
    if (target?.fixed) {
      return false;
    }
    const next = occasions.filter((occasion) => occasion.id !== id);
    writeJson(PM_STORAGE.occasions, next);
    return next.length !== occasions.length;
  },
};
