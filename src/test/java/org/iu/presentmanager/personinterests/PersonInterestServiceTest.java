package org.iu.presentmanager.personinterests;

import org.iu.presentmanager.exceptions.DuplicateResourceException;
import org.iu.presentmanager.exceptions.ResourceNotFoundException;
import org.iu.presentmanager.interests.Interest;
import org.iu.presentmanager.interests.InterestRepository;
import org.iu.presentmanager.persons.Person;
import org.iu.presentmanager.persons.PersonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersonInterestServiceTest {

    @Mock
    private PersonInterestRepository personInterestRepository;

    @Mock
    private PersonRepository personRepository;

    @Mock
    private InterestRepository interestRepository;

    @InjectMocks
    private PersonInterestService personInterestService;

    private final UUID userId = UUID.randomUUID();
    private final UUID personId = UUID.randomUUID();
    private final UUID interestId = UUID.randomUUID();
    private final UUID interestId2 = UUID.randomUUID();
    private final UUID otherPersonId = UUID.randomUUID();

    private Person testPerson;
    private Interest testInterest;
    private Interest testInterest2;
    private PersonInterest testPersonInterest;

    @BeforeEach
    void setUp() {
        testPerson = createTestPerson("Alice", userId);
        testInterest = createTestInterest("Reading");
        testInterest2 = createTestInterest("Gaming");
        testPersonInterest = createTestPersonInterest(personId, interestId, testPerson, testInterest);
    }

    private Person createTestPerson(String name, UUID userId) {
        Person person = new Person();
        person.setId(personId);
        person.setName(name);
        person.setUserId(userId);
        person.setBirthday(LocalDate.of(1990, 3, 15));
        return person;
    }

    private Interest createTestInterest(String name) {
        Interest interest = new Interest();
        interest.setId(UUID.randomUUID());
        interest.setName(name);
        return interest;
    }

    private PersonInterest createTestPersonInterest(UUID pId, UUID iId, Person person, Interest interest) {
        PersonInterest pi = new PersonInterest();
        pi.setId(new PersonInterestId(pId, iId));
        pi.setPerson(person);
        pi.setInterest(interest);
        return pi;
    }

    @Test
    void shouldAddInterestSuccessfully() {
        // GIVEN
        when(personRepository.findByIdAndUserId(personId, userId))
                .thenReturn(Optional.of(testPerson));
        when(interestRepository.findById(interestId))
                .thenReturn(Optional.of(testInterest));
        when(personInterestRepository.existsByPersonIdAndInterestId(personId, interestId))
                .thenReturn(false);
        when(personInterestRepository.save(any(PersonInterest.class)))
                .thenReturn(testPersonInterest);

        // WHEN
        PersonInterest result = personInterestService.addInterest(personId, userId, interestId);

        // THEN
        assertNotNull(result);
        assertEquals(personId, result.getId().getPersonId());
        assertEquals(interestId, result.getId().getInterestId());
        verify(personRepository).findByIdAndUserId(personId, userId);
        verify(interestRepository).findById(interestId);
        verify(personInterestRepository).save(any(PersonInterest.class));
    }

    @Test
    void shouldThrowResourceNotFoundWhenPersonNotFound() {
        // GIVEN
        when(personRepository.findByIdAndUserId(personId, userId))
                .thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(ResourceNotFoundException.class,
                () -> personInterestService.addInterest(personId, userId, interestId));
        verify(personRepository).findByIdAndUserId(personId, userId);
        verify(interestRepository, never()).findById(any());
        verify(personInterestRepository, never()).save(any());
    }

    @Test
    void shouldThrowResourceNotFoundWhenInterestNotFound() {
        // GIVEN
        when(personRepository.findByIdAndUserId(personId, userId))
                .thenReturn(Optional.of(testPerson));
        when(interestRepository.findById(interestId))
                .thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(ResourceNotFoundException.class,
                () -> personInterestService.addInterest(personId, userId, interestId));
        verify(personInterestRepository, never()).save(any());
    }

    @Test
    void shouldThrowDuplicateResourceExceptionWhenInterestExists() {
        // GIVEN
        when(personRepository.findByIdAndUserId(personId, userId))
                .thenReturn(Optional.of(testPerson));
        when(interestRepository.findById(interestId))
                .thenReturn(Optional.of(testInterest));
        when(personInterestRepository.existsByPersonIdAndInterestId(personId, interestId))
                .thenReturn(true);

        // WHEN & THEN
        assertThrows(DuplicateResourceException.class,
                () -> personInterestService.addInterest(personId, userId, interestId));
        verify(personInterestRepository, never()).save(any());
    }

    @Test
    void shouldValidateUserOwnershipWhenAddingInterest() {
        // GIVEN - Different userId
        UUID differentUserId = UUID.randomUUID();
        when(personRepository.findByIdAndUserId(personId, differentUserId))
                .thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(ResourceNotFoundException.class,
                () -> personInterestService.addInterest(personId, differentUserId, interestId));
        verify(personInterestRepository, never()).save(any());
    }

    @Test
    void shouldAddMultipleInterestsSuccessfully() {
        // GIVEN
        Set<UUID> interestIds = Set.of(interestId, interestId2);
        when(personRepository.findByIdAndUserId(personId, userId))
                .thenReturn(Optional.of(testPerson));
        when(interestRepository.findById(interestId))
                .thenReturn(Optional.of(testInterest));
        when(interestRepository.findById(interestId2))
                .thenReturn(Optional.of(testInterest2));
        when(personInterestRepository.existsByPersonIdAndInterestId(eq(personId), any()))
                .thenReturn(false);
        when(personInterestRepository.save(any(PersonInterest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        List<PersonInterest> result = personInterestService.addMultipleInterests(personId, userId, interestIds);

        // THEN
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(personInterestRepository, times(2)).save(any(PersonInterest.class));
    }

    @Test
    void shouldSkipDuplicateWhenAddingMultiple() {
        // GIVEN
        Set<UUID> interestIds = Set.of(interestId, interestId2);
        when(personRepository.findByIdAndUserId(personId, userId))
                .thenReturn(Optional.of(testPerson));
        when(interestRepository.findById(interestId))
                .thenReturn(Optional.of(testInterest));
        when(interestRepository.findById(interestId2))
                .thenReturn(Optional.of(testInterest2));

        // First interest already exists, second doesn't
        when(personInterestRepository.existsByPersonIdAndInterestId(personId, interestId))
                .thenReturn(true);
        when(personInterestRepository.existsByPersonIdAndInterestId(personId, interestId2))
                .thenReturn(false);
        when(personInterestRepository.save(any(PersonInterest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        List<PersonInterest> result = personInterestService.addMultipleInterests(personId, userId, interestIds);

        // THEN
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(personInterestRepository, times(1)).save(any(PersonInterest.class));
    }

    @Test
    void shouldGetPersonInterests() {
        // GIVEN
        when(personRepository.findByIdAndUserId(personId, userId))
                .thenReturn(Optional.of(testPerson));
        when(personInterestRepository.findByPersonId(personId))
                .thenReturn(List.of(testPersonInterest));

        // WHEN
        List<PersonInterest> result = personInterestService.getPersonInterests(personId, userId);

        // THEN
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(interestId, result.getFirst().getId().getInterestId());
        verify(personRepository).findByIdAndUserId(personId, userId);
    }

    @Test
    void shouldReturnEmptyListWhenPersonHasNoInterests() {
        // GIVEN
        when(personRepository.findByIdAndUserId(personId, userId))
                .thenReturn(Optional.of(testPerson));
        when(personInterestRepository.findByPersonId(personId))
                .thenReturn(List.of());

        // WHEN
        List<PersonInterest> result = personInterestService.getPersonInterests(personId, userId);

        // THEN
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldThrowExceptionWhenGettingPersonInterestsForNonExistentPerson() {
        // GIVEN
        when(personRepository.findByIdAndUserId(personId, userId))
                .thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(ResourceNotFoundException.class,
                () -> personInterestService.getPersonInterests(personId, userId));
        verify(personInterestRepository, never()).findByPersonId(any());
    }

    @Test
    void shouldGetInterestsForPerson() {
        // GIVEN
        when(personRepository.findByIdAndUserId(personId, userId))
                .thenReturn(Optional.of(testPerson));
        when(personInterestRepository.findByPersonId(personId))
                .thenReturn(List.of(testPersonInterest));

        // WHEN
        List<Interest> result = personInterestService.getInterestsForPerson(personId, userId);

        // THEN
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Reading", result.getFirst().getName());
    }

    @Test
    void shouldGetPersonsForInterest() {
        // GIVEN
        when(interestRepository.findById(interestId))
                .thenReturn(Optional.of(testInterest));
        when(personInterestRepository.findByInterestId(interestId))
                .thenReturn(List.of(testPersonInterest));

        // WHEN
        List<PersonInterest> result = personInterestService.getInterestPersons(interestId);

        // THEN
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(interestRepository).findById(interestId);
    }

    @Test
    void shouldThrowWhenGettingPersonsForNonExistentInterest() {
        // GIVEN
        when(interestRepository.findById(interestId))
                .thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(ResourceNotFoundException.class,
                () -> personInterestService.getInterestPersons(interestId));
    }

    @Test
    void shouldFilterPersonsByUserIdWhenGettingPersonsForInterest() {
        // GIVEN
        Person otherPerson = createTestPerson("Bob", UUID.randomUUID());
        otherPerson.setId(otherPersonId);

        PersonInterest otherPersonInterest = createTestPersonInterest(otherPersonId, interestId, otherPerson, testInterest);

        when(interestRepository.findById(interestId))
                .thenReturn(Optional.of(testInterest));
        when(personInterestRepository.findByInterestId(interestId))
                .thenReturn(List.of(testPersonInterest, otherPersonInterest));

        // WHEN
        List<Person> result = personInterestService.getPersonsForInterest(interestId, userId);

        // THEN
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(userId, result.getFirst().getUserId());
    }

    @Test
    void shouldGetAllByUser() {
        // GIVEN
        when(personInterestRepository.findAllByUserId(eq(userId)))
                .thenReturn(List.of(testPersonInterest));

        // WHEN
        List<PersonInterest> result = personInterestService.getAllByUser(userId);

        // THEN
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void shouldCheckIfPersonHasInterest() {
        // GIVEN
        when(personRepository.findByIdAndUserId(personId, userId))
                .thenReturn(Optional.of(testPerson));
        when(personInterestRepository.existsByPersonIdAndInterestId(personId, interestId))
                .thenReturn(true);

        // WHEN
        boolean result = personInterestService.hasInterest(personId, userId, interestId);

        // THEN
        assertTrue(result);
    }

    @Test
    void shouldReturnFalseWhenPersonDoesntHaveInterest() {
        // GIVEN
        when(personRepository.findByIdAndUserId(personId, userId))
                .thenReturn(Optional.of(testPerson));
        when(personInterestRepository.existsByPersonIdAndInterestId(personId, interestId))
                .thenReturn(false);

        // WHEN
        boolean result = personInterestService.hasInterest(personId, userId, interestId);

        // THEN
        assertFalse(result);
    }

    @Test
    void shouldReplaceAllInterests() {
        // GIVEN
        Set<UUID> newInterestIds = Set.of(interestId2);
        when(personRepository.findByIdAndUserId(personId, userId))
                .thenReturn(Optional.of(testPerson));
        when(personInterestRepository.findByPersonId(personId))
                .thenReturn(List.of(testPersonInterest));
        when(interestRepository.findById(interestId2))
                .thenReturn(Optional.of(testInterest2));
        when(personInterestRepository.save(any(PersonInterest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        personInterestService.replaceAllInterests(personId, userId, newInterestIds);

        // THEN
        verify(personInterestRepository).deleteAll(any());
        verify(personInterestRepository, times(1)).save(any(PersonInterest.class));
    }

    @Test
    void shouldThrowWhenReplacingInterestsForNonExistentPerson() {
        // GIVEN
        Set<UUID> newInterestIds = Set.of(interestId2);
        when(personRepository.findByIdAndUserId(personId, userId))
                .thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(ResourceNotFoundException.class,
                () -> personInterestService.replaceAllInterests(personId, userId, newInterestIds));
        verify(personInterestRepository, never()).deleteAll(any());
    }

    @Test
    void shouldRemoveInterest() {
        // GIVEN
        when(personRepository.findByIdAndUserId(personId, userId))
                .thenReturn(Optional.of(testPerson));
        when(personInterestRepository.existsByPersonIdAndInterestId(personId, interestId))
                .thenReturn(true);

        // WHEN
        personInterestService.removeInterest(personId, userId, interestId);

        // THEN
        verify(personInterestRepository).deleteByPersonIdAndInterestId(personId, interestId);
    }

    @Test
    void shouldThrowWhenRemovingNonExistentInterest() {
        // GIVEN
        when(personRepository.findByIdAndUserId(personId, userId))
                .thenReturn(Optional.of(testPerson));
        when(personInterestRepository.existsByPersonIdAndInterestId(personId, interestId))
                .thenReturn(false);

        // WHEN & THEN
        assertThrows(ResourceNotFoundException.class,
                () -> personInterestService.removeInterest(personId, userId, interestId));
        verify(personInterestRepository, never()).deleteByPersonIdAndInterestId(any(), any());
    }

    @Test
    void shouldRemoveAllInterests() {
        // GIVEN
        when(personRepository.findByIdAndUserId(personId, userId))
                .thenReturn(Optional.of(testPerson));
        when(personInterestRepository.findByPersonId(personId))
                .thenReturn(List.of(testPersonInterest));

        // WHEN
        personInterestService.removeAllInterests(personId, userId);

        // THEN
        verify(personInterestRepository).deleteAll(any());
    }

    @Test
    void shouldThrowWhenRemovingAllInterestsForNonExistentPerson() {
        // GIVEN
        when(personRepository.findByIdAndUserId(personId, userId))
                .thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(ResourceNotFoundException.class,
                () -> personInterestService.removeAllInterests(personId, userId));
        verify(personInterestRepository, never()).deleteAll(any());
    }

    @Test
    void shouldCountInterestsForPerson() {
        // GIVEN
        when(personRepository.findByIdAndUserId(personId, userId))
                .thenReturn(Optional.of(testPerson));
        when(personInterestRepository.findByPersonId(personId))
                .thenReturn(List.of(testPersonInterest));

        // WHEN
        long count = personInterestService.countInterestsForPerson(personId, userId);

        // THEN
        assertEquals(1L, count);
    }

    @Test
    void shouldCountPersonsWithInterest() {
        // GIVEN
        Person otherPerson = createTestPerson("Bob", UUID.randomUUID());
        otherPerson.setId(otherPersonId);

        PersonInterest otherPersonInterest = createTestPersonInterest(otherPersonId, interestId, otherPerson, testInterest);

        when(interestRepository.findById(interestId))
                .thenReturn(Optional.of(testInterest));
        when(personInterestRepository.findByInterestId(interestId))
                .thenReturn(List.of(testPersonInterest, otherPersonInterest));

        // WHEN
        long count = personInterestService.countPersonsWithInterest(interestId, userId);

        // THEN
        assertEquals(1L, count);
    }
}


