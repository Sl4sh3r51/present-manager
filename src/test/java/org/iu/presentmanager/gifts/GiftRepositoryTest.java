package org.iu.presentmanager.gifts;

import org.iu.presentmanager.giftideas.GiftIdea;
import org.iu.presentmanager.giftideas.GiftSource;
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
class GiftRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private GiftRepository giftRepository;

    private UUID userId;
    private UUID otherUserId;
    private UUID personId;
    private UUID personId2;
    private Gift testGift1;
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

    private Gift createTestGift(UUID personId, UUID userId, String title, UUID occasionId,GiftStatus status) {
        Gift gift = new Gift();
        gift.setPersonId(personId);
        gift.setOccasionId(occasionId);
        gift.setUserId(userId);
        gift.setTitle(title);
        gift.setStatus(status);
        return gift;
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

    private GiftIdea createTestGiftIdea(UUID personId, UUID userId, String title, UUID occasionId , GiftSource source) {
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

        // STEP 1: Erstelle Persons OHNE setId() und persiste sie
        // Die IDs werden von Hibernate automatisch generiert
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

        // STEP 2: Hole die generierten IDs nach dem Persist
        personId = person1.getId();
        personId2 = person2.getId();
        occasionId = occasion1.getId();
        occasionId2 = occasion2.getId();
        otherOccasionId = occasion3.getId();

        // STEP 3: Erstelle Gifts mit den echten IDs und persiste sie
        testGift1 = createTestGift(personId, userId, "Book", occasionId,GiftStatus.PLANNED);
        Gift gift2 = createTestGift(personId, userId, "Watch", occasionId,GiftStatus.BOUGHT);
        Gift gift3 = createTestGift(personId2, userId, "Perfume", occasionId2,GiftStatus.GIFTED);
        gift3.setPurchaseDate(LocalDate.now().minusDays(5));
        gift3.setGivenDate(LocalDate.now());

        Gift gift4 = createTestGift(personId, otherUserId, "Camera", otherOccasionId,GiftStatus.PLANNED);

        entityManager.persistAndFlush(testGift1);
        entityManager.persistAndFlush(gift2);
        entityManager.persistAndFlush(gift3);
        entityManager.persistAndFlush(gift4);
    }

    @Test
    void shouldFindAllGiftsByUserId() {
        // WHEN
        List<Gift> result = giftRepository.findByUserId(userId);

        // THEN
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.stream().allMatch(g -> g.getUserId().equals(userId)));
        assertTrue(result.stream().anyMatch(g -> g.getTitle().equals("Book")));
        assertTrue(result.stream().anyMatch(g -> g.getTitle().equals("Watch")));
        assertTrue(result.stream().anyMatch(g -> g.getTitle().equals("Perfume")));
    }

    @Test
    void shouldReturnEmptyListForUserWithoutGifts() {
        // GIVEN
        UUID nonExistentUserId = UUID.fromString("99999999-0000-0000-0000-000000000099");

        // WHEN
        List<Gift> result = giftRepository.findByUserId(nonExistentUserId);

        // THEN
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldIsolateBetweenUsers() {
        // WHEN
        List<Gift> userResult = giftRepository.findByUserId(userId);
        List<Gift> otherUserResult = giftRepository.findByUserId(otherUserId);

        // THEN
        assertEquals(3, userResult.size());
        assertEquals(1, otherUserResult.size());
        assertTrue(userResult.stream().noneMatch(g -> g.getUserId().equals(otherUserId)));
        assertTrue(otherUserResult.stream().noneMatch(g -> g.getUserId().equals(userId)));
    }

    @Test
    void shouldFindGiftByIdAndUserId() {
        // WHEN
        Optional<Gift> result = giftRepository.findByIdAndUserId(testGift1.getId(), userId);

        // THEN
        assertTrue(result.isPresent());
        assertEquals("Book", result.get().getTitle());
        assertEquals(userId, result.get().getUserId());
    }

    @Test
    void shouldNotFindGiftWithWrongUserId() {
        // WHEN
        Optional<Gift> result = giftRepository.findByIdAndUserId(testGift1.getId(), otherUserId);

        // THEN
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldFindGiftsByPersonId() {
        // WHEN
        List<Gift> result = giftRepository.findByUserIdAndPersonId(userId, personId);

        // THEN
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(g -> g.getPersonId().equals(personId)));
    }

    @Test
    void shouldReturnEmptyListForNonExistentPerson() {
        // WHEN
        List<Gift> result = giftRepository.findByUserIdAndPersonId(userId, UUID.randomUUID());

        // THEN
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldFindGiftsByStatus() {
        // WHEN
        List<Gift> plannedGifts = giftRepository.findByUserIdAndStatus(userId, GiftStatus.PLANNED);
        List<Gift> boughtGifts = giftRepository.findByUserIdAndStatus(userId, GiftStatus.BOUGHT);
        List<Gift> giftedGifts = giftRepository.findByUserIdAndStatus(userId, GiftStatus.GIFTED);

        // THEN
        assertEquals(1, plannedGifts.size());
        assertEquals(1, boughtGifts.size());
        assertEquals(1, giftedGifts.size());
        assertTrue(plannedGifts.stream().allMatch(g -> g.getStatus().equals(GiftStatus.PLANNED)));
        assertTrue(boughtGifts.stream().allMatch(g -> g.getStatus().equals(GiftStatus.BOUGHT)));
        assertTrue(giftedGifts.stream().allMatch(g -> g.getStatus().equals(GiftStatus.GIFTED)));
    }

    @Test
    void shouldFindNotPurchasedGifts() {
        // WHEN
        List<Gift> result = giftRepository.findNotPurchased(userId);

        // THEN
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(g -> g.getPurchaseDate() == null));
    }

    @Test
    void shouldFindNotGivenGifts() {
        // WHEN
        List<Gift> result = giftRepository.findNotGiven(userId);

        // THEN
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(g -> g.getGivenDate() == null));
    }

    @Test
    void shouldFindRecentGifts() {
        // WHEN
        List<Gift> result = giftRepository.findRecentGifts(userId);

        // THEN
        assertEquals(3, result.size());
    }

    @Test
    void shouldSearchGiftsByTitle() {
        // WHEN
        List<Gift> result = giftRepository.searchByTitleOrDescription(userId, "Book");

        // THEN
        assertEquals(1, result.size());
        assertEquals("Book", result.getFirst().getTitle());
    }

    @Test
    void shouldSearchGiftsIgnoreCase() {
        // WHEN
        List<Gift> result = giftRepository.searchByTitleOrDescription(userId, "watch");

        // THEN
        assertEquals(1, result.size());
        assertEquals("Watch", result.getFirst().getTitle());
    }

    @Test
    void shouldReturnEmptyListForNonMatchingSearch() {
        // WHEN
        List<Gift> result = giftRepository.searchByTitleOrDescription(userId, "nonexistent");

        // THEN
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldCheckIfGiftIdeaExists() {
        // GIVEN
        testGiftIdea1 = createTestGiftIdea(personId, userId, "Book Idea", occasionId,GiftSource.MANUAL);
        entityManager.persistAndFlush(testGiftIdea1);
        testGift1.setGiftIdeaId(testGiftIdea1.getId());
        entityManager.merge(testGift1);
        entityManager.persist(testGift1);

        // WHEN
        boolean exists = giftRepository.existsByUserIdAndGiftIdeaId(userId, testGiftIdea1.getId());
        boolean notExists = giftRepository.existsByUserIdAndGiftIdeaId(userId, UUID.randomUUID());

        // THEN
        assertTrue(exists);
        assertFalse(notExists);
    }

    @Test
    void shouldDeleteGiftByUserIdAndId() {
        // GIVEN
        UUID giftIdToDelete = testGift1.getId();

        // WHEN
        giftRepository.deleteByUserIdAndId(userId, giftIdToDelete);

        // THEN
        Optional<Gift> result = giftRepository.findByIdAndUserId(giftIdToDelete, userId);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldNotDeleteGiftWithWrongUserId() {
        // GIVEN
        UUID giftIdToDelete = testGift1.getId();
        // WHEN
        giftRepository.deleteByUserIdAndId(otherUserId, giftIdToDelete);

        // THEN
        Optional<Gift> result = giftRepository.findByIdAndUserId(giftIdToDelete, userId);
        assertTrue(result.isPresent());
    }
}







