package org.iu.presentmanager.gifts;

import org.iu.presentmanager.exceptions.ResourceNotFoundException;
import org.iu.presentmanager.giftideas.GiftIdea;
import org.iu.presentmanager.giftideas.GiftIdeaRepository;
import org.iu.presentmanager.giftideas.GiftSource;
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
class GiftServiceTest {

    @Mock
    private GiftRepository giftRepository;

    @Mock
    private PersonRepository personRepository;

    @Mock
    private OccasionRepository occasionRepository;

    @Mock
    private GiftIdeaRepository giftIdeaRepository;

    @InjectMocks
    private GiftService giftService;

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
    void shouldCreateGiftSuccessfully() {
        // GIVEN
        Gift newGift = new Gift();
        newGift.setPersonId(personId);
        newGift.setOccasionId(occasionId);
        newGift.setTitle("New Gift");
        newGift.setStatus(GiftStatus.PLANNED);

        when(personRepository.findByIdAndUserId(personId, userId)).thenReturn(Optional.of(new org.iu.presentmanager.persons.Person()));
        when(occasionRepository.findByIdAndUserId(occasionId, userId)).thenReturn(Optional.of(new org.iu.presentmanager.occasions.Occasion()));
        when(giftRepository.save(any(Gift.class))).thenReturn(testGift);

        // WHEN
        Gift result = giftService.createGift(newGift, userId);

        // THEN
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals("Test Gift", result.getTitle());
        verify(giftRepository, times(1)).save(any(Gift.class));
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenPersonNotFoundDuringCreate() {
        // GIVEN
        Gift newGift = new Gift();
        newGift.setPersonId(personId);
        newGift.setOccasionId(occasionId);
        newGift.setTitle("New Gift");

        when(personRepository.findByIdAndUserId(personId, userId)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(ResourceNotFoundException.class, () -> giftService.createGift(newGift, userId));
        verify(giftRepository, never()).save(any());
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenOccasionNotFoundDuringCreate() {
        // GIVEN
        Gift newGift = new Gift();
        newGift.setPersonId(personId);
        newGift.setOccasionId(occasionId);
        newGift.setTitle("New Gift");

        when(personRepository.findByIdAndUserId(personId, userId)).thenReturn(Optional.of(new org.iu.presentmanager.persons.Person()));
        when(occasionRepository.findByIdAndUserId(occasionId, userId)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(ResourceNotFoundException.class, () -> giftService.createGift(newGift, userId));
        verify(giftRepository, never()).save(any());
    }

    @Test
    void shouldValidateGiftIdeaWhenProvided() {
        // GIVEN
        Gift newGift = new Gift();
        newGift.setPersonId(personId);
        newGift.setOccasionId(occasionId);
        newGift.setGiftIdeaId(giftIdeaId);
        newGift.setTitle("New Gift");

        when(personRepository.findByIdAndUserId(personId, userId)).thenReturn(Optional.of(new org.iu.presentmanager.persons.Person()));
        when(occasionRepository.findByIdAndUserId(occasionId, userId)).thenReturn(Optional.of(new org.iu.presentmanager.occasions.Occasion()));
        when(giftIdeaRepository.findByIdAndUserId(giftIdeaId, userId)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(ResourceNotFoundException.class, () -> giftService.createGift(newGift, userId));
        verify(giftRepository, never()).save(any());
    }

    @Test
    void shouldSetUserIdOnCreatedGift() {
        // GIVEN
        Gift newGift = new Gift();
        newGift.setPersonId(personId);
        newGift.setOccasionId(occasionId);
        newGift.setTitle("New Gift");

        when(personRepository.findByIdAndUserId(personId, userId)).thenReturn(Optional.of(new org.iu.presentmanager.persons.Person()));
        when(occasionRepository.findByIdAndUserId(occasionId, userId)).thenReturn(Optional.of(new org.iu.presentmanager.occasions.Occasion()));
        when(giftRepository.save(any(Gift.class))).thenReturn(testGift);

        ArgumentCaptor<Gift> captor = ArgumentCaptor.forClass(Gift.class);

        // WHEN
        giftService.createGift(newGift, userId);

        // THEN
        verify(giftRepository).save(captor.capture());
        assertEquals(userId, captor.getValue().getUserId());
    }

    @Test
    void shouldCreateGiftFromGiftIdeaSuccessfully() {
        // GIVEN
        GiftIdea giftIdea = new GiftIdea();
        giftIdea.setId(giftIdeaId);
        giftIdea.setPersonId(personId);
        giftIdea.setOccasionId(occasionId);
        giftIdea.setUserId(userId);
        giftIdea.setTitle("Gift Idea Title");
        giftIdea.setDescription("Gift Description");
        giftIdea.setLink("https://example.com");
        giftIdea.setImageUrl("https://example.com/image.jpg");

        testGift.setGiftIdeaId(giftIdeaId);
        
        when(giftIdeaRepository.findByIdAndUserId(giftIdeaId, userId)).thenReturn(Optional.of(giftIdea));
        when(giftRepository.save(any(Gift.class))).thenReturn(testGift);

        // WHEN
        Gift result = giftService.createGiftFromGiftIdea(giftIdea, userId);

        // THEN
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(giftIdeaId, result.getGiftIdeaId());
        verify(giftRepository, times(1)).save(any(Gift.class));
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenGiftIdeaNotFound() {
        // GIVEN
        GiftIdea giftIdea = new GiftIdea();
        giftIdea.setId(giftIdeaId);

        when(giftIdeaRepository.findByIdAndUserId(giftIdeaId, userId)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(ResourceNotFoundException.class, () -> giftService.createGiftFromGiftIdea(giftIdea, userId));
        verify(giftRepository, never()).save(any());
    }

    @Test
    void shouldCopyAllPropertiesFromGiftIdea() {
        // GIVEN
        GiftIdea giftIdea = new GiftIdea();
        giftIdea.setId(giftIdeaId);
        giftIdea.setPersonId(personId);
        giftIdea.setOccasionId(occasionId);
        giftIdea.setUserId(userId);
        giftIdea.setTitle("Test Title");
        giftIdea.setDescription("Test Description");
        giftIdea.setLink("https://test.com");
        giftIdea.setImageUrl("https://test.com/img.jpg");
        giftIdea.setSource(GiftSource.AI);

        when(giftIdeaRepository.findByIdAndUserId(giftIdeaId, userId)).thenReturn(Optional.of(giftIdea));
        when(giftRepository.save(any(Gift.class))).thenReturn(testGift);

        ArgumentCaptor<Gift> captor = ArgumentCaptor.forClass(Gift.class);

        // WHEN
        giftService.createGiftFromGiftIdea(giftIdea, userId);

        // THEN
        verify(giftRepository).save(captor.capture());
        Gift savedGift = captor.getValue();
        assertEquals("Test Title", savedGift.getTitle());
        assertEquals("Test Description", savedGift.getDescription());
        assertEquals("https://test.com", savedGift.getLink());
        assertEquals("https://test.com/img.jpg", savedGift.getImageUrl());
    }

    @Test
    void shouldGetAllGiftsByUser() {
        // GIVEN
        List<Gift> gifts = List.of(testGift);
        when(giftRepository.findByUserId(userId)).thenReturn(gifts);

        // WHEN
        List<Gift> result = giftService.getAllGiftsByUser(userId);

        // THEN
        assertEquals(1, result.size());
        assertEquals(giftId, result.getFirst().getId());
        verify(giftRepository, times(1)).findByUserId(userId);
    }

    @Test
    void shouldGetGiftByIdSuccessfully() {
        // GIVEN
        when(giftRepository.findByIdAndUserId(giftId, userId)).thenReturn(Optional.of(testGift));

        // WHEN
        Gift result = giftService.getGiftById(giftId, userId);

        // THEN
        assertNotNull(result);
        assertEquals(giftId, result.getId());
        assertEquals(userId, result.getUserId());
    }

    @Test
    void shouldThrowExceptionWhenGiftNotFound() {
        // GIVEN
        when(giftRepository.findByIdAndUserId(giftId, userId)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(ResourceNotFoundException.class, () -> giftService.getGiftById(giftId, userId));
    }

    @Test
    void shouldGetGiftsByPerson() {
        // GIVEN
        List<Gift> gifts = List.of(testGift);
        when(personRepository.findByIdAndUserId(personId, userId)).thenReturn(Optional.of(new org.iu.presentmanager.persons.Person()));
        when(giftRepository.findByUserIdAndPersonId(userId, personId)).thenReturn(gifts);

        // WHEN
        List<Gift> result = giftService.getGiftsByPerson(userId, personId);

        // THEN
        assertEquals(1, result.size());
    }

    @Test
    void shouldThrowExceptionWhenPersonNotFoundInGetGiftsByPerson() {
        // GIVEN
        when(personRepository.findByIdAndUserId(personId, userId)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(ResourceNotFoundException.class, () -> giftService.getGiftsByPerson(userId, personId));
        verify(giftRepository, never()).findByUserIdAndPersonId(any(), any());
    }

    @Test
    void shouldGetGiftsByOccasion() {
        // GIVEN
        List<Gift> gifts = List.of(testGift);
        when(occasionRepository.findByIdAndUserId(occasionId, userId)).thenReturn(Optional.of(new org.iu.presentmanager.occasions.Occasion()));
        when(giftRepository.findByUserIdAndOccasionId(userId, occasionId)).thenReturn(gifts);

        // WHEN
        List<Gift> result = giftService.getGiftsByOccasion(userId, occasionId);

        // THEN
        assertEquals(1, result.size());
    }

    @Test
    void shouldGetGiftsByStatus() {
        // GIVEN
        List<Gift> gifts = List.of(testGift);
        when(giftRepository.findByUserIdAndStatus(userId, GiftStatus.PLANNED)).thenReturn(gifts);

        // WHEN
        List<Gift> result = giftService.getGiftsByStatus(userId, GiftStatus.PLANNED);

        // THEN
        assertEquals(1, result.size());
        assertEquals(GiftStatus.PLANNED, result.getFirst().getStatus());
    }

    @Test
    void shouldUpdateGiftSuccessfully() {
        // GIVEN
        UUID newPersonId = UUID.randomUUID();
        UUID newOccasionId = UUID.randomUUID();
        
        Gift updatedGift = new Gift();
        updatedGift.setPersonId(newPersonId);
        updatedGift.setOccasionId(newOccasionId);
        updatedGift.setTitle("Updated Title");
        updatedGift.setStatus(GiftStatus.BOUGHT);

        when(giftRepository.findByIdAndUserId(giftId, userId)).thenReturn(Optional.of(testGift));
        when(personRepository.findByIdAndUserId(newPersonId, userId)).thenReturn(Optional.of(new org.iu.presentmanager.persons.Person()));
        when(occasionRepository.findByIdAndUserId(newOccasionId, userId)).thenReturn(Optional.of(new org.iu.presentmanager.occasions.Occasion()));
        when(giftRepository.save(any(Gift.class))).thenReturn(testGift);

        // WHEN
        Gift result = giftService.updateGift(giftId, userId, updatedGift);

        // THEN
        assertNotNull(result);
        verify(giftRepository, times(1)).save(any(Gift.class));
    }

    @Test
    void shouldUpdateGiftStatusToBoughtAndSetPurchaseDate() {
        // GIVEN
        when(giftRepository.findByIdAndUserId(giftId, userId)).thenReturn(Optional.of(testGift));
        when(giftRepository.save(any(Gift.class))).thenReturn(testGift);

        ArgumentCaptor<Gift> captor = ArgumentCaptor.forClass(Gift.class);

        // WHEN
        giftService.updateGiftStatus(giftId, userId, GiftStatus.BOUGHT);

        // THEN
        verify(giftRepository).save(captor.capture());
        Gift savedGift = captor.getValue();
        assertEquals(GiftStatus.BOUGHT, savedGift.getStatus());
        assertNotNull(savedGift.getPurchaseDate());
    }

    @Test
    void shouldUpdateGiftStatusToGiftedAndSetGivenDate() {
        // GIVEN
        when(giftRepository.findByIdAndUserId(giftId, userId)).thenReturn(Optional.of(testGift));
        when(giftRepository.save(any(Gift.class))).thenReturn(testGift);

        ArgumentCaptor<Gift> captor = ArgumentCaptor.forClass(Gift.class);

        // WHEN
        giftService.updateGiftStatus(giftId, userId, GiftStatus.GIFTED);

        // THEN
        verify(giftRepository).save(captor.capture());
        Gift savedGift = captor.getValue();
        assertEquals(GiftStatus.GIFTED, savedGift.getStatus());
        assertNotNull(savedGift.getGivenDate());
    }

    @Test
    void shouldDeleteGiftSuccessfully() {
        // GIVEN
        when(giftRepository.findByIdAndUserId(giftId, userId)).thenReturn(Optional.of(testGift));

        // WHEN
        giftService.deleteGift(giftId, userId);

        // THEN
        verify(giftRepository, times(1)).deleteByUserIdAndId(userId, giftId);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentGift() {
        // GIVEN
        when(giftRepository.findByIdAndUserId(giftId, userId)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(ResourceNotFoundException.class, () -> giftService.deleteGift(giftId, userId));
        verify(giftRepository, never()).deleteByUserIdAndId(any(), any());
    }

    @Test
    void shouldSearchGifts() {
        // GIVEN
        List<Gift> gifts = List.of(testGift);
        String searchTerm = "Test";
        when(giftRepository.searchByTitleOrDescription(userId, searchTerm)).thenReturn(gifts);

        // WHEN
        List<Gift> result = giftService.searchGifts(userId, searchTerm);

        // THEN
        assertEquals(1, result.size());
        verify(giftRepository, times(1)).searchByTitleOrDescription(userId, searchTerm);
    }

    @Test
    void shouldGetNotPurchasedGifts() {
        // GIVEN
        List<Gift> gifts = List.of(testGift);
        when(giftRepository.findNotPurchased(userId)).thenReturn(gifts);

        // WHEN
        List<Gift> result = giftService.getNotPurchasedGifts(userId);

        // THEN
        assertEquals(1, result.size());
    }

    @Test
    void shouldGetNotGivenGifts() {
        // GIVEN
        List<Gift> gifts = List.of(testGift);
        when(giftRepository.findNotGiven(userId)).thenReturn(gifts);

        // WHEN
        List<Gift> result = giftService.getNotGivenGifts(userId);

        // THEN
        assertEquals(1, result.size());
    }

    @Test
    void shouldGetRecentGifts() {
        // GIVEN
        List<Gift> gifts = List.of(testGift);
        when(giftRepository.findRecentGifts(userId)).thenReturn(gifts);

        // WHEN
        List<Gift> result = giftService.getRecentGifts(userId);

        // THEN
        assertEquals(1, result.size());
    }

    @Test
    void shouldCheckIfGiftIdeaIsAlreadyUsed() {
        // GIVEN
        when(giftRepository.existsByUserIdAndGiftIdeaId(userId, giftIdeaId)).thenReturn(true);

        // WHEN
        boolean result = giftService.isGiftIdeaAlreadyUsed(userId, giftIdeaId);

        // THEN
        assertTrue(result);
    }
}



