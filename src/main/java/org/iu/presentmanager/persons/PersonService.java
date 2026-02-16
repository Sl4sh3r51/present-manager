package org.iu.presentmanager.persons;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iu.presentmanager.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PersonService {

    private final PersonRepository personRepository;

    public List<Person> getAllPersonsByUser(UUID userId) {
        return personRepository.findByUserId(userId);
    }

    public List<Person> getPersonsByStatus(UUID userId, PersonStatus status) {
        return personRepository.findByUserIdAndStatus(userId, status);
    }

    public Person getPersonById(UUID id, UUID userId) {
        return personRepository.findById(id)
                .filter(person -> person.getUserId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Person not found"));
    }

    @Transactional
    public Person createPerson(Person person) {
        log.info("Creating person: {} for user: {}", person.getName(), person.getUserId());
        return personRepository.save(person);
    }

    public List<Person> getTodaysBirthdays(UUID userId) {
        LocalDate today = LocalDate.now();
        return personRepository.findUpcomingBirthdays(
                userId,
                today.getMonthValue(),
                today.getDayOfMonth()
        );
    }

    public List<Person> getBirthdaysInMonth(UUID userId, int month) {
        return personRepository.findBirthdaysInMonth(userId, month);
    }

    public long countByStatus(UUID userId, PersonStatus status) {
        return personRepository.countByUserIdAndStatus(userId, status);
    }

    @Transactional
    public void deletePerson(UUID id, UUID userId) {
        Person person = getPersonById(id, userId);
        personRepository.delete(person);
        log.info("Deleted person: {} for user: {}", id, userId);
    }
}
