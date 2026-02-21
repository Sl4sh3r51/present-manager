package org.iu.presentmanager.interests;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/interests")
@RequiredArgsConstructor
public class InterestController {

    private final InterestService interestService;

    @GetMapping
    public ResponseEntity<List<Interest>> findAllInterests() {
        List<Interest> foundInterests = interestService.getAllInterests();
        return ResponseEntity.ok(foundInterests);
    }

    @GetMapping("/search")
    public ResponseEntity<Interest> findInterestsByName(@RequestParam @NotBlank String name) {
        Interest foundInterest = interestService.getInterestsByName(name);
        return ResponseEntity.ok(foundInterest);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Interest> findInterestsById(@PathVariable @NotBlank UUID id) {
        Interest foundInterest = interestService.getInterestsById(id);
        return ResponseEntity.ok(foundInterest);
    }

    @PostMapping
    public ResponseEntity<Interest> createInterests(@RequestBody @NotBlank Interest interest) {
        Interest createdInterest = interestService.createInterests(interest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdInterest);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInterests(@PathVariable @NotBlank UUID id) {
        interestService.deleteInterests(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/exists")
    public ResponseEntity<Boolean> checkInterestExists(@RequestParam String name) {
        boolean exists = interestService.existsByName(name);
        return ResponseEntity.ok(exists);
    }

}
