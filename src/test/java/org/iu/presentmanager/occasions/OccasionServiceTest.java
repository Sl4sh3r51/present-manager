package org.iu.presentmanager.occasions;

import org.iu.presentmanager.exceptions.DuplicateResourceException;
import org.iu.presentmanager.exceptions.ResourceNotFoundException;
import org.iu.presentmanager.gifts.Gift;
import org.iu.presentmanager.gifts.GiftRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OccasionServiceTest {

    @Mock
    private OccasionRepository occasionRepository;

    @Mock
    private GiftRepository giftRepository;

    @InjectMocks
    private OccasionService occasionService;

    private final UUID userId = UUID.randomUUID();
    private final UUID occasionId = UUID.randomUUID();
    private Occasion testOccasion;

    @BeforeEach
    void setUp() {
        testOccasion = createTestOccasion("Christmas", OccasionType.FIXED, 12, 25);
    }

    private Occasion createTestOccasion(String name, OccasionType type, Integer month, Integer day) {
        Occasion occasion = new Occasion();
        occasion.setId(occasionId);
        occasion.setName(name);
        occasion.setUserId(userId);
        occasion.setType(type);
        occasion.setFixedMonth(month);
        occasion.setFixedDay(day);
        occasion.setIsRecurring(true);
        return occasion;
    }

    @Test
    void shouldCreateOccasion() {
        // GIVEN
        Occasion newOccasion = new Occasion();
        newOccasion.setName("Birthday");
        newOccasion.setType(OccasionType.FIXED);
        newOccasion.setFixedMonth(6);
        newOccasion.setFixedDay(15);
        newOccasion.setIsRecurring(true);

        when(occasionRepository.existsByUserIdAndName(userId, "Birthday")).thenReturn(false);
        when(occasionRepository.save(any(Occasion.class))).thenReturn(testOccasion);

        // WHEN
        Occasion result = occasionService.createOccasion(newOccasion, userId);

        // THEN
        assertNotNull(result);
        assertEquals("Christmas", result.getName());
        assertEquals(userId, result.getUserId());
        verify(occasionRepository).existsByUserIdAndName(userId, "Birthday");
        verify(occasionRepository).save(any(Occasion.class));
    }

    @Test
    void shouldThrowDuplicateResourceExceptionWhenNameExists() {
        // GIVEN
        Occasion newOccasion = new Occasion();
        newOccasion.setName("Christmas");
        newOccasion.setType(OccasionType.FIXED);
        newOccasion.setFixedMonth(12);
        newOccasion.setFixedDay(25);

        when(occasionRepository.existsByUserIdAndName(userId, "Christmas")).thenReturn(true);

        // WHEN & THEN
        assertThrows(DuplicateResourceException.class,
                () -> occasionService.createOccasion(newOccasion, userId));
        verify(occasionRepository).existsByUserIdAndName(userId, "Christmas");
        verify(occasionRepository, never()).save(any());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenFixedMonthMissing() {
        // GIVEN
        Occasion newOccasion = new Occasion();
        newOccasion.setName("New Occasion");
        newOccasion.setType(OccasionType.FIXED);
        newOccasion.setFixedMonth(null);
        newOccasion.setFixedDay(25);

        when(occasionRepository.existsByUserIdAndName(userId, "New Occasion")).thenReturn(false);

        // WHEN & THEN
        assertThrows(IllegalArgumentException.class,
                () -> occasionService.createOccasion(newOccasion, userId));
        verify(occasionRepository, never()).save(any());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenFixedDayMissing() {
        // GIVEN
        Occasion newOccasion = new Occasion();
        newOccasion.setName("New Occasion");
        newOccasion.setType(OccasionType.FIXED);
        newOccasion.setFixedMonth(12);
        newOccasion.setFixedDay(null);

        when(occasionRepository.existsByUserIdAndName(userId, "New Occasion")).thenReturn(false);

        // WHEN & THEN
        assertThrows(IllegalArgumentException.class,
                () -> occasionService.createOccasion(newOccasion, userId));
        verify(occasionRepository, never()).save(any());
    }

    @Test
    void shouldCreateCustomOccasionWithoutFixedValues() {
        // GIVEN
        Occasion customOccasion = new Occasion();
        customOccasion.setName("Custom Occasion");
        customOccasion.setType(OccasionType.CUSTOM);
        customOccasion.setIsRecurring(false);

        when(occasionRepository.existsByUserIdAndName(userId, "Custom Occasion")).thenReturn(false);
        when(occasionRepository.save(any(Occasion.class))).thenReturn(customOccasion);

        // WHEN
        Occasion result = occasionService.createOccasion(customOccasion, userId);

        // THEN
        assertNotNull(result);
        assertEquals(OccasionType.CUSTOM, result.getType());
        verify(occasionRepository).save(any(Occasion.class));
    }

    @Test
    void shouldGetAllOccasionsByUser() {
        // GIVEN
        when(occasionRepository.findByUserId(userId))
                .thenReturn(List.of(testOccasion));

        // WHEN
        List<Occasion> result = occasionService.getAllOccasionsByUser(userId);

        // THEN
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Christmas", result.getFirst().getName());
        verify(occasionRepository).findByUserId(userId);
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoOccasions() {
        // GIVEN
        when(occasionRepository.findByUserId(userId))
                .thenReturn(Collections.emptyList());

        // WHEN
        List<Occasion> result = occasionService.getAllOccasionsByUser(userId);

        // THEN
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(occasionRepository).findByUserId(userId);
    }

    @Test
    void shouldGetOccasionById() {
        // GIVEN
        testOccasion.setGifts(Set.of(new Gift()));
        when(occasionRepository.findByIdAndUserId(occasionId, userId))
                .thenReturn(Optional.of(testOccasion));

        // WHEN
        Occasion result = occasionService.getOccasionById(occasionId, userId);

        // THEN
        assertNotNull(result);
        assertEquals("Christmas", result.getName());
        assertEquals(userId, result.getUserId());
        verify(occasionRepository).findByIdAndUserId(occasionId, userId);
    }

    @Test
    void shouldReturnIllegalStateExceptionWhenDeletingOccasionWithGifts(){
        //Given
        when(occasionRepository.findByIdAndUserId(occasionId, userId))
                .thenReturn(Optional.of(testOccasion));
        when(giftRepository.existsByOccasionId(occasionId)).thenReturn(true);

        //When & Then
        assertThrows(IllegalStateException.class, () -> occasionService.deleteOccasion(occasionId, userId));
        verify(occasionRepository).findByIdAndUserId(occasionId, userId);
        verify(giftRepository).existsByOccasionId(occasionId);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenOccasionNotFound() {
        // GIVEN
        when(occasionRepository.findByIdAndUserId(occasionId, userId))
                .thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(ResourceNotFoundException.class,
                () -> occasionService.getOccasionById(occasionId, userId));
        verify(occasionRepository).findByIdAndUserId(occasionId, userId);
    }

    @Test
    void shouldGetOccasionsByType() {
        // GIVEN
        when(occasionRepository.findByUserIdAndType(userId, OccasionType.FIXED))
                .thenReturn(List.of(testOccasion));

        // WHEN
        List<Occasion> result = occasionService.getOccasionsByType(userId, OccasionType.FIXED);

        // THEN
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(OccasionType.FIXED, result.getFirst().getType());
        verify(occasionRepository).findByUserIdAndType(userId, OccasionType.FIXED);
    }

    @Test
    void shouldReturnEmptyListWhenNoOccasionsOfType() {
        // GIVEN
        when(occasionRepository.findByUserIdAndType(userId, OccasionType.CUSTOM))
                .thenReturn(Collections.emptyList());

        // WHEN
        List<Occasion> result = occasionService.getOccasionsByType(userId, OccasionType.CUSTOM);

        // THEN
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(occasionRepository).findByUserIdAndType(userId, OccasionType.CUSTOM);
    }

    @Test
    void shouldGetRecurringOccasions() {
        // GIVEN
        when(occasionRepository.findByUserIdAndIsRecurring(userId, true))
                .thenReturn(List.of(testOccasion));

        // WHEN
        List<Occasion> result = occasionService.getRecurringOccasions(userId, true);

        // THEN
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.getFirst().getIsRecurring());
        verify(occasionRepository).findByUserIdAndIsRecurring(userId, true);
    }

    @Test
    void shouldGetNonRecurringOccasions() {
        // GIVEN
        Occasion nonRecurringOccasion = createTestOccasion("One-Time Event", OccasionType.CUSTOM, null, null);
        nonRecurringOccasion.setIsRecurring(false);

        when(occasionRepository.findByUserIdAndIsRecurring(userId, false))
                .thenReturn(List.of(nonRecurringOccasion));

        // WHEN
        List<Occasion> result = occasionService.getRecurringOccasions(userId, false);

        // THEN
        assertNotNull(result);
        assertEquals(1, result.size());
        assertFalse(result.getFirst().getIsRecurring());
        verify(occasionRepository).findByUserIdAndIsRecurring(userId, false);
    }

    @Test
    void shouldSearchOccasionsByName() {
        // GIVEN
        when(occasionRepository.searchByName(userId, "Christ"))
                .thenReturn(List.of(testOccasion));

        // WHEN
        List<Occasion> result = occasionService.searchOccasionsByName(userId, "Christ");

        // THEN
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.getFirst().getName().contains("Christ"));
        verify(occasionRepository).searchByName(userId, "Christ");
    }

    @Test
    void shouldReturnEmptyListForNonMatchingSearch() {
        // GIVEN
        when(occasionRepository.searchByName(userId, "NonExistent"))
                .thenReturn(Collections.emptyList());

        // WHEN
        List<Occasion> result = occasionService.searchOccasionsByName(userId, "NonExistent");

        // THEN
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(occasionRepository).searchByName(userId, "NonExistent");
    }

    @Test
    void shouldGetFixedOccasionsByMonth() {
        // GIVEN
        when(occasionRepository.findFixedOccasionByMonth(userId, 12))
                .thenReturn(List.of(testOccasion));

        // WHEN
        List<Occasion> result = occasionService.getFixedOccasionsByMonth(userId, 12);

        // THEN
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(12, result.getFirst().getFixedMonth());
        verify(occasionRepository).findFixedOccasionByMonth(userId, 12);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionForInvalidMonth() {
        // GIVEN - month 0 is invalid (must be 1-12)

        // WHEN & THEN
        assertThrows(IllegalArgumentException.class,
                () -> occasionService.getFixedOccasionsByMonth(userId, 0));
        verify(occasionRepository, never()).findFixedOccasionByMonth(eq(userId), anyInt());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionForMonthGreaterThan12() {
        // WHEN & THEN
        assertThrows(IllegalArgumentException.class,
                () -> occasionService.getFixedOccasionsByMonth(userId, 13));
        verify(occasionRepository, never()).findFixedOccasionByMonth(eq(userId), anyInt());
    }

    @Test
    void shouldReturnEmptyListWhenNoFixedOccasionsInMonth() {
        // GIVEN
        when(occasionRepository.findFixedOccasionByMonth(userId, 6))
                .thenReturn(Collections.emptyList());

        // WHEN
        List<Occasion> result = occasionService.getFixedOccasionsByMonth(userId, 6);

        // THEN
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(occasionRepository).findFixedOccasionByMonth(userId, 6);
    }

    @Test
    void shouldGetTodayFixedOccasions() {
        // GIVEN
        LocalDate today = LocalDate.now();
        when(occasionRepository.findFixedOccasionByDate(userId, today.getMonthValue(), today.getDayOfMonth()))
                .thenReturn(List.of(testOccasion));

        // WHEN
        List<Occasion> result = occasionService.getTodayFixedOccasions(userId);

        // THEN
        assertNotNull(result);
        verify(occasionRepository).findFixedOccasionByDate(eq(userId), eq(today.getMonthValue()), eq(today.getDayOfMonth()));
    }

    @Test
    void shouldReturnEmptyListWhenNoTodayOccasions() {
        // GIVEN
        LocalDate today = LocalDate.now();
        when(occasionRepository.findFixedOccasionByDate(userId, today.getMonthValue(), today.getDayOfMonth()))
                .thenReturn(Collections.emptyList());

        // WHEN
        List<Occasion> result = occasionService.getTodayFixedOccasions(userId);

        // THEN
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldGetRecurringFixedOccasions() {
        // GIVEN
        when(occasionRepository.findRecurringFixedOccasion(userId))
                .thenReturn(List.of(testOccasion));

        // WHEN
        List<Occasion> result = occasionService.getRecurringFixedOccasions(userId);

        // THEN
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(OccasionType.FIXED, result.getFirst().getType());
        assertTrue(result.getFirst().getIsRecurring());
        verify(occasionRepository).findRecurringFixedOccasion(userId);
    }

    @Test
    void shouldReturnEmptyListWhenNoRecurringFixedOccasions() {
        // GIVEN
        when(occasionRepository.findRecurringFixedOccasion(userId))
                .thenReturn(Collections.emptyList());

        // WHEN
        List<Occasion> result = occasionService.getRecurringFixedOccasions(userId);

        // THEN
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(occasionRepository).findRecurringFixedOccasion(userId);
    }

    @Test
    void shouldUpdateOccasion() {
        // GIVEN
        Occasion updatedData = new Occasion();
        updatedData.setName("New Year");
        updatedData.setType(OccasionType.FIXED);
        updatedData.setFixedMonth(1);
        updatedData.setFixedDay(1);
        updatedData.setIsRecurring(true);

        when(occasionRepository.findByIdAndUserId(occasionId, userId))
                .thenReturn(Optional.of(testOccasion));
        when(occasionRepository.existsByUserIdAndName(userId, "New Year"))
                .thenReturn(false);
        when(occasionRepository.save(any(Occasion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        Occasion result = occasionService.updateOccasion(occasionId, userId, updatedData);

        // THEN
        assertNotNull(result);
        verify(occasionRepository).findByIdAndUserId(occasionId, userId);
        verify(occasionRepository).existsByUserIdAndName(userId, "New Year");
        verify(occasionRepository).save(any(Occasion.class));
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenUpdatingNonExistent() {
        // GIVEN
        Occasion updatedData = new Occasion();
        updatedData.setName("Updated");

        when(occasionRepository.findByIdAndUserId(occasionId, userId))
                .thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(ResourceNotFoundException.class,
                () -> occasionService.updateOccasion(occasionId, userId, updatedData));
        verify(occasionRepository, never()).save(any());
    }

    @Test
    void shouldThrowDuplicateResourceExceptionWhenUpdatingWithExistingName() {
        // GIVEN
        Occasion updatedData = new Occasion();
        updatedData.setName("Different Name");
        updatedData.setType(OccasionType.FIXED);
        updatedData.setFixedMonth(6);
        updatedData.setFixedDay(15);

        when(occasionRepository.findByIdAndUserId(occasionId, userId))
                .thenReturn(Optional.of(testOccasion));
        when(occasionRepository.existsByUserIdAndName(userId, "Different Name"))
                .thenReturn(true);

        // WHEN & THEN
        assertThrows(DuplicateResourceException.class,
                () -> occasionService.updateOccasion(occasionId, userId, updatedData));
        verify(occasionRepository, never()).save(any());
    }

    @Test
    void shouldAllowUpdatingOccasionWithSameName() {
        // GIVEN
        Occasion updatedData = new Occasion();
        updatedData.setName("Christmas");
        updatedData.setType(OccasionType.FIXED);
        updatedData.setFixedMonth(12);
        updatedData.setFixedDay(26);
        updatedData.setIsRecurring(false);

        when(occasionRepository.findByIdAndUserId(occasionId, userId))
                .thenReturn(Optional.of(testOccasion));
        when(occasionRepository.save(any(Occasion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        Occasion result = occasionService.updateOccasion(occasionId, userId, updatedData);

        // THEN
        assertNotNull(result);
        verify(occasionRepository).save(any(Occasion.class));
    }

    @Test
    void shouldDeleteOccasion() {
        // GIVEN

        when(occasionRepository.findByIdAndUserId(occasionId, userId))
                .thenReturn(Optional.of(testOccasion));
        when(giftRepository.existsByOccasionId(occasionId)).thenReturn(false);
        doNothing().when(occasionRepository).deleteByIdAndUserId(occasionId, userId);

        // WHEN
        occasionService.deleteOccasion(occasionId, userId);

        // THEN
        verify(occasionRepository).findByIdAndUserId(occasionId, userId);
        verify(giftRepository).existsByOccasionId(occasionId);
        verify(occasionRepository).deleteByIdAndUserId(occasionId, userId);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenDeletingNonExistent() {
        // GIVEN
        when(occasionRepository.findByIdAndUserId(occasionId, userId))
                .thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(ResourceNotFoundException.class,
                () -> occasionService.deleteOccasion(occasionId, userId));
        verify(occasionRepository, never()).deleteByIdAndUserId(any(), any());
    }

    @Test
    void shouldCountOccasionsByUser() {
        // GIVEN
        when(occasionRepository.findByUserId(userId))
                .thenReturn(List.of(testOccasion));

        // WHEN
        long count = occasionService.countOccasionsByUser(userId);

        // THEN
        assertEquals(1L, count);
        verify(occasionRepository).findByUserId(userId);
    }

    @Test
    void shouldCountOccasionsByType() {
        // GIVEN
        when(occasionRepository.findByUserIdAndType(userId, OccasionType.FIXED))
                .thenReturn(List.of(testOccasion));

        // WHEN
        long count = occasionService.countOccasionsByType(userId, OccasionType.FIXED);

        // THEN
        assertEquals(1L, count);
        verify(occasionRepository).findByUserIdAndType(userId, OccasionType.FIXED);
    }

    @Test
    void shouldCheckIfOccasionExistsByName() {
        // GIVEN
        when(occasionRepository.existsByUserIdAndName(userId, "Christmas"))
                .thenReturn(true);

        // WHEN
        boolean exists = occasionService.existsByName(userId, "Christmas");

        // THEN
        assertTrue(exists);
        verify(occasionRepository).existsByUserIdAndName(userId, "Christmas");
    }

    @Test
    void shouldReturnFalseWhenOccasionNameDoesNotExist() {
        // GIVEN
        when(occasionRepository.existsByUserIdAndName(userId, "NonExistent"))
                .thenReturn(false);

        // WHEN
        boolean exists = occasionService.existsByName(userId, "NonExistent");

        // THEN
        assertFalse(exists);
        verify(occasionRepository).existsByUserIdAndName(userId, "NonExistent");
    }
}
