package org.iu.presentmanager.occasions;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/occasions")
@RequiredArgsConstructor
public class OccasionController {

    private final OccasionService occasionService;

    @PostMapping
    public ResponseEntity<Occasion> createOccasion(
            @Valid @RequestBody Occasion occasion,
            @AuthenticationPrincipal UUID userId
    ) {
        Occasion created = occasionService.createOccasion(occasion, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<Occasion>> getAllOccasions(
            @AuthenticationPrincipal UUID userId,
            @RequestParam(required = false) OccasionType type,
            @RequestParam(required = false) Boolean isRecurring
    ) {
        List<Occasion> occasions;

        if (type != null) {
            occasions = occasionService.getOccasionsByType(userId, type);
        } else if (isRecurring != null) {
            occasions = occasionService.getRecurringOccasions(userId, isRecurring);
        } else {
            occasions = occasionService.getAllOccasionsByUser(userId);
        }

        return ResponseEntity.ok(occasions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Occasion> getOccasionById(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID userId
    ) {
        Occasion occasion = occasionService.getOccasionById(id, userId);
        return ResponseEntity.ok(occasion);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Occasion>> searchOccasions(
            @RequestParam String query,
            @AuthenticationPrincipal UUID userId
    ) {
        List<Occasion> occasions = occasionService.searchOccasionsByName(userId, query);
        return ResponseEntity.ok(occasions);
    }

    @GetMapping("/fixed/month/{month}")
    public ResponseEntity<List<Occasion>> getFixedOccasionsByMonth(
            @PathVariable int month,
            @AuthenticationPrincipal UUID userId
    ) {
        List<Occasion> occasions = occasionService.getFixedOccasionsByMonth(userId, month);
        return ResponseEntity.ok(occasions);
    }

    @GetMapping("/fixed/today")
    public ResponseEntity<List<Occasion>> getTodayFixedOccasions(
            @AuthenticationPrincipal UUID userId
    ) {
        List<Occasion> occasions = occasionService.getTodayFixedOccasions(userId);
        return ResponseEntity.ok(occasions);
    }

    @GetMapping("/fixed/recurring")
    public ResponseEntity<List<Occasion>> getRecurringFixedOccasions(
            @AuthenticationPrincipal UUID userId
    ) {
        List<Occasion> occasions = occasionService.getRecurringFixedOccasions(userId);
        return ResponseEntity.ok(occasions);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Occasion> updateOccasion(
            @PathVariable UUID id,
            @Valid @RequestBody Occasion occasion,
            @AuthenticationPrincipal UUID userId
    ) {
        Occasion updated = occasionService.updateOccasion(id, userId, occasion);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOccasion(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID userId
    ) {
        occasionService.deleteOccasion(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats/count")
    public ResponseEntity<Long> countOccasions(
            @AuthenticationPrincipal UUID userId,
            @RequestParam(required = false) OccasionType type
    ) {
        long count = type != null
                ? occasionService.countOccasionsByType(userId, type)
                : occasionService.countOccasionsByUser(userId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/exists")
    public ResponseEntity<Boolean> checkOccasionExists(
            @RequestParam String name,
            @AuthenticationPrincipal UUID userId
    ) {
        boolean exists = occasionService.existsByName(userId, name);
        return ResponseEntity.ok(exists);
    }
}
