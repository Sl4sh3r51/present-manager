package org.iu.presentmanager.interests;

import org.iu.presentmanager.exceptions.DuplicateResourceException;
import org.iu.presentmanager.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InterestServiceTest {

    @Mock
    private InterestRepository interestRepository;

    @InjectMocks
    private InterestService interestService;

    private Interest testInterest;

    @BeforeEach
    void setUp() {
        testInterest = new Interest();
        testInterest.setId(UUID.randomUUID());
        testInterest.setName("Test Interest");
    }

    @Test
    void shouldReturnAllInterests() {
        // Given
        when(interestRepository.findAll()).thenReturn(List.of(testInterest));

        // When
        List<Interest> result = interestService.getAllInterests();

        // Then
        assertEquals(1, result.size());
        assertEquals("Test Interest", result.getFirst().getName());
    }

    @Test
    void shouldReturnEmptyListWhenNoInterests() {
        // Given
        when(interestRepository.findAll()).thenReturn(List.of());

        // When
        List<Interest> result = interestService.getAllInterests();

        // Then
        assertEquals(0, result.size());
    }

    @Test
    void shouldReturnInterestById() {
        // Given
        when(interestRepository.findById(testInterest.getId())).thenReturn(Optional.of(testInterest));

        // When
        Interest result = interestService.getInterestById(testInterest.getId());

        // Then
        assertNotNull(result);
        assertEquals("Test Interest", result.getName());
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenInterestNotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(interestRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> interestService.getInterestById(nonExistentId));
        verify(interestRepository).findById(nonExistentId);
    }

    @Test
    void shouldReturnInterestByName() {
        // Given
        when(interestRepository.findByNameIgnoreCase("Test Interest")).thenReturn(Optional.of(testInterest));

        // When
        Interest result = interestService.getInterestByName("Test Interest");

        // Then
        assertNotNull(result);
        assertEquals("Test Interest", result.getName());
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenInterestByNameNotFound() {
        // Given
        when(interestRepository.findByNameIgnoreCase("Nonexistent Interest")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> interestService.getInterestByName("Nonexistent Interest"));
        verify(interestRepository).findByNameIgnoreCase("Nonexistent Interest");
    }

    @Test
    void shouldCreateNewInterest() {
        // Given
        Interest newInterest = new Interest();
        newInterest.setName("New Interest");
        when(interestRepository.save(newInterest)).thenReturn(newInterest);

        // When
        Interest result = interestService.createInterest(newInterest);

        // Then
        assertNotNull(result);
        assertEquals("New Interest", result.getName());
        verify(interestRepository).save(newInterest);
    }

    @Test
    void shouldReturnDuplicateResourceExceptionWhenCreatingInterestWithExistingName() {
        // Given
        Interest newInterest = new Interest();
        newInterest.setName("Test Interest");
        when(interestRepository.existsByNameIgnoreCase("Test Interest")).thenReturn(true);

        // When & Then
        assertThrows(DuplicateResourceException.class, () -> interestService.createInterest(newInterest));
        verify(interestRepository).existsByNameIgnoreCase("Test Interest");
    }

    @Test
    void shouldReturnIllegalArgumentExceptionWhenCreatingInterestWithEmptyName() {
        // Given
        Interest newInterest = new Interest();
        newInterest.setName("");

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> interestService.createInterest(newInterest));
    }

    @Test
    void shouldDeleteInterest() {
        // Given
        when(interestRepository.existsById(testInterest.getId())).thenReturn(true);
        when(interestRepository.findById(testInterest.getId())).thenReturn(Optional.of(testInterest));

        // When
        interestService.deleteInterest(testInterest.getId());

        // Then
        verify(interestRepository).existsById(testInterest.getId());
        verify(interestRepository).findById(testInterest.getId());
        verify(interestRepository).delete(testInterest);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenDeleteInterestWithNonExistentId() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(interestRepository.existsById(nonExistentId)).thenReturn(false);

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> interestService.deleteInterest(nonExistentId));
        verify(interestRepository).existsById(nonExistentId);
    }

    @Test
    void shouldReturnTrueWhenInterestExistsByName() {
        // Given
        when(interestRepository.existsByNameIgnoreCase("Test Interest")).thenReturn(true);

        // When
        boolean exists = interestService.existsByName("Test Interest");

        // Then
        assertTrue(exists);
        verify(interestRepository).existsByNameIgnoreCase("Test Interest");
    }

    @Test
    void shouldReturnFalseWhenInterestDoesNotExistByName() {
        // Given
        when(interestRepository.existsByNameIgnoreCase("Nonexistent Interest")).thenReturn(false);

        // When
        boolean exists = interestService.existsByName("Nonexistent Interest");

        // Then
        assertFalse(exists);
        verify(interestRepository).existsByNameIgnoreCase("Nonexistent Interest");
    }

    @Test
    void shouldReturnIllegalArgumentExceptionWhenExistsByNameWithNull() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> interestService.existsByName(null));
    }

    @Test
    void shouldReturnIllegalArgumentExceptionWhenExistsByNameWithBlank() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> interestService.existsByName("   "));
    }

    @Test
    void shouldReturnIllegalArgumentExceptionWhenCreatingInterestWithNullName() {
        // Given
        Interest newInterest = new Interest();
        newInterest.setName(null);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> interestService.createInterest(newInterest));
    }

    @Test
    void shouldReturnIllegalArgumentExceptionWhenCreatingInterestWithBlankName() {
        // Given
        Interest newInterest = new Interest();
        newInterest.setName("   ");

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> interestService.createInterest(newInterest));
    }

}
