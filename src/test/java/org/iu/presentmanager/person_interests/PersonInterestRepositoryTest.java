package org.iu.presentmanager.person_interests;

import org.iu.presentmanager.interests.Interest;
import org.iu.presentmanager.persons.Person;
import org.iu.presentmanager.persons.PersonStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class PersonInterestRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PersonInterestRepository personInterestRepository;

    private UUID userId;
    private UUID otherUserId;
    Person person1 = new Person();
    Person person2 = new Person();
    Person otherPerson = new Person();
    Interest interest1 = new Interest();
    Interest interest2 = new Interest();

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        otherUserId = UUID.randomUUID();

        // Create and persist test data using repositories
        person1.setName("Alice");
        person1.setUserId(userId);
        person1.setStatus(PersonStatus.NONE);
        person1.setBirthday(LocalDate.of(1990, 3, 15));
        entityManager.persistAndFlush(person1);


        person2.setName("Bob");
        person2.setUserId(userId);
        person2.setStatus(PersonStatus.NONE);
        person2.setBirthday(LocalDate.of(1985, 6, 20));
        entityManager.persistAndFlush(person2);


        otherPerson.setName("Charlie");
        otherPerson.setUserId(otherUserId);
        otherPerson.setStatus(PersonStatus.NONE);
        otherPerson.setBirthday(LocalDate.of(1995, 1, 10));
        entityManager.persistAndFlush(otherPerson);


        interest1.setName("Reading");
        entityManager.persistAndFlush(interest1);


        interest2.setName("Gaming");
        entityManager.persistAndFlush(interest2);

        // Create person-interest relationships
        createAndPersistPersonInterest(person1, interest1);
        createAndPersistPersonInterest(person1, interest2);
        createAndPersistPersonInterest(person2, interest1);
        createAndPersistPersonInterest(otherPerson, interest1);
    }

    private void createAndPersistPersonInterest(Person person, Interest interest) {
        PersonInterest pi = new PersonInterest();
        pi.setId(new PersonInterestId(person.getId(), interest.getId()));
        pi.setPerson(person);
        pi.setInterest(interest);
        entityManager.persistAndFlush(pi);
    }

    @Test
    void shouldFindByPersonId() {
        // WHEN
        List<PersonInterest> result = personInterestRepository.findByPersonId(person1.getId());

        // THEN
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(pi -> pi.getId().getPersonId().equals(person1.getId())));
    }

    @Test
    void shouldReturnEmptyListWhenPersonHasNoInterests() {
        // GIVEN - Create a person without interests
        Person person = new Person();
        person.setName("Dave");
        person.setUserId(userId);
        person.setStatus(PersonStatus.NONE);
        person.setBirthday(LocalDate.of(1992, 5, 25));
        entityManager.persistAndFlush(person);

        // WHEN
        List<PersonInterest> result = personInterestRepository.findByPersonId(person.getId());

        // THEN
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldFindByInterestId() {
        // WHEN
        List<PersonInterest> result = personInterestRepository.findByInterestId(interest1.getId());

        // THEN
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.stream().allMatch(pi -> pi.getId().getInterestId().equals(interest1.getId())));
    }

    @Test
    void shouldReturnEmptyListWhenInterestHasNoPersons() {
        // GIVEN - Create an interest without persons
        Interest interest = new Interest();
        interest.setName("Sports");
        entityManager.persistAndFlush(interest);

        // WHEN
        List<PersonInterest> result = personInterestRepository.findByInterestId(interest.getId());

        // THEN
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldCheckExistsByPersonIdAndInterestId() {
        // WHEN
        boolean exists = personInterestRepository.existsByPersonIdAndInterestId(person1.getId(), interest1.getId());

        // THEN
        assertTrue(exists);
    }

    @Test
    void shouldReturnFalseWhenRelationshipDoesntExist() {
        // WHEN
        boolean exists = personInterestRepository.existsByPersonIdAndInterestId(person2.getId(), interest2.getId());

        // THEN
        assertFalse(exists);
    }

    @Test
    void shouldDeleteByPersonIdAndInterestId() {
        // GIVEN
        assertTrue(personInterestRepository.existsByPersonIdAndInterestId(person1.getId(), interest1.getId()));

        // WHEN
        personInterestRepository.deleteByPersonIdAndInterestId(person1.getId(), interest1.getId());

        // THEN
        assertFalse(personInterestRepository.existsByPersonIdAndInterestId(person1.getId(), interest1.getId()));
    }

    @Test
    void shouldNotAffectOtherRelationshipsWhenDeleting() {
        // GIVEN
        assertTrue(personInterestRepository.existsByPersonIdAndInterestId(person1.getId(), interest1.getId()));
        assertTrue(personInterestRepository.existsByPersonIdAndInterestId(person1.getId(), interest2.getId()));

        // WHEN
        personInterestRepository.deleteByPersonIdAndInterestId(person1.getId(), interest1.getId());

        // THEN
        assertFalse(personInterestRepository.existsByPersonIdAndInterestId(person1.getId(), interest1.getId()));
        assertTrue(personInterestRepository.existsByPersonIdAndInterestId(person1.getId(), interest2.getId()));
    }

    @Test
    void shouldFindByUserIdAndPersonId() {
        // WHEN
        List<PersonInterest> result = personInterestRepository.findAllByUserId(userId);

        // THEN
        assertNotNull(result);
        assertTrue(result.size() >= 2);
        assertTrue(result.stream().allMatch(pi -> pi.getPerson().getUserId().equals(userId)));
    }

    @Test
    void shouldIsolateResultsByUserId() {
        // WHEN
        List<PersonInterest> userResult = personInterestRepository.findAllByUserId(userId);
        List<PersonInterest> otherUserResult = personInterestRepository.findAllByUserId(otherUserId);

        // THEN
        assertTrue(userResult.size() > otherUserResult.size());
        assertTrue(userResult.stream().allMatch(pi -> pi.getPerson().getUserId().equals(userId)));
        assertTrue(otherUserResult.stream().allMatch(pi -> pi.getPerson().getUserId().equals(otherUserId)));
    }

    @Test
    void shouldHandleEmbeddedId() {
        // GIVEN
        PersonInterestId id = new PersonInterestId(person1.getId(), interest1.getId());

        // WHEN
        PersonInterest result = personInterestRepository.findById(id).orElse(null);

        // THEN
        assertNotNull(result);
        assertEquals(person1.getId(), result.getId().getPersonId());
        assertEquals(interest1.getId(), result.getId().getInterestId());
    }

    @Test
    void shouldReturnEmptyForNonExistentCompositeKey() {
        // GIVEN
        PersonInterestId nonExistentId = new PersonInterestId(person2.getId(), interest2.getId());

        // WHEN
        var result = personInterestRepository.findById(nonExistentId);

        // THEN
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldMaintainReferentialIntegrity() {
        // WHEN
        List<PersonInterest> results = personInterestRepository.findByPersonId(person1.getId());

        // THEN
        assertNotNull(results);
        results.forEach(pi -> {
            assertNotNull(pi.getPerson());
            assertNotNull(pi.getInterest());
            assertEquals(person1.getId(), pi.getPerson().getId());
            assertTrue(pi.getInterest().getId().equals(interest1.getId()) ||
                      pi.getInterest().getId().equals(interest2.getId()));
        });
    }
}
