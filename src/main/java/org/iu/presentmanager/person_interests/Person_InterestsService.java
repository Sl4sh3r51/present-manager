package org.iu.presentmanager.person_interests;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iu.presentmanager.exceptions.DuplicateResourceException;
import org.iu.presentmanager.exceptions.ResourceNotFoundException;
import org.iu.presentmanager.interests.Interests;
import org.iu.presentmanager.interests.InterestsRepository;
import org.iu.presentmanager.persons.Person;
import org.iu.presentmanager.persons.PersonRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class Person_InterestsService {

    private final Person_InterestsRepository personInterestsRepository;
    private final InterestsRepository interestsRepository;
    private final PersonRepository personRepository;

    @Transactional
    public Person_Interests addInterest(UUID personId, UUID userId, UUID interestId) {

        Person person = personRepository.findByIdAndUserId(personId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Person not found with id: " + personId));


        Interests interest = interestsRepository.findById(interestId)
                .orElseThrow(() -> new ResourceNotFoundException("Interest not found with id: " + interestId));


        if (personInterestsRepository.existsByPersonIdAndInterestsId(personId, interestId)) {
            throw new DuplicateResourceException(
                    "Person " + personId + " already has interest " + interestId
            );
        }

        Person_Interests personInterest = new Person_Interests();
        personInterest.setId(new PersonInterestsId(personId, interestId));
        personInterest.setPerson(person);
        personInterest.setInterests(interest);

        Person_Interests saved = personInterestsRepository.save(personInterest);
        log.info("Added interest '{}' to person '{}' (user: {})",
                interest.getName(), person.getName(), userId);

        return saved;
    }

    @Transactional
    public List<Person_Interests> addMultipleInterests(UUID personId, UUID userId, Set<UUID> interestIds) {

        Person person = personRepository.findByIdAndUserId(personId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Person not found with id: " + personId));

        return interestIds.stream()
                .map(interestId -> {
                    try {
                        return addInterest(person.getId(), person.getUserId(), interestId);
                    } catch (DuplicateResourceException e) {
                        log.warn("Skipping duplicate interest: {}", interestId);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<Person_Interests> getPersonInterests(UUID personId, UUID userId) {
        // Validierung: Person gehört dem User
        personRepository.findByIdAndUserId(personId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Person not found with id: " + personId));

        return personInterestsRepository.findByPersonId(personId);
    }

    public List<Interests> getInterestsForPerson(UUID personId, UUID userId) {
        List<Person_Interests> personInterests = getPersonInterests(personId, userId);
        return personInterests.stream()
                .map(Person_Interests::getInterests)
                .collect(Collectors.toList());
    }

    public List<Person_Interests> getInterestPersons(UUID interestId) {
        // Validierung: Interest existiert
        interestsRepository.findById(interestId)
                .orElseThrow(() -> new ResourceNotFoundException("Interest not found with id: " + interestId));

        return personInterestsRepository.findByInterestsId(interestId);
    }

    public List<Person> getPersonsForInterest(UUID interestId, UUID userId) {
        List<Person_Interests> personInterests = getInterestPersons(interestId);

        // Nur Personen des Users zurückgeben
        return personInterests.stream()
                .map(Person_Interests::getPerson)
                .filter(person -> person.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    public List<Person_Interests> getAllByUser(UUID userId) {
        return personInterestsRepository.findByUserIdAndPersonId(userId, null);
    }

    public boolean hasInterest(UUID personId, UUID userId, UUID interestId) {
        // Validierung: Person gehört dem User
        personRepository.findByIdAndUserId(personId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Person not found with id: " + personId));

        return personInterestsRepository.existsByPersonIdAndInterestsId(personId, interestId);
    }

    @Transactional
    public void replaceAllInterests(UUID personId, UUID userId, Set<UUID> interestIds) {
        // Validierung: Person gehört dem User
        Person person = personRepository.findByIdAndUserId(personId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Person not found with id: " + personId));

        // Alle alten Beziehungen löschen
        List<Person_Interests> existing = personInterestsRepository.findByPersonId(personId);
        personInterestsRepository.deleteAll(existing);

        log.info("Removed {} existing interests for person {}", existing.size(), personId);

        // Neue Beziehungen erstellen
        for (UUID interestId : interestIds) {
            Interests interest = interestsRepository.findById(interestId)
                    .orElseThrow(() -> new ResourceNotFoundException("Interest not found with id: " + interestId));

            Person_Interests personInterest = new Person_Interests();
            personInterest.setId(new PersonInterestsId(personId, interestId));
            personInterest.setPerson(person);
            personInterest.setInterests(interest);

            personInterestsRepository.save(personInterest);
        }

        log.info("Set {} new interests for person '{}' (user: {})",
                interestIds.size(), person.getName(), userId);
    }

    @Transactional
    public void removeInterest(UUID personId, UUID userId, UUID interestId) {
        // Validierung: Person gehört dem User
        personRepository.findByIdAndUserId(personId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Person not found with id: " + personId));

        // Prüfen ob Beziehung existiert
        if (!personInterestsRepository.existsByPersonIdAndInterestsId(personId, interestId)) {
            throw new ResourceNotFoundException(
                    "Person " + personId + " does not have interest " + interestId
            );
        }

        personInterestsRepository.deleteByPersonIdAndInterestsId(personId, interestId);
        log.info("Removed interest {} from person {} (user: {})", interestId, personId, userId);
    }

    @Transactional
    public void removeAllInterests(UUID personId, UUID userId) {
        // Validierung: Person gehört dem User
        personRepository.findByIdAndUserId(personId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Person not found with id: " + personId));

        List<Person_Interests> existing = personInterestsRepository.findByPersonId(personId);
        personInterestsRepository.deleteAll(existing);

        log.info("Removed all {} interests from person {} (user: {})",
                existing.size(), personId, userId);
    }

    public long countInterestsForPerson(UUID personId, UUID userId) {
        personRepository.findByIdAndUserId(personId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Person not found with id: " + personId));

        return personInterestsRepository.findByPersonId(personId).size();
    }

    public long countPersonsWithInterest(UUID interestId, UUID userId) {
        return getPersonsForInterest(interestId, userId).size();
    }
}
