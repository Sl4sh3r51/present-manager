package org.iu.presentmanager.personinterests;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iu.presentmanager.exceptions.DuplicateResourceException;
import org.iu.presentmanager.exceptions.ResourceNotFoundException;
import org.iu.presentmanager.interests.Interest;
import org.iu.presentmanager.interests.InterestRepository;
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
public class PersonInterestService {

    private final PersonInterestRepository personInterestRepository;
    private final InterestRepository interestRepository;
    private final PersonRepository personRepository;

    @Transactional
    public PersonInterest addInterest(UUID personId, UUID userId, UUID interestId) {

        Person person = personRepository.findByIdAndUserId(personId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Person not found with id: " + personId));


        final Interest interest = interestRepository.findById(interestId)
                .orElseThrow(() -> new ResourceNotFoundException("Interest not found with id: " + interestId));


        if (personInterestRepository.existsByPersonIdAndInterestId(personId, interestId)) {
            throw new DuplicateResourceException(
                    "Person " + personId + " already has interest " + interestId
            );
        }

        PersonInterest personInterest = new PersonInterest();
        personInterest.setId(new PersonInterestId(personId, interestId));
        personInterest.setPerson(person);
        personInterest.setInterest(interest);

        PersonInterest saved = personInterestRepository.save(personInterest);
        log.info("Added interest '{}' to person '{}' (user: {})",
                interest.getName(), person.getName(), userId);

        return saved;
    }

    @Transactional
    public List<PersonInterest> addMultipleInterests(UUID personId, UUID userId, Set<UUID> interestIds) {

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

    public List<PersonInterest> getPersonInterests(UUID personId, UUID userId) {
        // Validierung: Person gehört dem User
        personRepository.findByIdAndUserId(personId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Person not found with id: " + personId));

        return personInterestRepository.findByPersonId(personId);
    }

    public List<Interest> getInterestsForPerson(UUID personId, UUID userId) {
        List<PersonInterest> personInterests = getPersonInterests(personId, userId);
        return personInterests.stream()
                .map(PersonInterest::getInterest)
                .collect(Collectors.toList());
    }

    public List<PersonInterest> getInterestPersons(UUID interestId) {
        // Validierung: Interest existiert
        interestRepository.findById(interestId)
                .orElseThrow(() -> new ResourceNotFoundException("Interest not found with id: " + interestId));

        return personInterestRepository.findByInterestId(interestId);
    }

    public List<Person> getPersonsForInterest(UUID interestId, UUID userId) {
        List<PersonInterest> personInterests = getInterestPersons(interestId);

        // Nur Personen des Users zurückgeben
        return personInterests.stream()
                .map(PersonInterest::getPerson)
                .filter(person -> person.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    public List<PersonInterest> getAllByUser(UUID userId) {
        return personInterestRepository.findAllByUserId(userId);
    }

    public boolean hasInterest(UUID personId, UUID userId, UUID interestId) {
        // Validierung: Person gehört dem User
        personRepository.findByIdAndUserId(personId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Person not found with id: " + personId));

        return personInterestRepository.existsByPersonIdAndInterestId(personId, interestId);
    }

    @Transactional
    public void replaceAllInterests(UUID personId, UUID userId, Set<UUID> interestIds) {
        // Validierung: Person gehört dem User
        Person person = personRepository.findByIdAndUserId(personId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Person not found with id: " + personId));

        // Alle alten Beziehungen löschen
        List<PersonInterest> existing = personInterestRepository.findByPersonId(personId);
        personInterestRepository.deleteAll(existing);

        log.info("Removed {} existing interests for person {}", existing.size(), personId);

        // Neue Beziehungen erstellen
        for (UUID interestId : interestIds) {
            Interest interest = interestRepository.findById(interestId)
                    .orElseThrow(() -> new ResourceNotFoundException("Interest not found with id: " + interestId));

            PersonInterest personInterest = new PersonInterest();
            personInterest.setId(new PersonInterestId(personId, interestId));
            personInterest.setPerson(person);
            personInterest.setInterest(interest);

            personInterestRepository.save(personInterest);
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
        if (!personInterestRepository.existsByPersonIdAndInterestId(personId, interestId)) {
            throw new ResourceNotFoundException(
                    "Person " + personId + " does not have interest " + interestId
            );
        }

        personInterestRepository.deleteByPersonIdAndInterestId(personId, interestId);
        log.info("Removed interest {} from person {} (user: {})", interestId, personId, userId);
    }

    @Transactional
    public void removeAllInterests(UUID personId, UUID userId) {
        // Validierung: Person gehört dem User
        personRepository.findByIdAndUserId(personId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Person not found with id: " + personId));

        List<PersonInterest> existing = personInterestRepository.findByPersonId(personId);
        personInterestRepository.deleteAll(existing);

        log.info("Removed all {} interests from person {} (user: {})",
                existing.size(), personId, userId);
    }

    public long countInterestsForPerson(UUID personId, UUID userId) {
        personRepository.findByIdAndUserId(personId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Person not found with id: " + personId));

        return personInterestRepository.findByPersonId(personId).size();
    }

    public long countPersonsWithInterest(UUID interestId, UUID userId) {
        return getPersonsForInterest(interestId, userId).size();
    }
}
