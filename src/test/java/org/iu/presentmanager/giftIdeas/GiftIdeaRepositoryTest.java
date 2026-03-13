package org.iu.presentmanager.giftIdeas;

import org.iu.presentmanager.occasions.Occasion;
import org.iu.presentmanager.occasions.OccasionType;
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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class GiftIdeaRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private GiftIdeaRepository giftIdeaRepository;

    private UUID userId;
    private UUID otherUserId;
    private UUID personId;
    private UUID personId2;
    private UUID occasionId;
    private UUID occasionId2;
    private UUID otherOccasionId;
    private GiftIdea testGiftIdea1;

    private Person createTestPerson(String name, UUID userId) {
        Person person = new Person();
        person.setName(name);
        person.setUserId(userId);
        person.setStatus(PersonStatus.PLANNED);
        person.setBirthday(LocalDate.of(1990, 1, 1));
        return person;
    }

    private Occasion createTestOccasion(String name, UUID userId) {
        Occasion occasion = new Occasion();
        occasion.setName(name);
        occasion.setUserId(userId);
        occasion.setType(OccasionType.CUSTOM);
        occasion.setFixedMonth(1);
        occasion.setFixedDay(1);
        occasion.setIsRecurring(true);
        return occasion;
    }

    private GiftIdea createTestGiftIdea(UUID personId, UUID userId, String title, UUID occasionId ,GiftSource source) {
        GiftIdea giftIdea = new GiftIdea();
        giftIdea.setOccasionId(occasionId);
        giftIdea.setPersonId(personId);
        giftIdea.setUserId(userId);
        giftIdea.setTitle(title);
        giftIdea.setSource(source);
        return giftIdea;
    }

    @BeforeEach
    void setUp() {
        userId = UUID.fromString("10000000-0000-0000-0000-000000000001");
        otherUserId = UUID.fromString("20000000-0000-0000-0000-000000000002");

        // STEP 1: Persiste Persons zuerst
        Person person1 = createTestPerson("Alice", userId);
        Person person2 = createTestPerson("Bob", userId);
        Person person3 = createTestPerson("Charlie", otherUserId);

        entityManager.persistAndFlush(person1);
        entityManager.persistAndFlush(person2);
        entityManager.persistAndFlush(person3);

        Occasion occasion1 = createTestOccasion("TestOccasion", userId);
        Occasion occasion2 = createTestOccasion("TestOccasion2", userId);
        Occasion occasion3 = createTestOccasion("TestOccasion3", otherUserId);

        entityManager.persistAndFlush(occasion1);
        entityManager.persistAndFlush(occasion2);
        entityManager.persistAndFlush(occasion3);

        // STEP 2: Hole generierte Person-IDs
        personId = person1.getId();
        personId2 = person2.getId();
        occasionId = occasion1.getId();
        occasionId2 = occasion2.getId();
        otherOccasionId = occasion3.getId();

        // STEP 3: Erstelle und persiste GiftIdeas mit den echten Person-IDs
        testGiftIdea1 = createTestGiftIdea(personId, userId, "Book Idea", occasionId,GiftSource.MANUAL);
        GiftIdea giftIdea2 = createTestGiftIdea(personId, userId, "Watch Idea", occasionId,GiftSource.AI);
        GiftIdea giftIdea3 = createTestGiftIdea(personId2, userId, "Perfume Idea", occasionId2,GiftSource.SHARED);
        GiftIdea giftIdea4 = createTestGiftIdea(personId, otherUserId, "Camera Idea", otherOccasionId,GiftSource.MANUAL);

        entityManager.persistAndFlush(testGiftIdea1);
        entityManager.persistAndFlush(giftIdea2);
        entityManager.persistAndFlush(giftIdea3);
        entityManager.persistAndFlush(giftIdea4);
    }

    @Test
    void shouldFindAllGiftIdeasByUserId() {
        // WHEN
        List<GiftIdea> result = giftIdeaRepository.findByUserId(userId);

        // THEN
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.stream().allMatch(gi -> gi.getUserId().equals(userId)));
    }

    @Test
    void shouldReturnEmptyListForUserWithoutGiftIdeas() {
        // GIVEN
        UUID nonExistentUserId = UUID.fromString("99999999-0000-0000-0000-000000000099");

        // WHEN
        List<GiftIdea> result = giftIdeaRepository.findByUserId(nonExistentUserId);

        // THEN
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldIsolateBetweenUsers() {
        // WHEN
        List<GiftIdea> userResult = giftIdeaRepository.findByUserId(userId);
        List<GiftIdea> otherUserResult = giftIdeaRepository.findByUserId(otherUserId);

        // THEN
        assertEquals(3, userResult.size());
        assertEquals(1, otherUserResult.size());
    }

    @Test
    void shouldFindGiftIdeaByIdAndUserId() {
        // WHEN
        Optional<GiftIdea> result = giftIdeaRepository.findByIdAndUserId(testGiftIdea1.getId(), userId);

        // THEN
        assertTrue(result.isPresent());
        assertEquals("Book Idea", result.get().getTitle());
    }

    @Test
    void shouldNotFindGiftIdeaWithWrongUserId() {
        // WHEN
        Optional<GiftIdea> result = giftIdeaRepository.findByIdAndUserId(testGiftIdea1.getId(), otherUserId);

        // THEN
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldFindGiftIdeasByPersonId() {
        // WHEN
        List<GiftIdea> result = giftIdeaRepository.findByUserIdAndPersonId(userId, personId);

        // THEN
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(gi -> gi.getPersonId().equals(personId)));
    }

    @Test
    void shouldReturnEmptyListForNonExistentPerson() {
        // WHEN
        List<GiftIdea> result = giftIdeaRepository.findByUserIdAndPersonId(userId, UUID.randomUUID());

        // THEN
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldFindGiftIdeasBySource() {
        // WHEN
        List<GiftIdea> manualGiftIdeas = giftIdeaRepository.findByUserIdAndSource(userId, GiftSource.MANUAL);
        List<GiftIdea> aiGiftIdeas = giftIdeaRepository.findByUserIdAndSource(userId, GiftSource.AI);
        List<GiftIdea> sharedGiftIdeas = giftIdeaRepository.findByUserIdAndSource(userId, GiftSource.SHARED);

        // THEN
        assertEquals(1, manualGiftIdeas.size());
        assertEquals(1, aiGiftIdeas.size());
        assertEquals(1, sharedGiftIdeas.size());
    }

    @Test
    void shouldSearchGiftIdeasByTitle() {
        // WHEN
        List<GiftIdea> result = giftIdeaRepository.searchByTitleOrDescription(userId, "Book");

        // THEN
        assertEquals(1, result.size());
        assertEquals("Book Idea", result.getFirst().getTitle());
    }

    @Test
    void shouldSearchGiftsIgnoreCase() {
        // WHEN
        List<GiftIdea> result = giftIdeaRepository.searchByTitleOrDescription(userId, "watch");

        // THEN
        assertEquals(1, result.size());
        assertEquals("Watch Idea", result.getFirst().getTitle());
    }


    @Test
    void shouldReturnEmptyListForNonMatchingSearch() {
        // WHEN
        List<GiftIdea> result = giftIdeaRepository.searchByTitleOrDescription(userId, "nonexistent");

        // THEN
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldDeleteGiftIdeaByUserIdAndId() {
        // GIVEN
        UUID giftIdeaIdToDelete = testGiftIdea1.getId();

        // WHEN
        giftIdeaRepository.deleteByIdAndUserId(giftIdeaIdToDelete, userId);

        // THEN
        Optional<GiftIdea> result = giftIdeaRepository.findByIdAndUserId(giftIdeaIdToDelete, userId);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldNotDeleteGiftIdeaWithWrongUserId() {
        // GIVEN
        UUID giftIdeaIdToDelete = testGiftIdea1.getId();

        // WHEN
        giftIdeaRepository.deleteByIdAndUserId(giftIdeaIdToDelete, otherUserId);

        // THEN
        Optional<GiftIdea> result = giftIdeaRepository.findByIdAndUserId(giftIdeaIdToDelete, userId);
        assertTrue(result.isPresent());
    }
}







