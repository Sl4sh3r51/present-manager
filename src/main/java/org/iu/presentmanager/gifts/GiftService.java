package org.iu.presentmanager.gifts;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iu.presentmanager.exceptions.ResourceNotFoundException;
import org.iu.presentmanager.giftIdeas.GiftIdea;
import org.iu.presentmanager.giftIdeas.GiftIdeaRepository;
import org.iu.presentmanager.occasions.OccasionRepository;
import org.iu.presentmanager.persons.PersonRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class GiftService {

    private final GiftRepository giftRepository;
    private final PersonRepository personRepository;
    private final OccasionRepository occasionRepository;
    private final GiftIdeaRepository giftIdeaRepository;

    @Transactional
    public Gift createGift(Gift gift, UUID userId) {
        log.info("Creating gift: {} for user: {}", gift.getTitle(), userId);

        // Validierung: Person existiert und gehört dem User
        personRepository.findByIdAndUserId(gift.getPersonId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Person not found with id: " + gift.getPersonId()));

        // Validierung: Occasion existiert und gehört dem User
        occasionRepository.findByIdAndUserId(gift.getOccasionId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Occasion not found with id: " + gift.getOccasionId()));

        // Validierung: GiftIdea existiert und gehört dem User (falls angegeben)
        if (gift.getGiftIdeaId() != null) {
            giftIdeaRepository.findByIdAndUserId(gift.getGiftIdeaId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Gift idea not found with id: " + gift.getGiftIdeaId()));
        }

        gift.setUserId(userId);
        return giftRepository.save(gift);
    }

    public Gift createGiftFromGiftIdea(GiftIdea giftIdea, UUID userId) {
        log.info("Creating gift from gift idea: {} for user: {}", giftIdea.getTitle(), userId);

        // Validierung: GiftIdea existiert und gehört dem User
        GiftIdea existingIdea = giftIdeaRepository.findByIdAndUserId(giftIdea.getId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Gift idea not found with id: " + giftIdea.getId()));

        Gift gift = new Gift();
        gift.setGiftIdeaId(existingIdea.getId());
        gift.setPersonId(existingIdea.getPersonId());
        gift.setUserId(userId);
        gift.setOccasionId(existingIdea.getOccasionId());
        gift.setGiftIdeaId(existingIdea.getId());
        gift.setTitle(existingIdea.getTitle());
        gift.setDescription(existingIdea.getDescription());
        gift.setLink(existingIdea.getLink());
        gift.setImageUrl(existingIdea.getImageUrl());

        return giftRepository.save(gift);
    }

    public List<Gift> getAllGiftsByUser(UUID userId) {
        return giftRepository.findByUserId(userId);
    }

    public Gift getGiftById(UUID id, UUID userId) {
        return giftRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Gift not found with id: " + id));
    }

    public List<Gift> getGiftsByPerson(UUID userId, UUID personId) {
        // Validierung: Person gehört dem User
        personRepository.findByIdAndUserId(personId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Person not found with id: " + personId));

        return giftRepository.findByUserIdAndPersonId(userId, personId);
    }

    public List<Gift> getGiftsByOccasion(UUID userId, UUID occasionId) {
        // Validierung: Occasion gehört dem User
        occasionRepository.findByIdAndUserId(occasionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Occasion not found with id: " + occasionId));

        return giftRepository.findByUserIdAndOccasionId(userId, occasionId);
    }

    public List<Gift> getGiftsByStatus(UUID userId, GiftStatus status) {
        return giftRepository.findByUserIdAndStatus(userId, status);
    }

    public List<Gift> getGiftsByPersonAndOccasion(UUID userId, UUID personId, UUID occasionId) {
        return giftRepository.findByUserIdAndPersonIdAndOccasionId(userId, personId, occasionId);
    }

    public List<Gift> getGiftsByPersonAndStatus(UUID userId, UUID personId, GiftStatus status) {
        return giftRepository.findByUserIdAndPersonIdAndStatus(userId, personId, status);
    }

    public List<Gift> getGiftsByGiftIdea(UUID userId, UUID giftIdeaId) {
        return giftRepository.findByUserIdAndGiftIdeaId(userId, giftIdeaId);
    }

    public boolean isGiftIdeaAlreadyUsed(UUID userId, UUID giftIdeaId) {
        return giftRepository.existsByUserIdAndGiftIdeaId(userId, giftIdeaId);
    }

    public List<Gift> searchGifts(UUID userId, String searchTerm) {
        return giftRepository.searchByTitleOrDescription(userId, searchTerm);
    }

    public List<Gift> getNotPurchasedGifts(UUID userId) {
        return giftRepository.findNotPurchased(userId);
    }

    public List<Gift> getNotGivenGifts(UUID userId) {
        return giftRepository.findNotGiven(userId);
    }

    public List<Gift> getRecentGifts(UUID userId) {
        return giftRepository.findRecentGifts(userId);
    }

    public List<Gift> getGiftsByWorkflowOrder(UUID userId) {
        return giftRepository.findByUserIdOrderByStatusWorkflow(userId);
    }

    @Transactional
    public Gift updateGift(UUID id, UUID userId, Gift updatedGift) {
        Gift existing = getGiftById(id, userId);

        // Validierung: Wenn Person geändert wird
        if (!existing.getPersonId().equals(updatedGift.getPersonId())) {
            personRepository.findByIdAndUserId(updatedGift.getPersonId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Person not found with id: " + updatedGift.getPersonId()));
        }

        // Validierung: Wenn Occasion geändert wird
        if (!existing.getOccasionId().equals(updatedGift.getOccasionId())) {
            occasionRepository.findByIdAndUserId(updatedGift.getOccasionId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Occasion not found with id: " + updatedGift.getOccasionId()));
        }

        // Validierung: Wenn GiftIdea geändert wird
        if (updatedGift.getGiftIdeaId() != null &&
                !updatedGift.getGiftIdeaId().equals(existing.getGiftIdeaId())) {
            giftIdeaRepository.findByIdAndUserId(updatedGift.getGiftIdeaId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Gift idea not found with id: " + updatedGift.getGiftIdeaId()));
        }

        existing.setPersonId(updatedGift.getPersonId());
        existing.setOccasionId(updatedGift.getOccasionId());
        existing.setGiftIdeaId(updatedGift.getGiftIdeaId());
        existing.setTitle(updatedGift.getTitle());
        existing.setDescription(updatedGift.getDescription());
        existing.setLink(updatedGift.getLink());
        existing.setImageUrl(updatedGift.getImageUrl());
        existing.setStatus(updatedGift.getStatus());
        existing.setPurchaseDate(updatedGift.getPurchaseDate());
        existing.setGivenDate(updatedGift.getGivenDate());

        log.info("Updated gift: {} for user: {}", id, userId);
        return giftRepository.save(existing);
    }

    @Transactional
    public Gift updateGiftStatus(UUID id, UUID userId, GiftStatus newStatus) {
        Gift existing = getGiftById(id, userId);
        existing.setStatus(newStatus);

        // Automatisch ein Datum setzen
        if (newStatus == GiftStatus.BOUGHT && existing.getPurchaseDate() == null) {
            existing.setPurchaseDate(LocalDate.now());
        }
        if (newStatus == GiftStatus.GIFTED && existing.getGivenDate() == null) {
            existing.setGivenDate(LocalDate.now());
        }

        log.info("Updated gift status: {} to {} for user: {}", id, newStatus, userId);
        return giftRepository.save(existing);
    }

    @Transactional
    public void deleteGift(UUID id, UUID userId) {
        if (!giftRepository.findByIdAndUserId(id, userId).isPresent()) {
            throw new ResourceNotFoundException("Gift not found with id: " + id);
        }
        giftRepository.deleteByUserIdAndId(userId, id);
        log.info("Deleted gift: {} for user: {}", id, userId);
    }
}
