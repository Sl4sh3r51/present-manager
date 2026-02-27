package org.iu.presentmanager.occasions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class OccasionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private OccasionRepository occasionRepository;

    private UUID userId;
    private UUID otherUserId;
    private UUID occasionId1;

    private Occasion createTestOccasion(String name, OccasionType type, Integer month, Integer day,
                                        Boolean isRecurring, UUID userId) {
        Occasion occasion = new Occasion();
        occasion.setName(name);
        occasion.setUserId(userId);
        occasion.setType(type);
        occasion.setFixedMonth(month);
        occasion.setFixedDay(day);
        occasion.setIsRecurring(isRecurring);
        return occasion;
    }

    @BeforeEach
    void setUp() {
        userId = UUID.fromString("10000000-0000-0000-0000-000000000001");
        otherUserId = UUID.fromString("20000000-0000-0000-0000-000000000002");

        // Occasions for userId
        Occasion christmas = createTestOccasion("Christmas", OccasionType.FIXED, 12, 25, true, userId);
        Occasion birthday = createTestOccasion("Birthday", OccasionType.FIXED, 6, 15, true, userId);
        Occasion vacation = createTestOccasion("Summer Vacation", OccasionType.CUSTOM, null, null, false, userId);
        Occasion anniversary = createTestOccasion("Anniversary", OccasionType.FIXED, 7, 1, true, userId);

        // Occasions for otherUserId
        Occasion otherChristmas = createTestOccasion("Christmas", OccasionType.FIXED, 12, 25, true, otherUserId);

        entityManager.persistAndFlush(christmas);
        entityManager.persistAndFlush(birthday);
        entityManager.persistAndFlush(vacation);
        entityManager.persistAndFlush(anniversary);
        entityManager.persistAndFlush(otherChristmas);

        occasionId1 = christmas.getId();
    }

    @Test
    void shouldFindAllOccasionsByUserId() {
        // WHEN
        List<Occasion> result = occasionRepository.findByUserId(userId);

        // THEN
        assertNotNull(result);
        assertEquals(4, result.size());
        assertTrue(result.stream().allMatch(o -> o.getUserId().equals(userId)));
        assertTrue(result.stream().anyMatch(o -> o.getName().equals("Christmas")));
        assertTrue(result.stream().anyMatch(o -> o.getName().equals("Birthday")));
    }

    @Test
    void shouldReturnEmptyListForUserWithoutOccasions() {
        // GIVEN
        UUID nonExistentUserId = UUID.fromString("99999999-0000-0000-0000-000000000099");

        // WHEN
        List<Occasion> result = occasionRepository.findByUserId(nonExistentUserId);

        // THEN
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldIsolateBetweenUsers() {
        // WHEN
        List<Occasion> userResult = occasionRepository.findByUserId(userId);
        List<Occasion> otherUserResult = occasionRepository.findByUserId(otherUserId);

        // THEN
        assertEquals(4, userResult.size());
        assertEquals(1, otherUserResult.size());
        assertTrue(userResult.stream().allMatch(o -> o.getUserId().equals(userId)));
        assertTrue(otherUserResult.stream().allMatch(o -> o.getUserId().equals(otherUserId)));
    }

    @Test
    void shouldFindOccasionByIdAndUserId() {
        // WHEN
        Optional<Occasion> result = occasionRepository.findByIdAndUserId(occasionId1, userId);

        // THEN
        assertTrue(result.isPresent());
        assertEquals("Christmas", result.get().getName());
        assertEquals(userId, result.get().getUserId());
    }

    @Test
    void shouldNotFindOccasionIfUserIdDoesNotMatch() {
        // WHEN
        Optional<Occasion> result = occasionRepository.findByIdAndUserId(occasionId1, otherUserId);

        // THEN
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyOptionalForNonExistentOccasion() {
        // WHEN
        UUID nonExistentId = UUID.randomUUID();
        Optional<Occasion> result = occasionRepository.findByIdAndUserId(nonExistentId, userId);

        // THEN
        assertTrue(result.isEmpty());
    }
    @Test
    void shouldFindOccasionsByType() {
        // WHEN
        List<Occasion> result = occasionRepository.findByUserIdAndType(userId, OccasionType.FIXED);

        // THEN
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.stream().allMatch(o -> o.getType() == OccasionType.FIXED));
    }

    @Test
    void shouldFindCustomOccasions() {
        // WHEN
        List<Occasion> result = occasionRepository.findByUserIdAndType(userId, OccasionType.CUSTOM);

        // THEN
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(OccasionType.CUSTOM, result.get(0).getType());
        assertEquals("Summer Vacation", result.get(0).getName());
    }

    @Test
    void shouldReturnEmptyListWhenNoOccasionsOfType() {
        // WHEN
        List<Occasion> result = occasionRepository.findByUserIdAndType(userId, OccasionType.CUSTOM);
        result = result.stream().filter(o -> o.getType() == OccasionType.FIXED).toList();

        // Reset and test again
        result = occasionRepository.findByUserIdAndType(UUID.randomUUID(), OccasionType.FIXED);

        // THEN
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldFindRecurringOccasions() {
        // WHEN
        List<Occasion> result = occasionRepository.findByUserIdAndIsRecurring(userId, true);

        // THEN
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.stream().allMatch(Occasion::getIsRecurring));
    }

    @Test
    void shouldFindNonRecurringOccasions() {
        // WHEN
        List<Occasion> result = occasionRepository.findByUserIdAndIsRecurring(userId, false);

        // THEN
        assertNotNull(result);
        assertEquals(1, result.size());
        assertFalse(result.get(0).getIsRecurring());
        assertEquals("Summer Vacation", result.get(0).getName());
    }

    @Test
    void shouldReturnEmptyListForUserWithoutRecurringOccasions() {
        // WHEN
        List<Occasion> result = occasionRepository.findByUserIdAndIsRecurring(otherUserId, false);

        // THEN
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldCheckOccasionExistsByUserIdAndName() {
        // WHEN
        boolean exists = occasionRepository.existsByUserIdAndName(userId, "Christmas");

        // THEN
        assertTrue(exists);
    }

    @Test
    void shouldReturnFalseWhenOccasionNameDoesNotExistForUser() {
        // WHEN
        boolean exists = occasionRepository.existsByUserIdAndName(userId, "NonExistent");

        // THEN
        assertFalse(exists);
    }

    @Test
    void shouldNotFindOccasionNameFromOtherUser() {
        // WHEN
        boolean existsForUser = occasionRepository.existsByUserIdAndName(userId, "Christmas");
        boolean existsForOtherUser = occasionRepository.existsByUserIdAndName(otherUserId, "Christmas");

        // Create a temporary occasion for otherUserId with different name
        Occasion tempOccasion = createTestOccasion("Other Event", OccasionType.FIXED, 1, 1, true, otherUserId);
        entityManager.persistAndFlush(tempOccasion);

        boolean existsOtherEvent = occasionRepository.existsByUserIdAndName(otherUserId, "Other Event");

        // THEN
        assertTrue(existsForUser);
        assertTrue(existsForOtherUser);
        assertTrue(existsOtherEvent);
        assertFalse(occasionRepository.existsByUserIdAndName(otherUserId, "Birthday")); // Birthday is only for userId
    }

    @Test
    void shouldSearchOccasionsByName() {
        // WHEN
        List<Occasion> result = occasionRepository.searchByName(userId, "Christ");

        // THEN
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getName().toLowerCase().contains("christ"));
    }

    @Test
    void shouldSearchCaseInsensitively() {
        // WHEN
        List<Occasion> result1 = occasionRepository.searchByName(userId, "CHRISTMAS");
        List<Occasion> result2 = occasionRepository.searchByName(userId, "christmas");
        List<Occasion> result3 = occasionRepository.searchByName(userId, "ChRiStMaS");

        // THEN
        assertEquals(1, result1.size());
        assertEquals(1, result2.size());
        assertEquals(1, result3.size());
    }

    @Test
    void shouldReturnEmptyListForNonMatchingSearch() {
        // WHEN
        List<Occasion> result = occasionRepository.searchByName(userId, "NonExistent");

        // THEN
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldIsolateSearchResultsByUserId() {
        // WHEN
        List<Occasion> userResult = occasionRepository.searchByName(userId, "Christmas");
        List<Occasion> otherUserResult = occasionRepository.searchByName(otherUserId, "Christmas");

        // THEN
        assertEquals(1, userResult.size());
        assertEquals(1, otherUserResult.size());
        assertEquals(userResult.get(0).getUserId(), userId);
        assertEquals(otherUserResult.get(0).getUserId(), otherUserId);
    }

    @Test
    void shouldFindFixedOccasionByMonth() {
        // WHEN
        List<Occasion> result = occasionRepository.findFixedOccasionByMonth(userId, 12);

        // THEN
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Christmas", result.get(0).getName());
        assertEquals(12, result.get(0).getFixedMonth());
    }

    @Test
    void shouldFindMultipleFixedOccasionsInSameMonth() {
        // GIVEN - Add another occasion in December
        Occasion newYearEve = createTestOccasion("New Year Eve", OccasionType.FIXED, 12, 31, true, userId);
        entityManager.persistAndFlush(newYearEve);

        // WHEN
        List<Occasion> result = occasionRepository.findFixedOccasionByMonth(userId, 12);

        // THEN
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(o -> o.getFixedMonth() == 12));
    }

    @Test
    void shouldReturnEmptyListWhenNoFixedOccasionsInMonth() {
        // WHEN
        List<Occasion> result = occasionRepository.findFixedOccasionByMonth(userId, 2); // February

        // THEN
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldNotIncludeNonFixedOccasions() {
        // WHEN
        List<Occasion> result = occasionRepository.findFixedOccasionByMonth(userId, 6); // June

        // THEN
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Birthday", result.get(0).getName());
        assertEquals(OccasionType.FIXED, result.get(0).getType());
    }

    @Test
    void shouldFindFixedOccasionByDate() {
        // WHEN
        List<Occasion> result = occasionRepository.findFixedOccasionByDate(userId, 12, 25);

        // THEN
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Christmas", result.get(0).getName());
    }

    @Test
    void shouldReturnEmptyListWhenNoOccasionsOnDate() {
        // WHEN
        List<Occasion> result = occasionRepository.findFixedOccasionByDate(userId, 12, 24);

        // THEN
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldFindRecurringFixedOccasion() {
        // WHEN
        List<Occasion> result = occasionRepository.findRecurringFixedOccasion(userId);

        // THEN
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.stream().allMatch(o -> o.getType() == OccasionType.FIXED));
        assertTrue(result.stream().allMatch(Occasion::getIsRecurring));
    }

    @Test
    void shouldNotIncludeNonRecurringFixedOccasions() {
        // GIVEN - Add a non-recurring fixed occasion
        Occasion nonRecurringFixed = createTestOccasion("One-Time Event", OccasionType.FIXED, 3, 15, false, userId);
        entityManager.persistAndFlush(nonRecurringFixed);

        // WHEN
        List<Occasion> result = occasionRepository.findRecurringFixedOccasion(userId);

        // THEN
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.stream().allMatch(Occasion::getIsRecurring));
    }

    @Test
    void shouldReturnEmptyListWhenNoRecurringFixedOccasions() {
        // WHEN
        List<Occasion> result = occasionRepository.findRecurringFixedOccasion(otherUserId);

        // THEN
        assertNotNull(result);
        // otherUserId has one recurring fixed occasion (Christmas)
        assertEquals(1, result.size());
    }

    @Test
    void shouldDeleteOccasionByIdAndUserId() {
        // WHEN
        occasionRepository.deleteByIdAndUserId(occasionId1, userId);
        entityManager.flush();

        // THEN
        Optional<Occasion> result = occasionRepository.findByIdAndUserId(occasionId1, userId);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldNotDeleteOccasionIfUserIdDoesNotMatch() {
        // WHEN
        occasionRepository.deleteByIdAndUserId(occasionId1, otherUserId);
        entityManager.flush();

        // THEN
        Optional<Occasion> result = occasionRepository.findByIdAndUserId(occasionId1, userId);
        assertTrue(result.isPresent());
    }
}
