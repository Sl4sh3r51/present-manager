package org.iu.presentmanager.persons;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class PersonRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PersonRepository personRepository;

    private UUID userId;
    private UUID otherUserId;
    private Person alicePerson;
    private Person evePerson;

    private Person createTestPerson(String name, PersonStatus status, LocalDate birthday, UUID userId) {
        Person person = new Person();
        person.setName(name);
        person.setUserId(userId);
        person.setStatus(status);
        person.setBirthday(birthday);
        return person;
    }

    @BeforeEach
    void setUp() {
        userId = UUID.fromString("10000000-0000-0000-0000-000000000001");
        otherUserId = UUID.fromString("20000000-0000-0000-0000-000000000002");

        alicePerson = createTestPerson("Alice", PersonStatus.PLANNED, LocalDate.of(1990, 3, 15), userId);
        Person bobPerson = createTestPerson("Bob", PersonStatus.COMPLETED, LocalDate.of(1985, 6, 20), userId);
        Person charliePerson = createTestPerson("Charlie", PersonStatus.PLANNED, LocalDate.of(1992, 3, 15), userId);
        Person davePerson = createTestPerson("Dave", PersonStatus.NONE, null, userId);
        evePerson = createTestPerson("Eve", PersonStatus.PLANNED, LocalDate.of(1990, 3, 15), otherUserId);

        entityManager.persistAndFlush(alicePerson);
        entityManager.persistAndFlush(bobPerson);
        entityManager.persistAndFlush(charliePerson);
        entityManager.persistAndFlush(davePerson);
        entityManager.persistAndFlush(evePerson);
    }

    @Test
    void shouldFindAllPersonsByUserId() {
        // WHEN
        List<Person> result = personRepository.findByUserId(userId);

        // THEN
        assertNotNull(result);
        assertEquals(4, result.size());
        assertTrue(result.stream().allMatch(p -> p.getUserId().equals(userId)));
        assertTrue(result.stream().anyMatch(p -> p.getName().equals("Alice")));
        assertTrue(result.stream().anyMatch(p -> p.getName().equals("Bob")));
    }

    @Test
    void shouldReturnEmptyListForNonExistentUser() {
        // GIVEN
        UUID nonExistentUserId = UUID.fromString("99999999-0000-0000-0000-000000000099");

        // WHEN
        List<Person> result = personRepository.findByUserId(nonExistentUserId);

        // THEN
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldIsolateBetweenUsers() {
        // WHEN
        List<Person> userResult = personRepository.findByUserId(userId);
        List<Person> otherUserResult = personRepository.findByUserId(otherUserId);

        // THEN
        assertEquals(4, userResult.size());
        assertEquals(1, otherUserResult.size());
        assertTrue(userResult.stream().noneMatch(p -> p.getUserId().equals(otherUserId)));
        assertTrue(otherUserResult.stream().noneMatch(p -> p.getUserId().equals(userId)));
    }

    @Test
    void shouldFindPersonsByUserIdAndStatus() {
        // WHEN
        List<Person> plannedPersons = personRepository.findByUserIdAndStatus(userId, PersonStatus.PLANNED);
        List<Person> completedPersons = personRepository.findByUserIdAndStatus(userId, PersonStatus.COMPLETED);

        // THEN
        assertEquals(2, plannedPersons.size()); // Alice, Charlie
        assertEquals(1, completedPersons.size()); // Bob
        assertTrue(plannedPersons.stream().allMatch(p -> p.getStatus().equals(PersonStatus.PLANNED)));
        assertTrue(completedPersons.stream().allMatch(p -> p.getStatus().equals(PersonStatus.COMPLETED)));
    }

    @Test
    void shouldReturnEmptyListForStatusWithoutPersons() {
        // WHEN
        List<Person> result = personRepository.findByUserIdAndStatus(userId, PersonStatus.IDEAS);

        // THEN
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyListForNonExistentUserWithStatus() {
        // GIVEN
        UUID nonExistentUserId = UUID.fromString("99999999-0000-0000-0000-000000000099");

        // WHEN
        List<Person> result = personRepository.findByUserIdAndStatus(nonExistentUserId, PersonStatus.PLANNED);

        // THEN
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldSupportAllStatusTypes() {
        // GIVEN
        entityManager.persistAndFlush(createTestPerson("Frank", PersonStatus.IDEAS, LocalDate.of(1995, 1, 1), userId));

        // WHEN & THEN
        assertEquals(2, personRepository.findByUserIdAndStatus(userId, PersonStatus.PLANNED).size());
        assertEquals(1, personRepository.findByUserIdAndStatus(userId, PersonStatus.COMPLETED).size());
        assertEquals(1, personRepository.findByUserIdAndStatus(userId, PersonStatus.IDEAS).size());
        assertEquals(1, personRepository.findByUserIdAndStatus(userId, PersonStatus.NONE).size());
    }

    @Test
    void shouldFindUpcomingBirthdays() {
        // WHEN
        List<Person> result = personRepository.findUpcomingBirthdays(userId, 3, 15);

        // THEN
        assertEquals(2, result.size()); // Alice, Charlie
        assertTrue(result.stream().allMatch(p -> p.getBirthday().getMonthValue() == 3));
        assertTrue(result.stream().allMatch(p -> p.getBirthday().getDayOfMonth() == 15));
    }

    @Test
    void shouldReturnEmptyListForDateWithoutBirthdays() {
        // WHEN
        List<Person> result = personRepository.findUpcomingBirthdays(userId, 12, 25);

        // THEN
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldIgnorePersonsWithNullBirthday() {
        // WHEN - Dave hat null als Geburtstag
        List<Person> result = personRepository.findUpcomingBirthdays(userId, 3, 15);

        // THEN
        assertEquals(2, result.size());
        assertFalse(result.stream().anyMatch(p -> p.getName().equals("Dave")));
    }

    @Test
    void shouldNotReturnOtherUsersPersonsForBirthday() {
        // WHEN
        List<Person> result = personRepository.findUpcomingBirthdays(userId, 3, 15);

        // THEN
        assertEquals(2, result.size());
        assertFalse(result.stream().anyMatch(p -> p.getUserId().equals(otherUserId)));
    }

    @Test
    void shouldFindBirthdaysInMonth() {
        // WHEN
        List<Person> marchBirthdays = personRepository.findBirthdaysInMonth(userId, 3);

        // THEN
        assertEquals(2, marchBirthdays.size()); // Alice (3.15), Charlie (3.15)
        assertTrue(marchBirthdays.stream().allMatch(p -> p.getBirthday().getMonthValue() == 3));
    }

    @Test
    void shouldReturnSortedByDayOfMonth() {
        // GIVEN
        entityManager.persistAndFlush(createTestPerson("Grace", PersonStatus.PLANNED, LocalDate.of(1995, 3, 1), userId));
        entityManager.persistAndFlush(createTestPerson("Henry", PersonStatus.PLANNED, LocalDate.of(1995, 3, 20), userId));

        // WHEN
        List<Person> result = personRepository.findBirthdaysInMonth(userId, 3);

        // THEN
        assertEquals(4, result.size());
        assertEquals(1, result.get(0).getBirthday().getDayOfMonth()); // Grace
        assertEquals(15, result.get(1).getBirthday().getDayOfMonth()); // Alice oder Charlie
        assertEquals(20, result.get(3).getBirthday().getDayOfMonth()); // Henry
    }

    @Test
    void shouldReturnEmptyListForMonthWithoutBirthdays() {
        // WHEN
        List<Person> result = personRepository.findBirthdaysInMonth(userId, 12);

        // THEN
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldSupportAllMonths() {
        // GIVEN - Personen in verschiedenen Monaten
        for (int month = 1; month <= 12; month++) {
            int day = 10;
            entityManager.persistAndFlush(
                    createTestPerson("Person-M" + month, PersonStatus.PLANNED,
                            LocalDate.of(1995, month, day), userId)
            );
        }

        // WHEN & THEN
        for (int month = 1; month <= 12; month++) {
            List<Person> result = personRepository.findBirthdaysInMonth(userId, month);
            assertFalse(result.isEmpty(), "Monat " + month + " sollte Personen haben");
        }
    }

    @Test
    void shouldIgnoreNullBirthdaysInMonth() {
        // WHEN
        List<Person> result = personRepository.findBirthdaysInMonth(userId, 3);

        // THEN - Dave hat null und sollte nicht dabei sein
        assertEquals(2, result.size());
        assertFalse(result.stream().anyMatch(p -> p.getName().equals("Dave")));
    }

    // ============ countByUserIdAndStatus ============

    @Test
    void shouldCountByUserIdAndStatus() {
        // WHEN
        long plannedCount = personRepository.countByUserIdAndStatus(userId, PersonStatus.PLANNED);
        long completedCount = personRepository.countByUserIdAndStatus(userId, PersonStatus.COMPLETED);

        // THEN
        assertEquals(2L, plannedCount); // Alice, Charlie
        assertEquals(1L, completedCount); // Bob
    }

    @Test
    void shouldReturnZeroForStatusWithoutPersons() {
        // WHEN
        long count = personRepository.countByUserIdAndStatus(userId, PersonStatus.IDEAS);

        // THEN
        assertEquals(0L, count);
    }

    @Test
    void shouldReturnZeroForNonExistentUser() {
        // GIVEN
        UUID nonExistentUserId = UUID.fromString("99999999-0000-0000-0000-000000000099");

        // WHEN
        long count = personRepository.countByUserIdAndStatus(nonExistentUserId, PersonStatus.PLANNED);

        // THEN
        assertEquals(0L, count);
    }

    @Test
    void shouldIsolateCountBetweenUsers() {
        // WHEN
        long userCount = personRepository.countByUserIdAndStatus(userId, PersonStatus.PLANNED);
        long otherUserCount = personRepository.countByUserIdAndStatus(otherUserId, PersonStatus.PLANNED);

        // THEN
        assertEquals(2L, userCount);
        assertEquals(1L, otherUserCount);
    }

    // ============ findByIdAndUserId ============

    @Test
    void shouldFindByIdAndUserId() {
        // WHEN
        Optional<Person> result = personRepository.findByIdAndUserId(alicePerson.getId(), userId);

        // THEN
        assertTrue(result.isPresent());
        assertEquals("Alice", result.get().getName());
    }

    @Test
    void shouldReturnEmptyForOtherUsersPerson() {
        // WHEN
        Optional<Person> result = personRepository.findByIdAndUserId(evePerson.getId(), userId);

        // THEN
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyForNonExistentId() {
        // WHEN
        Optional<Person> result = personRepository.findByIdAndUserId(UUID.randomUUID(), userId);

        // THEN
        assertTrue(result.isEmpty());
    }

    // ============ deleteByIdAndUserId ============

    @Test
    void shouldDeleteByIdAndUserId() {
        // GIVEN
        Long countBefore = (Long) entityManager.getEntityManager()
                .createQuery("SELECT COUNT(p) FROM Person p WHERE p.userId = :userId")
                .setParameter("userId", userId)
                .getSingleResult();

        // WHEN
        personRepository.deleteByIdAndUserId(alicePerson.getId(), userId);
        entityManager.flush();

        // THEN
        Long countAfter = (Long) entityManager.getEntityManager()
                .createQuery("SELECT COUNT(p) FROM Person p WHERE p.userId = :userId")
                .setParameter("userId", userId)
                .getSingleResult();
        assertEquals(countBefore - 1, countAfter);
        assertTrue(personRepository.findById(alicePerson.getId()).isEmpty());
    }

    @Test
    void shouldNotDeleteOtherUsersPerson() {
        // WHEN
        personRepository.deleteByIdAndUserId(evePerson.getId(), userId);
        entityManager.flush();

        // THEN
        assertTrue(personRepository.findById(evePerson.getId()).isPresent());
    }
}
