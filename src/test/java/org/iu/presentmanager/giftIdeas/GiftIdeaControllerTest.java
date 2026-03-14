package org.iu.presentmanager.giftIdeas;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GiftIdeaControllerTest {

    @Mock
    private GiftIdeaService giftIdeaService;

    @InjectMocks
    private GiftIdeaController giftIdeaController;

    private UUID userId;
    private UUID personId;
    private UUID occasionId;
    private UUID giftIdeaId;
    private GiftIdea testGiftIdea;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        personId = UUID.randomUUID();
        occasionId = UUID.randomUUID();
        giftIdeaId = UUID.randomUUID();

        testGiftIdea = new GiftIdea();
        testGiftIdea.setId(giftIdeaId);
        testGiftIdea.setUserId(userId);
        testGiftIdea.setPersonId(personId);
        testGiftIdea.setOccasionId(occasionId);
        testGiftIdea.setTitle("Test Gift Idea");
        testGiftIdea.setSource(GiftSource.MANUAL);
    }

    @Test
    void shouldCreateGiftIdeaAndReturnCreatedStatus() {
        // GIVEN
        GiftIdea newGiftIdea = new GiftIdea();
        newGiftIdea.setPersonId(personId);
        newGiftIdea.setOccasionId(occasionId);
        newGiftIdea.setTitle("New Gift Idea");

        when(giftIdeaService.createGiftIdea(any(GiftIdea.class), eq(userId))).thenReturn(testGiftIdea);

        // WHEN
        ResponseEntity<GiftIdea> response = giftIdeaController.createGiftIdea(newGiftIdea, userId);

        // THEN
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(giftIdeaId, response.getBody().getId());
        verify(giftIdeaService, times(1)).createGiftIdea(any(GiftIdea.class), eq(userId));
    }

    @Test
    void shouldReturnCreatedGiftIdeaObject() {
        // GIVEN
        GiftIdea newGiftIdea = new GiftIdea();
        newGiftIdea.setTitle("Test Gift Idea");
        when(giftIdeaService.createGiftIdea(any(GiftIdea.class), eq(userId))).thenReturn(testGiftIdea);

        // WHEN
        ResponseEntity<GiftIdea> response = giftIdeaController.createGiftIdea(newGiftIdea, userId);

        // THEN
        assertEquals(testGiftIdea, response.getBody());
    }

    @Test
    void shouldGetAllGiftIdeasWhenNoFilterParametersProvided() {
        // GIVEN
        List<GiftIdea> giftIdeas = List.of(testGiftIdea);
        when(giftIdeaService.getAllGiftIdeasByUser(userId)).thenReturn(giftIdeas);

        // WHEN
        ResponseEntity<List<GiftIdea>> response = giftIdeaController.getAllGiftIdeas(userId, null, null, null);

        // THEN
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(giftIdeaId, response.getBody().getFirst().getId());
        verify(giftIdeaService, times(1)).getAllGiftIdeasByUser(userId);
    }

    @Test
    void shouldGetGiftIdeasByPersonWhenPersonIdProvided() {
        // GIVEN
        List<GiftIdea> giftIdeas = List.of(testGiftIdea);
        when(giftIdeaService.getGiftIdeasByPerson(userId, personId)).thenReturn(giftIdeas);

        // WHEN
        ResponseEntity<List<GiftIdea>> response = giftIdeaController.getAllGiftIdeas(userId, personId, null, null);

        // THEN
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(giftIdeaService, times(1)).getGiftIdeasByPerson(userId, personId);
        verify(giftIdeaService, never()).getAllGiftIdeasByUser(any());
    }

    @Test
    void shouldGetGiftIdeasByOccasionWhenOccasionIdProvided() {
        // GIVEN
        List<GiftIdea> giftIdeas = List.of(testGiftIdea);
        when(giftIdeaService.getGiftIdeasByOccasion(userId, occasionId)).thenReturn(giftIdeas);

        // WHEN
        ResponseEntity<List<GiftIdea>> response = giftIdeaController.getAllGiftIdeas(userId, null, occasionId, null);

        // THEN
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(giftIdeaService, times(1)).getGiftIdeasByOccasion(userId, occasionId);
    }

    @Test
    void shouldGetGiftIdeasBySourceWhenSourceProvided() {
        // GIVEN
        List<GiftIdea> giftIdeas = List.of(testGiftIdea);
        when(giftIdeaService.getGiftIdeasBySource(userId, GiftSource.AI)).thenReturn(giftIdeas);

        // WHEN
        ResponseEntity<List<GiftIdea>> response = giftIdeaController.getAllGiftIdeas(userId, null, null, GiftSource.AI);

        // THEN
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(giftIdeaService, times(1)).getGiftIdeasBySource(userId, GiftSource.AI);
    }

    @Test
    void shouldGetGiftIdeasByPersonAndOccasionWhenBothProvided() {
        // GIVEN
        List<GiftIdea> giftIdeas = List.of(testGiftIdea);
        when(giftIdeaService.getGiftIdeasByPersonAndOccasion(userId, personId, occasionId)).thenReturn(giftIdeas);

        // WHEN
        ResponseEntity<List<GiftIdea>> response = giftIdeaController.getAllGiftIdeas(userId, personId, occasionId, null);

        // THEN
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(giftIdeaService, times(1)).getGiftIdeasByPersonAndOccasion(userId, personId, occasionId);
    }

    @Test
    void shouldReturnEmptyListWhenNoGiftIdeasFound() {
        // GIVEN
        when(giftIdeaService.getAllGiftIdeasByUser(userId)).thenReturn(List.of());

        // WHEN
        ResponseEntity<List<GiftIdea>> response = giftIdeaController.getAllGiftIdeas(userId, null, null, null);

        // THEN
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void shouldGetGiftIdeaByIdSuccessfully() {
        // GIVEN
        when(giftIdeaService.getGiftIdeaById(giftIdeaId, userId)).thenReturn(testGiftIdea);

        // WHEN
        ResponseEntity<GiftIdea> response = giftIdeaController.getGiftIdeaById(giftIdeaId, userId);

        // THEN
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(giftIdeaId, response.getBody().getId());
        verify(giftIdeaService, times(1)).getGiftIdeaById(giftIdeaId, userId);
    }

    @Test
    void shouldSearchGiftIdeas() {
        // GIVEN
        String query = "birthday";
        List<GiftIdea> giftIdeas = List.of(testGiftIdea);
        when(giftIdeaService.searchGiftIdeas(userId, query)).thenReturn(giftIdeas);

        // WHEN
        ResponseEntity<List<GiftIdea>> response = giftIdeaController.searchGiftIdeas(query, userId);

        // THEN
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(giftIdeaService, times(1)).searchGiftIdeas(userId, query);
    }

    @Test
    void shouldReturnEmptyListWhenSearchYieldsNoResults() {
        // GIVEN
        String query = "nonexistent";
        when(giftIdeaService.searchGiftIdeas(userId, query)).thenReturn(List.of());

        // WHEN
        ResponseEntity<List<GiftIdea>> response = giftIdeaController.searchGiftIdeas(query, userId);

        // THEN
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void shouldGetRecentGiftIdeas() {
        // GIVEN
        List<GiftIdea> giftIdeas = List.of(testGiftIdea);
        when(giftIdeaService.getRecentGiftIdeas(userId)).thenReturn(giftIdeas);

        // WHEN
        ResponseEntity<List<GiftIdea>> response = giftIdeaController.getRecentGiftIdeas(userId);

        // THEN
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(giftIdeaService, times(1)).getRecentGiftIdeas(userId);
    }

    @Test
    void shouldUpdateGiftIdeaSuccessfully() {
        // GIVEN
        GiftIdea updatedGiftIdea = new GiftIdea();
        updatedGiftIdea.setPersonId(personId);
        updatedGiftIdea.setOccasionId(occasionId);
        updatedGiftIdea.setTitle("Updated Title");

        when(giftIdeaService.updateGiftIdea(giftIdeaId, userId, updatedGiftIdea)).thenReturn(testGiftIdea);

        // WHEN
        ResponseEntity<GiftIdea> response = giftIdeaController.updateGiftIdea(giftIdeaId, updatedGiftIdea, userId);

        // THEN
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(giftIdeaService, times(1)).updateGiftIdea(giftIdeaId, userId, updatedGiftIdea);
    }

    @Test
    void shouldDeleteGiftIdeaAndReturnNoContentStatus() {
        // GIVEN
        doNothing().when(giftIdeaService).deleteGiftIdea(giftIdeaId, userId);

        // WHEN
        ResponseEntity<Void> response = giftIdeaController.deleteGiftIdea(giftIdeaId, userId);

        // THEN
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(giftIdeaService, times(1)).deleteGiftIdea(giftIdeaId, userId);
    }
}

