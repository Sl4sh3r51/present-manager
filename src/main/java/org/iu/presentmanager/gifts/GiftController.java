package org.iu.presentmanager.gifts;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.iu.presentmanager.giftIdeas.GiftIdea;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("gifts")
@RequiredArgsConstructor
public class GiftController {

    private final GiftService giftService;

    @PostMapping
    public ResponseEntity<Gift> createGift(@Valid @RequestBody Gift gift, @AuthenticationPrincipal UUID userId) {
        Gift created = giftService.createGift(gift, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/gift-idea")
    public ResponseEntity<Gift> createGiftFromGiftIdea(@Valid @RequestBody GiftIdea giftIdea, @AuthenticationPrincipal UUID userId) {
        Gift created = giftService.createGiftFromGiftIdea(giftIdea, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<Gift>> getAllGifts(
            @AuthenticationPrincipal UUID userId,
            @RequestParam(required = false) UUID personId,
            @RequestParam(required = false) UUID occasionId,
            @RequestParam(required = false) GiftStatus status,
            @RequestParam(required = false) UUID giftIdeaId
    ) {
        List<Gift> gifts;

        if (personId != null && occasionId != null) {
            gifts = giftService.getGiftsByPersonAndOccasion(userId, personId, occasionId);
        } else if (personId != null && status != null) {
            gifts = giftService.getGiftsByPersonAndStatus(userId, personId, status);
        } else if (personId != null) {
            gifts = giftService.getGiftsByPerson(userId, personId);
        } else if (occasionId != null) {
            gifts = giftService.getGiftsByOccasion(userId, occasionId);
        } else if (status != null) {
            gifts = giftService.getGiftsByStatus(userId, status);
        } else if (giftIdeaId != null) {
            gifts = giftService.getGiftsByGiftIdea(userId, giftIdeaId);
        } else {
            gifts = giftService.getAllGiftsByUser(userId);
        }

        return ResponseEntity.ok(gifts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Gift> getGiftById(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID userId
    ) {
        Gift gift = giftService.getGiftById(id, userId);
        return ResponseEntity.ok(gift);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Gift>> searchGifts(
            @RequestParam String query,
            @AuthenticationPrincipal UUID userId
    ) {
        List<Gift> gifts = giftService.searchGifts(userId, query);
        return ResponseEntity.ok(gifts);
    }

    @GetMapping("/not-purchased")
    public ResponseEntity<List<Gift>> getNotPurchasedGifts(
            @AuthenticationPrincipal UUID userId
    ) {
        List<Gift> gifts = giftService.getNotPurchasedGifts(userId);
        return ResponseEntity.ok(gifts);
    }

    @GetMapping("/not-given")
    public ResponseEntity<List<Gift>> getNotGivenGifts(
            @AuthenticationPrincipal UUID userId
    ) {
        List<Gift> gifts = giftService.getNotGivenGifts(userId);
        return ResponseEntity.ok(gifts);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<Gift>> getRecentGifts(
            @AuthenticationPrincipal UUID userId
    ) {
        List<Gift> gifts = giftService.getRecentGifts(userId);
        return ResponseEntity.ok(gifts);
    }

    @GetMapping("/workflow-order")
    public ResponseEntity<List<Gift>> getGiftsByWorkflowOrder(
            @AuthenticationPrincipal UUID userId
    ) {
        List<Gift> gifts = giftService.getGiftsByWorkflowOrder(userId);
        return ResponseEntity.ok(gifts);
    }

    @GetMapping("/check-gift-idea/{giftIdeaId}")
    public ResponseEntity<Boolean> isGiftIdeaAlreadyUsed(
            @PathVariable UUID giftIdeaId,
            @AuthenticationPrincipal UUID userId
    ) {
        boolean isUsed = giftService.isGiftIdeaAlreadyUsed(userId, giftIdeaId);
        return ResponseEntity.ok(isUsed);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Gift> updateGift(
            @PathVariable UUID id,
            @Valid @RequestBody Gift gift,
            @AuthenticationPrincipal UUID userId
    ) {
        Gift updated = giftService.updateGift(id, userId, gift);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Gift> updateGiftStatus(
            @PathVariable UUID id,
            @RequestParam GiftStatus status,
            @AuthenticationPrincipal UUID userId
    ) {
        Gift updated = giftService.updateGiftStatus(id, userId, status);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGift(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID userId
    ) {
        giftService.deleteGift(id, userId);
        return ResponseEntity.noContent().build();
    }
}
