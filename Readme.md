# 🎁 Present Manager – Backend

![Build main](https://github.com/Sl4sh3r51/present-manager/actions/workflows/build.yml/badge.svg?branch=main)
![Java](https://img.shields.io/badge/Java-21-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.x-green)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)

---

## Übersicht

Der **Present Manager** ist eine REST-API zur Verwaltung von Geschenken, Geschenkideen, Personen, Anlässen und Aufgaben. Die Anwendung ermöglicht es Benutzern, Geschenkideen für Personen zu planen, Anlässe zu verwalten und den Kaufstatus von Geschenken zu verfolgen (`PLANNED → BOUGHT → GIFTED`).

Das Backend ist als **Spring Boot Anwendung** realisiert und kommuniziert mit einer **PostgreSQL-Datenbank** (gehostet auf Supabase). Die Authentifizierung erfolgt über **Supabase Auth** mittels JWT-Token.

### Funktionsumfang

| Bereich | Beschreibung |
|---|---|
| **Personen** | CRUD, Statusverwaltung, Geburtstage |
| **Interessen** | Globale Interessenliste, Zuordnung zu Personen |
| **Anlässe** | FIXED & CUSTOM, wiederkehrend, Datumsfilter |
| **Geschenkideen** | Planung mit Person- und Anlasszuordnung |
| **Geschenke** | Workflow PLANNED → BOUGHT → GIFTED, automatische Datumsfelder |
| **Aufgaben** | To-Do-Verwaltung pro Person |

---

## Technologie-Stack

| Komponente | Technologie                                     |
|---|-------------------------------------------------|
| Sprache | Java 21                                         |
| Framework | Spring Boot 4.x                                 |
| Sicherheit | Spring Security + OAuth2 Resource Server (JWT)  |
| Authentifizierung | Supabase Auth (ES256 JWT)                       |
| Datenbank | PostgreSQL 16 (Supabase)                        |
| ORM | Hibernate / Spring Data JPA                     |
| Build-Tool | Maven                                           |
| Tests | JUnit 5, Mockito, Spring Boot Test, DataJpaTest |
| CI/CD | GitHub Actions                                  |

---

## Voraussetzungen

- **Java 21** oder höher
- **Maven 3.9+**
- **PostgreSQL 16** (lokal oder Supabase)
- Zugang zu einem **Supabase-Projekt** (für Authentifizierung)

---

## Installation und lokale Ausführung

### 1. Repository klonen

```bash
git clone https://github.com/DEIN-USERNAME/present-manager.git
cd present-manager
```

### 2. Umgebungsvariablen setzen

Die Anwendung liest Datenbankverbindung und Konfiguration aus Umgebungsvariablen. Diese müssen vor dem Start gesetzt werden:

```bash
export SPRING_DATASOURCE_URL
export SPRING_DATASOURCE_USERNAME
export SPRING_DATASOURCE_PASSWORD
```

Alternativ können die Werte in `src/main/resources/application.properties` direkt eingetragen werden (nicht für Produktion empfohlen).

### 3. Anwendung bauen

```bash
mvn clean install
```

### 4. Anwendung starten

```bash
java -jar target/present-manager-0.0.1-SNAPSHOT.jar
```

Die API ist anschließend unter `http://localhost:8080` erreichbar.

---

## Konfiguration

### (Hauptkonfiguration)
`application.properties`

```properties
# Datenbankverbindung – wird über Umgebungsvariablen gesetzt
# SPRING_DATASOURCE_URL
# SPRING_DATASOURCE_USERNAME
# SPRING_DATASOURCE_PASSWORD

# Supabase JWT – JWKS-Endpunkt des Projekts
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=https://<project-ref>.supabase.co/auth/v1/.well-known/jwks.json

# CORS – erlaubte Origins (Frontend-URL)
cors.allowed-origins=http://localhost:5173,https://present-manager-frontend.pages.dev/

# Hibernate
spring.jpa.hibernate.ddl-auto=none
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

> **Hinweis:** Die `jwk-set-uri` muss auf den JWKS-Endpunkt des eigenen Supabase-Projekts zeigen. Den Wert findet man im Supabase-Dashboard unter **Project Settings → API → JWT Settings**.

### Pflicht-Umgebungsvariablen

| Variable | Beschreibung |
|---|---|
| `SPRING_DATASOURCE_URL` | JDBC-URL zur PostgreSQL-Datenbank |
| `SPRING_DATASOURCE_USERNAME` | Datenbankbenutzer |
| `SPRING_DATASOURCE_PASSWORD` | Datenbankpasswort |

---

## Tests ausführen

### Voraussetzung: PostgreSQL Testdatenbank

Die Tests laufen gegen eine echte PostgreSQL-Datenbank. Lokal kann eine Testinstanz per Docker gestartet werden:

```bash
docker run --name presentmanager-test \
  -e POSTGRES_DB=presentmanager_test \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  -d postgres:16
```

### Testprofil

Die Testkonfiguration liegt in `src/test/resources/application-test.properties` und wird über das Profil `test` aktiviert. Sie enthält:

- Verbindung zur lokalen Testdatenbank
- `ddl-auto=create-drop` (Schema wird vor/nach jedem Test-Run erstellt und gelöscht)
- Dummy-JWKS-URI für Spring Security

### Tests starten

```bash
mvn test -Dspring.profiles.active=test
```

### Teststruktur

Die Tests sind in drei Schichten aufgeteilt:

| Typ | Annotation | Beschreibung |
|---|---|---|
| Controller-Tests | `@WebMvcTest` | HTTP-Schicht mit MockMvc, Spring Security aktiv |
| Service-Tests | `@ExtendWith(MockitoExtension)` | Geschäftslogik mit gemockten Repositories |
| Repository-Tests | `@DataJpaTest` | SQL-Queries gegen echte PostgreSQL-Testdatenbank |

---

## CI/CD

Die Pipeline ist in `.github/workflows/build.yml` definiert und läuft bei jedem Push auf `develop` und `main`.

### Pipeline-Schritte

```
Push → Checkout → Java 21 setup → PostgreSQL Service starten → mvn test → mvn package
```

### PostgreSQL in der Pipeline

GitHub Actions startet einen PostgreSQL-16-Service-Container, gegen den die Tests laufen. Die Verbindungsdaten werden als Umgebungsvariablen an Maven übergeben:

```yaml
services:
  postgres:
    image: postgres:16
    env:
      POSTGRES_DB: presentmanager_test
      POSTGRES_PASSWORD: postgres
```

> Für die Pipeline werden **keine** Supabase-Secrets benötigt. Die JWT-Validierung wird im Testprofil durch eine Dummy-URI deaktiviert.

---

## Projektstruktur

```
src/
├── main/
│   ├── java/org/iu/presentmanager/
│   │   ├── config/          # WebConfig, UUIDPrincipalArgumentResolver
│   │   ├── exceptions/      # GlobalExceptionHandler, Custom Exceptions
│   │   ├── security/        # SecurityConfig, SupabaseJwtConverter
│   │   ├── persons/         # Person Entity, Controller, Service, Repository
│   │   ├── interests/       # Interest Entity, Controller, Service, Repository
│   │   ├── personinterests/ # PersonInterest (N:M), Service, Repository
│   │   ├── occasions/       # Occasion Entity, Controller, Service, Repository
│   │   ├── giftideas/       # GiftIdea Entity, Controller, Service, Repository
│   │   ├── gifts/           # Gift Entity, Controller, Service, Repository
│   │   └── tasks/           # Task Entity, Controller, Service, Repository
│   └── resources/
│       ├── application.properties
│       ├── checkstyle-suppressions.xml
│       └── spotbugs-exclude.xml
└── test/
    ├── java/org/iu/presentmanager/
    │   ├── persons/         # PersonControllerTest, PersonServiceTest, PersonRepositoryTest
    │   ├── interests/       # ...
    │   ├── personinterests/ # ...
    │   ├── occasions/       # ...
    │   ├── giftideas/       # ...
    │   ├── gifts/           # ...
    │   └── tasks/           # ...
    └── resources/
        └── application-test.properties
```

---

## Sicherheit

Alle Endpunkte sind durch Spring Security geschützt. Requests müssen einen gültigen **Supabase-JWT** im `Authorization`-Header mitschicken:

```
Authorization: Bearer <jwt-token>
```

Der JWT wird über den JWKS-Endpunkt von Supabase mit dem Algorithmus **ES256** validiert. Der `sub`-Claim des Tokens wird als `userId` (UUID) verwendet und in jedem Request über `@AuthenticationPrincipal UUID userId` injiziert.

Datenisolierung zwischen Benutzern wird auf Service-Ebene durch das Muster `findByIdAndUserId()` sichergestellt — kein Benutzer kann auf Daten eines anderen zugreifen.

---

## Statische Code-Analyse

Das Projekt verwendet drei Code-Quality-Tools, die über `mvn site` ausgeführt werden:

| Tool | Beschreibung |
|---|---|
| **Checkstyle** | Google Java Style (mit projektspezifischen Suppressions) |
| **SpotBugs** | Statische Fehleranalyse |
| **PMD** | Code-Qualität und Best Practices |

Konfigurationsdateien: `src/main/resources/checkstyle-suppressions.xml`, `src/main/resources/spotbugs-exclude.xml`

---
