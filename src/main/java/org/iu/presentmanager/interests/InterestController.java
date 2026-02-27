package org.iu.presentmanager.interests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    public ResponseEntity<Interest> findInterestByName(@RequestParam @NotBlank String name) {
        Interest foundInterest = interestService.getInterestByName(name);
        return ResponseEntity.ok(foundInterest);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Interest> findInterestById(@PathVariable UUID id) {
        Interest foundInterest = interestService.getInterestById(id);
        return ResponseEntity.ok(foundInterest);
    }

    @PostMapping
    public ResponseEntity<Interest> createInterest(@RequestBody @NotNull Interest interest) {
        Interest createdInterest = interestService.createInterest(interest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdInterest);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInterest(@PathVariable UUID id) {
        interestService.deleteInterest(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/exists")
    public ResponseEntity<Boolean> checkInterestExists(@RequestParam String name) {
        boolean exists = interestService.existsByName(name);
        return ResponseEntity.ok(exists);
    }

}
