package org.iu.presentmanager.giftIdeas;

import org.iu.presentmanager.exceptions.ResourceNotFoundException;
import org.iu.presentmanager.occasions.OccasionRepository;
import org.iu.presentmanager.persons.PersonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GiftIdeaServiceTest {

    @Mock
    private GiftIdeaRepository giftIdeaRepository;

    @Mock
    private PersonRepository personRepository;

    @Mock
    private OccasionRepository occasionRepository;

    @InjectMocks
    private GiftIdeaService giftIdeaService;

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
    void shouldCreateGiftIdeaSuccessfully() {
        // GIVEN
        GiftIdea newGiftIdea = new GiftIdea();
        newGiftIdea.setPersonId(personId);
        newGiftIdea.setOccasionId(occasionId);
        newGiftIdea.setTitle("New Gift Idea");
        newGiftIdea.setSource(GiftSource.MANUAL);

        when(personRepository.findByIdAndUserId(personId, userId)).thenReturn(Optional.of(new org.iu.presentmanager.persons.Person()));
        when(occasionRepository.findByIdAndUserId(occasionId, userId)).thenReturn(Optional.of(new org.iu.presentmanager.occasions.Occasion()));
        when(giftIdeaRepository.save(any(GiftIdea.class))).thenReturn(testGiftIdea);

        // WHEN
        GiftIdea result = giftIdeaService.createGiftIdea(newGiftIdea, userId);

        // THEN
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals("Test Gift Idea", result.getTitle());
        verify(giftIdeaRepository, times(1)).save(any(GiftIdea.class));
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenPersonNotFoundDuringCreate() {
        // GIVEN
        GiftIdea newGiftIdea = new GiftIdea();
        newGiftIdea.setPersonId(personId);
        newGiftIdea.setTitle("New Gift Idea");

        when(personRepository.findByIdAndUserId(personId, userId)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(ResourceNotFoundException.class, () -> giftIdeaService.createGiftIdea(newGiftIdea, userId));
        verify(giftIdeaRepository, never()).save(any());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenTitleIsEmpty() {
        // GIVEN
        GiftIdea newGiftIdea = new GiftIdea();
        newGiftIdea.setPersonId(personId);
        newGiftIdea.setTitle("");

        when(personRepository.findByIdAndUserId(personId, userId)).thenReturn(Optional.of(new org.iu.presentmanager.persons.Person()));

        // WHEN & THEN
        assertThrows(IllegalArgumentException.class, () -> giftIdeaService.createGiftIdea(newGiftIdea, userId));
        verify(giftIdeaRepository, never()).save(any());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenTitleIsNull() {
        // GIVEN
        GiftIdea newGiftIdea = new GiftIdea();
        newGiftIdea.setPersonId(personId);
        newGiftIdea.setTitle(null);

        when(personRepository.findByIdAndUserId(personId, userId)).thenReturn(Optional.of(new org.iu.presentmanager.persons.Person()));

        // WHEN & THEN
        assertThrows(IllegalArgumentException.class, () -> giftIdeaService.createGiftIdea(newGiftIdea, userId));
        verify(giftIdeaRepository, never()).save(any());
    }

    @Test
    void shouldValidateOccasionWhenProvided() {
        // GIVEN
        GiftIdea newGiftIdea = new GiftIdea();
        newGiftIdea.setPersonId(personId);
        newGiftIdea.setOccasionId(occasionId);
        newGiftIdea.setTitle("New Gift Idea");

        when(personRepository.findByIdAndUserId(personId, userId)).thenReturn(Optional.of(new org.iu.presentmanager.persons.Person()));
        when(occasionRepository.findByIdAndUserId(occasionId, userId)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(ResourceNotFoundException.class, () -> giftIdeaService.createGiftIdea(newGiftIdea, userId));
        verify(giftIdeaRepository, never()).save(any());
    }

    @Test
    void shouldSetUserIdOnCreatedGiftIdea() {
        // GIVEN
        GiftIdea newGiftIdea = new GiftIdea();
        newGiftIdea.setPersonId(personId);
        newGiftIdea.setTitle("New Gift Idea");

        when(personRepository.findByIdAndUserId(personId, userId)).thenReturn(Optional.of(new org.iu.presentmanager.persons.Person()));
        when(giftIdeaRepository.save(any(GiftIdea.class))).thenReturn(testGiftIdea);

        ArgumentCaptor<GiftIdea> captor = ArgumentCaptor.forClass(GiftIdea.class);

        // WHEN
        giftIdeaService.createGiftIdea(newGiftIdea, userId);

        // THEN
        verify(giftIdeaRepository).save(captor.capture());
        assertEquals(userId, captor.getValue().getUserId());
    }

    @Test
    void shouldAllowNullOccasionId() {
        // GIVEN
        GiftIdea newGiftIdea = new GiftIdea();
        newGiftIdea.setPersonId(personId);
        newGiftIdea.setOccasionId(null);
        newGiftIdea.setTitle("New Gift Idea");

        when(personRepository.findByIdAndUserId(personId, userId)).thenReturn(Optional.of(new org.iu.presentmanager.persons.Person()));
        when(giftIdeaRepository.save(any(GiftIdea.class))).thenReturn(testGiftIdea);

        // WHEN
        GiftIdea result = giftIdeaService.createGiftIdea(newGiftIdea, userId);

        // THEN
        assertNotNull(result);
        verify(giftIdeaRepository, times(1)).save(any(GiftIdea.class));
    }

    @Test
    void shouldGetAllGiftIdeasByUser() {
        // GIVEN
        List<GiftIdea> giftIdeas = List.of(testGiftIdea);
        when(giftIdeaRepository.findByUserId(userId)).thenReturn(giftIdeas);

        // WHEN
        List<GiftIdea> result = giftIdeaService.getAllGiftIdeasByUser(userId);

        // THEN
        assertEquals(1, result.size());
        assertEquals(giftIdeaId, result.getFirst().getId());
        verify(giftIdeaRepository, times(1)).findByUserId(userId);
    }

    @Test
    void shouldGetGiftIdeaByIdSuccessfully() {
        // GIVEN
        when(giftIdeaRepository.findByIdAndUserId(giftIdeaId, userId)).thenReturn(Optional.of(testGiftIdea));

        // WHEN
        GiftIdea result = giftIdeaService.getGiftIdeaById(giftIdeaId, userId);

        // THEN
        assertNotNull(result);
        assertEquals(giftIdeaId, result.getId());
        assertEquals(userId, result.getUserId());
    }

    @Test
    void shouldThrowExceptionWhenGiftIdeaNotFound() {
        // GIVEN
        when(giftIdeaRepository.findByIdAndUserId(giftIdeaId, userId)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(ResourceNotFoundException.class, () -> giftIdeaService.getGiftIdeaById(giftIdeaId, userId));
    }

    @Test
    void shouldGetGiftIdeasByPerson() {
        // GIVEN
        List<GiftIdea> giftIdeas = List.of(testGiftIdea);
        when(personRepository.findByIdAndUserId(personId, userId)).thenReturn(Optional.of(new org.iu.presentmanager.persons.Person()));
        when(giftIdeaRepository.findByUserIdAndPersonId(userId, personId)).thenReturn(giftIdeas);

        // WHEN
        List<GiftIdea> result = giftIdeaService.getGiftIdeasByPerson(userId, personId);

        // THEN
        assertEquals(1, result.size());
    }

    @Test
    void shouldThrowExceptionWhenPersonNotFoundInGetGiftIdeasByPerson() {
        // GIVEN
        when(personRepository.findByIdAndUserId(personId, userId)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(ResourceNotFoundException.class, () -> giftIdeaService.getGiftIdeasByPerson(userId, personId));
        verify(giftIdeaRepository, never()).findByUserIdAndPersonId(any(), any());
    }

    @Test
    void shouldGetGiftIdeasByOccasion() {
        // GIVEN
        List<GiftIdea> giftIdeas = List.of(testGiftIdea);
        when(occasionRepository.findByIdAndUserId(occasionId, userId)).thenReturn(Optional.of(new org.iu.presentmanager.occasions.Occasion()));
        when(giftIdeaRepository.findByUserIdAndOccasionId(userId, occasionId)).thenReturn(giftIdeas);

        // WHEN
        List<GiftIdea> result = giftIdeaService.getGiftIdeasByOccasion(userId, occasionId);

        // THEN
        assertEquals(1, result.size());
    }

    @Test
    void shouldGetGiftIdeasBySource() {
        // GIVEN
        List<GiftIdea> giftIdeas = List.of(testGiftIdea);
        when(giftIdeaRepository.findByUserIdAndSource(userId, GiftSource.AI)).thenReturn(giftIdeas);

        // WHEN
        List<GiftIdea> result = giftIdeaService.getGiftIdeasBySource(userId, GiftSource.AI);

        // THEN
        assertEquals(1, result.size());
    }

    @Test
    void shouldGetGiftIdeasByPersonAndOccasion() {
        // GIVEN
        List<GiftIdea> giftIdeas = List.of(testGiftIdea);
        when(personRepository.findByIdAndUserId(personId, userId)).thenReturn(Optional.of(new org.iu.presentmanager.persons.Person()));
        when(occasionRepository.findByIdAndUserId(occasionId, userId)).thenReturn(Optional.of(new org.iu.presentmanager.occasions.Occasion()));
        when(giftIdeaRepository.findByUserIdAndPersonIdAndOccasionId(userId, personId, occasionId)).thenReturn(giftIdeas);

        // WHEN
        List<GiftIdea> result = giftIdeaService.getGiftIdeasByPersonAndOccasion(userId, personId, occasionId);

        // THEN
        assertEquals(1, result.size());
    }

    @Test
    void shouldGetRecentGiftIdeas() {
        // GIVEN
        List<GiftIdea> giftIdeas = List.of(testGiftIdea);
        when(giftIdeaRepository.findRecentGiftIdeas(userId)).thenReturn(giftIdeas);

        // WHEN
        List<GiftIdea> result = giftIdeaService.getRecentGiftIdeas(userId);

        // THEN
        assertEquals(1, result.size());
    }

    @Test
    void shouldUpdateGiftIdeaSuccessfully() {
        // GIVEN
        GiftIdea updatedGiftIdea = new GiftIdea();
        updatedGiftIdea.setPersonId(personId);
        updatedGiftIdea.setOccasionId(occasionId);
        updatedGiftIdea.setTitle("Updated Title");
        updatedGiftIdea.setDescription("Updated Description");
        updatedGiftIdea.setLink("https://updated.com");
        updatedGiftIdea.setImageUrl("https://updated.com/img.jpg");
        updatedGiftIdea.setSource(GiftSource.AI);

        when(giftIdeaRepository.findByIdAndUserId(giftIdeaId, userId)).thenReturn(Optional.of(testGiftIdea));
        when(giftIdeaRepository.save(any(GiftIdea.class))).thenReturn(testGiftIdea);

        // WHEN
        GiftIdea result = giftIdeaService.updateGiftIdea(giftIdeaId, userId, updatedGiftIdea);

        // THEN
        assertNotNull(result);
        verify(giftIdeaRepository, times(1)).save(any(GiftIdea.class));
    }

    @Test
    void shouldThrowExceptionWhenTryingToChangePersonDuringUpdate() {
        // GIVEN
        UUID newPersonId = UUID.randomUUID();
        GiftIdea updatedGiftIdea = new GiftIdea();
        updatedGiftIdea.setPersonId(newPersonId);
        updatedGiftIdea.setOccasionId(occasionId);

        when(giftIdeaRepository.findByIdAndUserId(giftIdeaId, userId)).thenReturn(Optional.of(testGiftIdea));

        // WHEN & THEN
        assertThrows(IllegalArgumentException.class, () -> giftIdeaService.updateGiftIdea(giftIdeaId, userId, updatedGiftIdea));
        verify(giftIdeaRepository, never()).save(any());
    }

    @Test
    void shouldAllowUpdatingOccasionToNull() {
        // GIVEN
        testGiftIdea.setOccasionId(null);
        GiftIdea updatedGiftIdea = new GiftIdea();
        updatedGiftIdea.setPersonId(personId);
        updatedGiftIdea.setOccasionId(null);
        updatedGiftIdea.setTitle("Updated Title");

        when(giftIdeaRepository.findByIdAndUserId(giftIdeaId, userId)).thenReturn(Optional.of(testGiftIdea));
        when(giftIdeaRepository.save(any(GiftIdea.class))).thenReturn(testGiftIdea);

        // WHEN
        GiftIdea result = giftIdeaService.updateGiftIdea(giftIdeaId, userId, updatedGiftIdea);

        // THEN
        assertNotNull(result);
        verify(giftIdeaRepository, times(1)).save(any(GiftIdea.class));
    }

    @Test
    void shouldSearchGiftIdeas() {
        // GIVEN
        List<GiftIdea> giftIdeas = List.of(testGiftIdea);
        String searchTerm = "Test";
        when(giftIdeaRepository.searchByTitleOrDescription(userId, searchTerm)).thenReturn(giftIdeas);

        // WHEN
        List<GiftIdea> result = giftIdeaService.searchGiftIdeas(userId, searchTerm);

        // THEN
        assertEquals(1, result.size());
        verify(giftIdeaRepository, times(1)).searchByTitleOrDescription(userId, searchTerm);
    }

    @Test
    void shouldReturnEmptyListWhenSearchYieldsNoResults() {
        // GIVEN
        String searchTerm = "nonexistent";
        when(giftIdeaRepository.searchByTitleOrDescription(userId, searchTerm)).thenReturn(List.of());

        // WHEN
        List<GiftIdea> result = giftIdeaService.searchGiftIdeas(userId, searchTerm);

        // THEN
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldDeleteGiftIdeaSuccessfully() {
        // GIVEN
        when(giftIdeaRepository.findByIdAndUserId(giftIdeaId, userId)).thenReturn(Optional.of(testGiftIdea));

        // WHEN
        giftIdeaService.deleteGiftIdea(giftIdeaId, userId);

        // THEN
        verify(giftIdeaRepository, times(1)).deleteByIdAndUserId(giftIdeaId, userId);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentGiftIdea() {
        // GIVEN
        when(giftIdeaRepository.findByIdAndUserId(giftIdeaId, userId)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(ResourceNotFoundException.class, () -> giftIdeaService.deleteGiftIdea(giftIdeaId, userId));
        verify(giftIdeaRepository, never()).deleteByIdAndUserId(any(), any());
    }
}

