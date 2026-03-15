package org.iu.presentmanager.persons;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/persons")
@RequiredArgsConstructor
public class PersonController {

    private final PersonService personService;

    @GetMapping
    public ResponseEntity<List<Person>> getAllPersons(
            @AuthenticationPrincipal UUID userId,
            @RequestParam(required = false) PersonStatus status
    ) {
        List<Person> persons = status != null
                ? personService.getPersonsByStatus(userId, status)
                : personService.getAllPersonsByUser(userId);

        return ResponseEntity.ok(persons);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Person> getPersonById(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID userId
    ) {
        Person person = personService.getPersonById(id, userId);
        return ResponseEntity.ok(person);
    }

    @PostMapping
    public ResponseEntity<Person> createPerson(
            @Valid @RequestBody Person person,
            @AuthenticationPrincipal UUID userId
    ) {
        Person created = personService.createPerson(person, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Person> updatePerson(
            @PathVariable UUID id,
            @Valid @RequestBody Person person,
            @AuthenticationPrincipal UUID userId){
        Person updated = personService.updatePerson(id, person, userId);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePerson(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID userId
    ) {
        personService.deletePerson(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/birthdays/today")
    public ResponseEntity<List<Person>> getTodaysBirthdays(
            @AuthenticationPrincipal UUID userId
    ) {
        List<Person> persons = personService.getTodaysBirthdays(userId);
        return ResponseEntity.ok(persons);
    }

    @GetMapping("/birthdays/month/{month}")
    public ResponseEntity<List<Person>> getBirthdaysInMonth(
            @PathVariable int month,
            @AuthenticationPrincipal UUID userId
    ) {
        List<Person> persons = personService.getBirthdaysInMonth(userId, month);
        return ResponseEntity.ok(persons);
    }

    @GetMapping("/stats/count")
    public ResponseEntity<Long> countByStatus(
            @AuthenticationPrincipal UUID userId,
            @RequestParam PersonStatus status
    ) {
        long count = personService.countByStatus(userId, status);
        return ResponseEntity.ok(count);
    }
}
