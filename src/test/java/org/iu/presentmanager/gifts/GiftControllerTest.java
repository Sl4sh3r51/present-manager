package org.iu.presentmanager.gifts;

import org.iu.presentmanager.giftIdeas.GiftIdea;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GiftControllerTest {

    @Mock
    private GiftService giftService;

    @InjectMocks
    private GiftController giftController;

    private UUID userId;
    private UUID personId;
    private UUID occasionId;
    private UUID giftId;
    private UUID giftIdeaId;
    private Gift testGift;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        personId = UUID.randomUUID();
        occasionId = UUID.randomUUID();
        giftId = UUID.randomUUID();
        giftIdeaId = UUID.randomUUID();

        testGift = new Gift();
        testGift.setId(giftId);
        testGift.setUserId(userId);
        testGift.setPersonId(personId);
        testGift.setOccasionId(occasionId);
        testGift.setTitle("Test Gift");
        testGift.setStatus(GiftStatus.PLANNED);
    }

    @Test
    void shouldCreateGiftAndReturnCreatedStatus() {
        // GIVEN
        Gift newGift = new Gift();
        newGift.setPersonId(personId);
        newGift.setOccasionId(occasionId);
        newGift.setTitle("New Gift");

        when(giftService.createGift(any(Gift.class), eq(userId))).thenReturn(testGift);

        // WHEN
        ResponseEntity<Gift> response = giftController.createGift(newGift, userId);

        // THEN
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(giftId, response.getBody().getId());
        verify(giftService, times(1)).createGift(any(Gift.class), eq(userId));
    }

    @Test
    void shouldReturnCreatedGiftObject() {
        // GIVEN
        Gift newGift = new Gift();
        newGift.setTitle("Test Gift");
        when(giftService.createGift(any(Gift.class), eq(userId))).thenReturn(testGift);

        // WHEN
        ResponseEntity<Gift> response = giftController.createGift(newGift, userId);

        // THEN
        assertEquals(testGift, response.getBody());
    }

    @Test
    void shouldCreateGiftFromGiftIdeaAndReturnCreatedStatus() {
        // GIVEN
        GiftIdea giftIdea = new GiftIdea();
        giftIdea.setId(giftIdeaId);
        giftIdea.setPersonId(personId);
        giftIdea.setOccasionId(occasionId);
        giftIdea.setTitle("Test Gift Idea");

        when(giftService.createGiftFromGiftIdea(any(GiftIdea.class), eq(userId)))
                .thenReturn(testGift);

        // WHEN
        ResponseEntity<Gift> response = giftController.createGiftFromGiftIdea(giftIdea, userId);

        // THEN
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(giftId, response.getBody().getId());
        verify(giftService, times(1)).createGiftFromGiftIdea(any(GiftIdea.class), eq(userId));
    }

    @Test
    void shouldGetAllGiftsWhenNoFilterParametersProvided() {
        // GIVEN
        List<Gift> gifts = List.of(testGift);
        when(giftService.getAllGiftsByUser(userId)).thenReturn(gifts);

        // WHEN
        ResponseEntity<List<Gift>> response = giftController.getAllGifts(userId, null, null, null, null);

        // THEN
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(giftId, response.getBody().getFirst().getId());
        verify(giftService, times(1)).getAllGiftsByUser(userId);
    }

    @Test
    void shouldGetGiftsByPersonWhenPersonIdProvided() {
        // GIVEN
        List<Gift> gifts = List.of(testGift);
        when(giftService.getGiftsByPerson(userId, personId)).thenReturn(gifts);

        // WHEN
        ResponseEntity<List<Gift>> response = giftController.getAllGifts(userId, personId, null, null, null);

        // THEN
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(giftService, times(1)).getGiftsByPerson(userId, personId);
        verify(giftService, never()).getAllGiftsByUser(any());
    }

    @Test
    void shouldGetGiftsByOccasionWhenOccasionIdProvided() {
        // GIVEN
        List<Gift> gifts = List.of(testGift);
        when(giftService.getGiftsByOccasion(userId, occasionId)).thenReturn(gifts);

        // WHEN
        ResponseEntity<List<Gift>> response = giftController.getAllGifts(userId, null, occasionId, null, null);

        // THEN
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(giftService, times(1)).getGiftsByOccasion(userId, occasionId);
    }

    @Test
    void shouldGetGiftsByStatusWhenStatusProvided() {
        // GIVEN
        List<Gift> gifts = List.of(testGift);
        when(giftService.getGiftsByStatus(userId, GiftStatus.PLANNED)).thenReturn(gifts);

        // WHEN
        ResponseEntity<List<Gift>> response = giftController.getAllGifts(userId, null, null, GiftStatus.PLANNED, null);

        // THEN
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(giftService, times(1)).getGiftsByStatus(userId, GiftStatus.PLANNED);
    }

    @Test
    void shouldGetGiftsByPersonAndOccasionWhenBothProvided() {
        // GIVEN
        List<Gift> gifts = List.of(testGift);
        when(giftService.getGiftsByPersonAndOccasion(userId, personId, occasionId)).thenReturn(gifts);

        // WHEN
        ResponseEntity<List<Gift>> response = giftController.getAllGifts(userId, personId, occasionId, null, null);

        // THEN
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(giftService, times(1)).getGiftsByPersonAndOccasion(userId, personId, occasionId);
    }

    @Test
    void shouldGetGiftsByGiftIdeaId() {
        // GIVEN
        List<Gift> gifts = List.of(testGift);
        when(giftService.getGiftsByGiftIdea(userId, giftIdeaId)).thenReturn(gifts);

        // WHEN
        ResponseEntity<List<Gift>> response = giftController.getAllGifts(userId, null, null, null, giftIdeaId);

        // THEN
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(giftService, times(1)).getGiftsByGiftIdea(userId, giftIdeaId);
    }

    @Test
    void shouldReturnEmptyListWhenNoGiftsFound() {
        // GIVEN
        when(giftService.getAllGiftsByUser(userId)).thenReturn(List.of());

        // WHEN
        ResponseEntity<List<Gift>> response = giftController.getAllGifts(userId, null, null, null, null);

        // THEN
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void shouldGetGiftByIdSuccessfully() {
        // GIVEN
        when(giftService.getGiftById(giftId, userId)).thenReturn(testGift);

        // WHEN
        ResponseEntity<Gift> response = giftController.getGiftById(giftId, userId);

        // THEN
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(giftId, response.getBody().getId());
        verify(giftService, times(1)).getGiftById(giftId, userId);
    }

    @Test
    void shouldSearchGifts() {
        // GIVEN
        String query = "birthday";
        List<Gift> gifts = List.of(testGift);
        when(giftService.searchGifts(userId, query)).thenReturn(gifts);

        // WHEN
        ResponseEntity<List<Gift>> response = giftController.searchGifts(query, userId);

        // THEN
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(giftService, times(1)).searchGifts(userId, query);
    }

    @Test
    void shouldReturnEmptyListWhenSearchYieldsNoResults() {
        // GIVEN
        String query = "nonexistent";
        when(giftService.searchGifts(userId, query)).thenReturn(List.of());

        // WHEN
        ResponseEntity<List<Gift>> response = giftController.searchGifts(query, userId);

        // THEN
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void shouldGetNotPurchasedGifts() {
        // GIVEN
        List<Gift> gifts = List.of(testGift);
        when(giftService.getNotPurchasedGifts(userId)).thenReturn(gifts);

        // WHEN
        ResponseEntity<List<Gift>> response = giftController.getNotPurchasedGifts(userId);

        // THEN
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(giftService, times(1)).getNotPurchasedGifts(userId);
    }

    @Test
    void shouldGetNotGivenGifts() {
        // GIVEN
        List<Gift> gifts = List.of(testGift);
        when(giftService.getNotGivenGifts(userId)).thenReturn(gifts);

        // WHEN
        ResponseEntity<List<Gift>> response = giftController.getNotGivenGifts(userId);

        // THEN
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(giftService, times(1)).getNotGivenGifts(userId);
    }

    @Test
    void shouldGetRecentGifts() {
        // GIVEN
        List<Gift> gifts = List.of(testGift);
        when(giftService.getRecentGifts(userId)).thenReturn(gifts);

        // WHEN
        ResponseEntity<List<Gift>> response = giftController.getRecentGifts(userId);

        // THEN
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(giftService, times(1)).getRecentGifts(userId);
    }

    @Test
    void shouldGetGiftsByWorkflowOrder() {
        // GIVEN
        List<Gift> gifts = List.of(testGift);
        when(giftService.getGiftsByWorkflowOrder(userId)).thenReturn(gifts);

        // WHEN
        ResponseEntity<List<Gift>> response = giftController.getGiftsByWorkflowOrder(userId);

        // THEN
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(giftService, times(1)).getGiftsByWorkflowOrder(userId);
    }

    @Test
    void shouldCheckIfGiftIdeaIsAlreadyUsed() {
        // GIVEN
        when(giftService.isGiftIdeaAlreadyUsed(userId, giftIdeaId)).thenReturn(true);

        // WHEN
        ResponseEntity<Boolean> response = giftController.isGiftIdeaAlreadyUsed(giftIdeaId, userId);

        // THEN
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody());
        verify(giftService, times(1)).isGiftIdeaAlreadyUsed(userId, giftIdeaId);
    }

    @Test
    void shouldReturnFalseWhenGiftIdeaNotUsed() {
        // GIVEN
        when(giftService.isGiftIdeaAlreadyUsed(userId, giftIdeaId)).thenReturn(false);

        // WHEN
        ResponseEntity<Boolean> response = giftController.isGiftIdeaAlreadyUsed(giftIdeaId, userId);

        // THEN
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody());
    }

    @Test
    void shouldUpdateGiftSuccessfully() {
        // GIVEN
        Gift updatedGift = new Gift();
        updatedGift.setTitle("Updated Title");
        when(giftService.updateGift(giftId, userId, updatedGift)).thenReturn(testGift);

        // WHEN
        ResponseEntity<Gift> response = giftController.updateGift(giftId, updatedGift, userId);

        // THEN
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(giftService, times(1)).updateGift(giftId, userId, updatedGift);
    }

    @Test
    void shouldUpdateGiftStatusToBought() {
        // GIVEN
        testGift.setStatus(GiftStatus.BOUGHT);
        testGift.setPurchaseDate(LocalDate.now());
        when(giftService.updateGiftStatus(giftId, userId, GiftStatus.BOUGHT)).thenReturn(testGift);

        // WHEN
        ResponseEntity<Gift> response = giftController.updateGiftStatus(giftId, GiftStatus.BOUGHT, userId);

        // THEN
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(GiftStatus.BOUGHT, response.getBody().getStatus());
        verify(giftService, times(1)).updateGiftStatus(giftId, userId, GiftStatus.BOUGHT);
    }

    @Test
    void shouldUpdateGiftStatusToGifted() {
        // GIVEN
        testGift.setStatus(GiftStatus.GIFTED);
        testGift.setGivenDate(LocalDate.now());
        when(giftService.updateGiftStatus(giftId, userId, GiftStatus.GIFTED)).thenReturn(testGift);

        // WHEN
        ResponseEntity<Gift> response = giftController.updateGiftStatus(giftId, GiftStatus.GIFTED, userId);

        // THEN
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(GiftStatus.GIFTED, response.getBody().getStatus());
        verify(giftService, times(1)).updateGiftStatus(giftId, userId, GiftStatus.GIFTED);
    }

    @Test
    void shouldDeleteGiftAndReturnNoContentStatus() {
        // GIVEN
        doNothing().when(giftService).deleteGift(giftId, userId);

        // WHEN
        ResponseEntity<Void> response = giftController.deleteGift(giftId, userId);

        // THEN
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(giftService, times(1)).deleteGift(giftId, userId);
    }
}

