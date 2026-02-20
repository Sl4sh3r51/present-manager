package org.iu.presentmanager.person_interests;

import lombok.RequiredArgsConstructor;
import org.iu.presentmanager.interests.Interests;
import org.iu.presentmanager.persons.Person;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/person-interests")
@RequiredArgsConstructor
public class Person_InterestsController {

    private final Person_InterestsService personInterestsService;

    @PostMapping
    public ResponseEntity<Person_Interests> addInterest(
            @RequestParam UUID personId,
            @RequestParam UUID interestId,
            @AuthenticationPrincipal UUID userId
    ) {
        Person_Interests created = personInterestsService.addInterest(personId, userId, interestId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/batch")
    public ResponseEntity<List<Person_Interests>> addMultipleInterests(
            @RequestParam UUID personId,
            @RequestBody Set<UUID> interestIds,
            @AuthenticationPrincipal UUID userId
    ) {
        List<Person_Interests> created = personInterestsService.addMultipleInterests(personId, userId, interestIds);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<Person_Interests>> getPersonInterests(
            @RequestParam UUID personId,
            @AuthenticationPrincipal UUID userId
    ) {
        List<Person_Interests> personInterests = personInterestsService.getPersonInterests(personId, userId);
        return ResponseEntity.ok(personInterests);
    }

    @GetMapping("/interests")
    public ResponseEntity<List<Interests>> getInterestsForPerson(
            @RequestParam UUID personId,
            @AuthenticationPrincipal UUID userId
    ) {
        List<Interests> interests = personInterestsService.getInterestsForPerson(personId, userId);
        return ResponseEntity.ok(interests);
    }

    @GetMapping("/persons")
    public ResponseEntity<List<Person>> getPersonsForInterest(
            @RequestParam UUID interestId,
            @AuthenticationPrincipal UUID userId
    ) {
        List<Person> persons = personInterestsService.getPersonsForInterest(interestId, userId);
        return ResponseEntity.ok(persons);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Person_Interests>> getAllByUser(
            @AuthenticationPrincipal UUID userId
    ) {
        List<Person_Interests> all = personInterestsService.getAllByUser(userId);
        return ResponseEntity.ok(all);
    }

    @GetMapping("/check")
    public ResponseEntity<Boolean> hasInterest(
            @RequestParam UUID personId,
            @RequestParam UUID interestId,
            @AuthenticationPrincipal UUID userId
    ) {
        boolean has = personInterestsService.hasInterest(personId, userId, interestId);
        return ResponseEntity.ok(has);
    }

    @PutMapping("/replace")
    public ResponseEntity<Void> replaceAllInterests(
            @RequestParam UUID personId,
            @RequestBody Set<UUID> interestIds,
            @AuthenticationPrincipal UUID userId
    ) {
        personInterestsService.replaceAllInterests(personId, userId, interestIds);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> removeInterest(
            @RequestParam UUID personId,
            @RequestParam UUID interestId,
            @AuthenticationPrincipal UUID userId
    ) {
        personInterestsService.removeInterest(personId, userId, interestId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/all")
    public ResponseEntity<Void> removeAllInterests(
            @RequestParam UUID personId,
            @AuthenticationPrincipal UUID userId
    ) {
        personInterestsService.removeAllInterests(personId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count/person")
    public ResponseEntity<Long> countInterestsForPerson(
            @RequestParam UUID personId,
            @AuthenticationPrincipal UUID userId
    ) {
        long count = personInterestsService.countInterestsForPerson(personId, userId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/count/interest")
    public ResponseEntity<Long> countPersonsWithInterest(
            @RequestParam UUID interestId,
            @AuthenticationPrincipal UUID userId
    ) {
        long count = personInterestsService.countPersonsWithInterest(interestId, userId);
        return ResponseEntity.ok(count);
    }
}
