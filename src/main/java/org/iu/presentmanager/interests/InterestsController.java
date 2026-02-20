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
public class InterestsController {

    private final InterestsService interestsService;

    @GetMapping
    public ResponseEntity<List<Interests>> findAllInterests() {
        List<Interests> foundInterests = interestsService.getAllInterests();
        return ResponseEntity.ok(foundInterests);
    }

    @GetMapping("/search")
    public ResponseEntity<Interests> findInterestsByName(@RequestParam @NotBlank String name) {
        Interests foundInterests = interestsService.getInterestsByName(name);
        return ResponseEntity.ok(foundInterests);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Interests> findInterestsById(@PathVariable @NotBlank UUID id) {
        Interests foundInterests = interestsService.getInterestsById(id);
        return ResponseEntity.ok(foundInterests);
    }

    @PostMapping
    public ResponseEntity<Interests> createInterests(@RequestBody @NotBlank Interests interests) {
        Interests createdInterests = interestsService.createInterests(interests);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdInterests);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInterests(@PathVariable @NotBlank UUID id) {
        interestsService.deleteInterests(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/exists")
    public ResponseEntity<Boolean> checkInterestExists(@RequestParam String name) {
        boolean exists = interestsService.existsByName(name);
        return ResponseEntity.ok(exists);
    }

}
