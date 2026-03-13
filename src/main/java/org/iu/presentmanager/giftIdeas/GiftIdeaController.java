package org.iu.presentmanager.giftIdeas;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("gift-ideas")
@RequiredArgsConstructor
public class GiftIdeaController {

    private final GiftIdeaService giftIdeaService;

    @PostMapping
    public ResponseEntity<GiftIdea> createGiftIdea(@Valid @RequestBody GiftIdea giftIdea, @AuthenticationPrincipal UUID userid) {
        GiftIdea created = giftIdeaService.createGiftIdea(giftIdea, userid);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<GiftIdea>> getAllGiftIdeas(@AuthenticationPrincipal UUID userid, @RequestParam(required = false) UUID personId,
                                                          @RequestParam(required = false) UUID occasionId,
                                                          @RequestParam(required = false) GiftSource source) {
        List<GiftIdea> giftIdeas;
        if (personId != null && occasionId != null) {
            giftIdeas = giftIdeaService.getGiftIdeasByPersonAndOccasion(userid, personId, occasionId);
        } else if (personId != null) {
            giftIdeas = giftIdeaService.getGiftIdeasByPerson(userid, personId);
        } else if (occasionId != null) {
            giftIdeas = giftIdeaService.getGiftIdeasByOccasion(userid, occasionId);
        } else if (source != null) {
            giftIdeas = giftIdeaService.getGiftIdeasBySource(userid, source);
        } else {
            giftIdeas = giftIdeaService.getAllGiftIdeasByUser(userid);
        }
        return ResponseEntity.ok(giftIdeas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GiftIdea> getGiftIdeaById(@PathVariable UUID id, @AuthenticationPrincipal UUID userid) {
        GiftIdea giftIdea = giftIdeaService.getGiftIdeaById(id, userid);
        return ResponseEntity.ok(giftIdea);
    }

    @GetMapping("/search")
    public ResponseEntity<List<GiftIdea>> searchGiftIdeas(@RequestParam String query, @AuthenticationPrincipal UUID userid) {
        List<GiftIdea> giftIdeas = giftIdeaService.searchGiftIdeas(userid, query);
        return ResponseEntity.ok(giftIdeas);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<GiftIdea>> getRecentGiftIdeas(@AuthenticationPrincipal UUID userid) {
        List<GiftIdea> giftIdeas = giftIdeaService.getRecentGiftIdeas(userid);
        return ResponseEntity.ok(giftIdeas);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GiftIdea> updateGiftIdea(@PathVariable UUID id, @Valid @RequestBody GiftIdea giftIdea, @AuthenticationPrincipal UUID userid) {
        GiftIdea updated = giftIdeaService.updateGiftIdea(id, userid, giftIdea);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGiftIdea(@PathVariable UUID id, @AuthenticationPrincipal UUID userid) {
        giftIdeaService.deleteGiftIdea(id, userid);
        return ResponseEntity.noContent().build();
    }
}
