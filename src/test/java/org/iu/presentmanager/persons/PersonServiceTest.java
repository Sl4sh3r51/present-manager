package org.iu.presentmanager.persons;

import org.iu.presentmanager.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersonServiceTest {

    @Mock
    private PersonRepository personRepository;

    @InjectMocks
    private PersonService personService;

    // UUIDs for testing
    private final UUID userId = UUID.randomUUID();
    private final UUID personId = UUID.randomUUID();
    private Person testPerson;

    @BeforeEach
    void setUp() {
        testPerson = createTestPerson("Alice", PersonStatus.PLANNED, LocalDate.of(1990, 3, 15));
    }

    private Person createTestPerson(String name, PersonStatus status, LocalDate birthday) {
        Person person = new Person();
        person.setId(personId);
        person.setName(name);
        person.setUserId(userId);
        person.setStatus(status);
        person.setBirthday(birthday);
        return person;
    }

    @Test
    void shouldGetAllPersonsByUser() {
        // GIVEN
        when(personRepository.findByUserId(userId))
                .thenReturn(List.of(testPerson));

        // WHEN
        List<Person> result = personService.getAllPersonsByUser(userId);

        // THEN
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Alice", result.get(0).getName());
        verify(personRepository).findByUserId(userId);
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoPersons() {
        // GIVEN
        when(personRepository.findByUserId(userId))
                .thenReturn(Collections.emptyList());

        // WHEN
        List<Person> result = personService.getAllPersonsByUser(userId);

        // THEN
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(personRepository).findByUserId(userId);
    }

    @Test
    void shouldGetPersonsByStatus() {
        // GIVEN
        when(personRepository.findByUserIdAndStatus(userId, PersonStatus.PLANNED))
                .thenReturn(List.of(testPerson));

        // WHEN
        List<Person> result = personService.getPersonsByStatus(userId, PersonStatus.PLANNED);

        // THEN
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(PersonStatus.PLANNED, result.get(0).getStatus());
        verify(personRepository).findByUserIdAndStatus(userId, PersonStatus.PLANNED);
    }

    @Test
    void shouldReturnEmptyListWhenNoPersonsWithStatus() {
        // GIVEN
        when(personRepository.findByUserIdAndStatus(userId, PersonStatus.COMPLETED))
                .thenReturn(Collections.emptyList());

        // WHEN
        List<Person> result = personService.getPersonsByStatus(userId, PersonStatus.COMPLETED);

        // THEN
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(personRepository).findByUserIdAndStatus(userId, PersonStatus.COMPLETED);
    }

    @Test
    void shouldSupportMultipleStatusFilters() {
        // GIVEN
        Person person2 = createTestPerson("Bob", PersonStatus.IDEAS, LocalDate.of(1985, 6, 20));
        when(personRepository.findByUserIdAndStatus(userId, PersonStatus.IDEAS))
                .thenReturn(List.of(person2));

        // WHEN
        List<Person> result = personService.getPersonsByStatus(userId, PersonStatus.IDEAS);

        // THEN
        assertEquals(1, result.size());
        assertEquals(PersonStatus.IDEAS, result.get(0).getStatus());
        assertEquals("Bob", result.get(0).getName());
    }

    @Test
    void shouldGetPersonByIdWhenBelongsToUser() {
        // GIVEN
        when(personRepository.findById(personId))
                .thenReturn(Optional.of(testPerson));

        // WHEN
        Person result = personService.getPersonById(personId, userId);

        // THEN
        assertNotNull(result);
        assertEquals("Alice", result.getName());
        assertEquals(userId, result.getUserId());
        verify(personRepository).findById(personId);
    }

    @Test
    void shouldThrowExceptionWhenPersonNotFound() {
        // GIVEN
        when(personRepository.findById(personId))
                .thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(ResourceNotFoundException.class, () ->
                personService.getPersonById(personId, userId)
        );
        verify(personRepository).findById(personId);
    }

    @Test
    void shouldThrowExceptionWhenPersonBelongsToOtherUser() {
        // GIVEN
        UUID otherUserId = UUID.fromString("00000000-0000-0000-0000-000000000099");
        Person otherUsersPerson = createTestPerson("Charlie", PersonStatus.PLANNED, LocalDate.of(1995, 1, 1));
        otherUsersPerson.setUserId(otherUserId);

        when(personRepository.findById(personId))
                .thenReturn(Optional.of(otherUsersPerson));

        // WHEN & THEN
        assertThrows(ResourceNotFoundException.class, () ->
                personService.getPersonById(personId, userId)
        );
        verify(personRepository).findById(personId);
    }

    @Test
    void shouldCreatePerson() {
        // GIVEN
        Person newPerson = new Person();
        newPerson.setName("David");
        newPerson.setStatus(PersonStatus.PLANNED);
        newPerson.setUserId(userId);
        newPerson.setBirthday(LocalDate.of(1992, 7, 10));

        when(personRepository.save(any(Person.class)))
                .thenReturn(newPerson);

        // WHEN
        Person result = personService.createPerson(newPerson);

        // THEN
        assertNotNull(result);
        assertEquals("David", result.getName());
        assertEquals(userId, result.getUserId());
        verify(personRepository).save(newPerson);
    }

    @Test
    void shouldReturnIllegalArgumentExceptionWhenCreatingPersonWithEmptyName() {
        // GIVEN
        Person newPerson = new Person();
        newPerson.setName("");
        newPerson.setStatus(PersonStatus.PLANNED);
        newPerson.setUserId(userId);

        // When + THEN
        assertThrows(IllegalArgumentException.class, () ->
                personService.createPerson(newPerson)
        );
        verify(personRepository, never()).save(any());
    }

    @Test
    void shouldReturnIllegalArgumentExceptionWhenCreatingPersonWithNullName() {
        // GIVEN
        Person newPerson = new Person();
        newPerson.setName(null);
        newPerson.setStatus(PersonStatus.PLANNED);
        newPerson.setUserId(userId);

        // When + THEN
        assertThrows(IllegalArgumentException.class, () ->
                personService.createPerson(newPerson)
        );
        verify(personRepository, never()).save(any());
    }

    @Test
    void shouldReturnIllegalArgumentExceptionWhenCreatingPersonWithNullStatus(){
        // GIVEN
        Person newPerson = new Person();
        newPerson.setName("David");
        newPerson.setStatus(null);
        newPerson.setUserId(userId);

        // When + THEN
        assertThrows(IllegalArgumentException.class, () -> personService.createPerson(newPerson));
        verify(personRepository, never()).save(any());
    }

    @Test
    void shouldHandleRepositorySaveCorrectly() {
        // GIVEN
        Person newPerson = new Person();
        newPerson.setName("Eve");
        newPerson.setStatus(PersonStatus.IDEAS);
        newPerson.setUserId(userId);

        Person savedPerson = new Person();
        savedPerson.setId(UUID.randomUUID());
        savedPerson.setName("Eve");
        savedPerson.setStatus(PersonStatus.IDEAS);
        savedPerson.setUserId(userId);

        when(personRepository.save(any(Person.class)))
                .thenReturn(savedPerson);

        // WHEN
        Person result = personService.createPerson(newPerson);

        // THEN
        assertNotNull(result.getId());
        assertEquals("Eve", result.getName());
        verify(personRepository).save(any(Person.class));
    }

    @Test
    void shouldGetTodaysBirthdays() {
        // GIVEN
        LocalDate today = LocalDate.now();
        Person birthdayPerson = createTestPerson("Frank", PersonStatus.PLANNED,
                LocalDate.of(1990, today.getMonthValue(), today.getDayOfMonth()));

        when(personRepository.findUpcomingBirthdays(userId, today.getMonthValue(), today.getDayOfMonth()))
                .thenReturn(List.of(birthdayPerson));

        // WHEN
        List<Person> result = personService.getTodaysBirthdays(userId);

        // THEN
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Frank", result.get(0).getName());
        verify(personRepository).findUpcomingBirthdays(
                eq(userId),
                eq(today.getMonthValue()),
                eq(today.getDayOfMonth())
        );
    }

    @Test
    void shouldReturnEmptyListWhenNoBirthdaysToday() {
        // GIVEN
        LocalDate today = LocalDate.now();
        when(personRepository.findUpcomingBirthdays(userId, today.getMonthValue(), today.getDayOfMonth()))
                .thenReturn(Collections.emptyList());

        // WHEN
        List<Person> result = personService.getTodaysBirthdays(userId);

        // THEN
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldGetBirthdaysInMonth() {
        // GIVEN
        int march = 3;
        when(personRepository.findBirthdaysInMonth(userId, march))
                .thenReturn(List.of(testPerson));

        // WHEN
        List<Person> result = personService.getBirthdaysInMonth(userId, march);

        // THEN
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(3, result.get(0).getBirthday().getMonthValue());
        verify(personRepository).findBirthdaysInMonth(userId, march);
    }

    @Test
    void shouldReturnEmptyListForMonthWithoutBirthdays() {
        // GIVEN
        int december = 12;
        when(personRepository.findBirthdaysInMonth(userId, december))
                .thenReturn(Collections.emptyList());

        // WHEN
        List<Person> result = personService.getBirthdaysInMonth(userId, december);

        // THEN
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldSupportAllMonths() {
        // GIVEN
        for (int month = 1; month <= 12; month++) {
            when(personRepository.findBirthdaysInMonth(userId, month))
                    .thenReturn(Collections.emptyList());

            // WHEN
            List<Person> result = personService.getBirthdaysInMonth(userId, month);

            // THEN
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Test
    void shouldCountByStatus() {
        // GIVEN
        when(personRepository.countByUserIdAndStatus(userId, PersonStatus.PLANNED))
                .thenReturn(5L);

        // WHEN
        long result = personService.countByStatus(userId, PersonStatus.PLANNED);

        // THEN
        assertEquals(5L, result);
        verify(personRepository).countByUserIdAndStatus(userId, PersonStatus.PLANNED);
    }

    @Test
    void shouldReturnZeroCountWhenNoPersonsWithStatus() {
        // GIVEN
        when(personRepository.countByUserIdAndStatus(userId, PersonStatus.COMPLETED))
                .thenReturn(0L);

        // WHEN
        long result = personService.countByStatus(userId, PersonStatus.COMPLETED);

        // THEN
        assertEquals(0L, result);
    }

    @Test
    void shouldHandleLargeCounts() {
        // GIVEN
        long largeCount = 1000000L;
        when(personRepository.countByUserIdAndStatus(userId, PersonStatus.IDEAS))
                .thenReturn(largeCount);

        // WHEN
        long result = personService.countByStatus(userId, PersonStatus.IDEAS);

        // THEN
        assertEquals(largeCount, result);
    }

    @Test
    void shouldDeletePerson() {
        // GIVEN
        when(personRepository.findById(personId))
                .thenReturn(Optional.of(testPerson));
        doNothing().when(personRepository).delete(testPerson);

        // WHEN
        personService.deletePerson(personId, userId);

        // THEN
        verify(personRepository).findById(personId);
        verify(personRepository).delete(testPerson);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentPerson() {
        // GIVEN
        when(personRepository.findById(personId))
                .thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(ResourceNotFoundException.class, () ->
                personService.deletePerson(personId, userId)
        );
        verify(personRepository, never()).delete(any());
    }

    @Test
    void shouldThrowExceptionWhenDeletingOtherUsersPerson() {
        // GIVEN
        UUID otherUserId = UUID.fromString("00000000-0000-0000-0000-000000000099");
        Person otherUsersPerson = createTestPerson("Helen", PersonStatus.PLANNED, LocalDate.of(1995, 1, 1));
        otherUsersPerson.setUserId(otherUserId);

        when(personRepository.findById(personId))
                .thenReturn(Optional.of(otherUsersPerson));

        // WHEN & THEN
        assertThrows(ResourceNotFoundException.class, () ->
                personService.deletePerson(personId, userId)
        );
        verify(personRepository, never()).delete(any());
    }

    @Test
    void shouldCallRepositoryDeleteAfterValidation() {
        // GIVEN
        when(personRepository.findById(personId))
                .thenReturn(Optional.of(testPerson));
        doNothing().when(personRepository).delete(any());

        // WHEN
        personService.deletePerson(personId, userId);

        // THEN
        verify(personRepository).delete(testPerson);
    }
}
