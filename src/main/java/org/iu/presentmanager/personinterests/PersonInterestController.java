package org.iu.presentmanager.personinterests;

import lombok.RequiredArgsConstructor;
import org.iu.presentmanager.interests.Interest;
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
public class PersonInterestController {

    private final PersonInterestService personInterestService;

    @PostMapping
    public ResponseEntity<PersonInterest> addInterest(
            @RequestParam UUID personId,
            @RequestParam UUID interestId,
            @AuthenticationPrincipal UUID userId
    ) {
        PersonInterest created = personInterestService.addInterest(personId, userId, interestId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/batch")
    public ResponseEntity<List<PersonInterest>> addMultipleInterests(
            @RequestParam UUID personId,
            @RequestBody Set<UUID> interestIds,
            @AuthenticationPrincipal UUID userId
    ) {
        List<PersonInterest> created = personInterestService.addMultipleInterests(personId, userId, interestIds);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<PersonInterest>> getPersonInterests(
            @RequestParam UUID personId,
            @AuthenticationPrincipal UUID userId
    ) {
        List<PersonInterest> personInterests = personInterestService.getPersonInterests(personId, userId);
        return ResponseEntity.ok(personInterests);
    }

    @GetMapping("/interests")
    public ResponseEntity<List<Interest>> getInterestsForPerson(
            @RequestParam UUID personId,
            @AuthenticationPrincipal UUID userId
    ) {
        List<Interest> interests = personInterestService.getInterestsForPerson(personId, userId);
        return ResponseEntity.ok(interests);
    }

    @GetMapping("/persons")
    public ResponseEntity<List<Person>> getPersonsForInterest(
            @RequestParam UUID interestId,
            @AuthenticationPrincipal UUID userId
    ) {
        List<Person> persons = personInterestService.getPersonsForInterest(interestId, userId);
        return ResponseEntity.ok(persons);
    }

    @GetMapping("/all")
    public ResponseEntity<List<PersonInterest>> getAllByUser(
            @AuthenticationPrincipal UUID userId
    ) {
        List<PersonInterest> all = personInterestService.getAllByUser(userId);
        return ResponseEntity.ok(all);
    }

    @GetMapping("/check")
    public ResponseEntity<Boolean> hasInterest(
            @RequestParam UUID personId,
            @RequestParam UUID interestId,
            @AuthenticationPrincipal UUID userId
    ) {
        boolean has = personInterestService.hasInterest(personId, userId, interestId);
        return ResponseEntity.ok(has);
    }

    @PutMapping("/replace")
    public ResponseEntity<Void> replaceAllInterests(
            @RequestParam UUID personId,
            @RequestBody Set<UUID> interestIds,
            @AuthenticationPrincipal UUID userId
    ) {
        personInterestService.replaceAllInterests(personId, userId, interestIds);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> removeInterest(
            @RequestParam UUID personId,
            @RequestParam UUID interestId,
            @AuthenticationPrincipal UUID userId
    ) {
        personInterestService.removeInterest(personId, userId, interestId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/all")
    public ResponseEntity<Void> removeAllInterests(
            @RequestParam UUID personId,
            @AuthenticationPrincipal UUID userId
    ) {
        personInterestService.removeAllInterests(personId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count/person")
    public ResponseEntity<Long> countInterestsForPerson(
            @RequestParam UUID personId,
            @AuthenticationPrincipal UUID userId
    ) {
        long count = personInterestService.countInterestsForPerson(personId, userId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/count/interest")
    public ResponseEntity<Long> countPersonsWithInterest(
            @RequestParam UUID interestId,
            @AuthenticationPrincipal UUID userId
    ) {
        long count = personInterestService.countPersonsWithInterest(interestId, userId);
        return ResponseEntity.ok(count);
    }
}
