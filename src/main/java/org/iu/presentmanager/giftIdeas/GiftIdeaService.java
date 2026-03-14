package org.iu.presentmanager.giftIdeas;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iu.presentmanager.exceptions.ResourceNotFoundException;
import org.iu.presentmanager.occasions.OccasionRepository;
import org.iu.presentmanager.persons.PersonRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class GiftIdeaService {

    private final GiftIdeaRepository giftIdeaRepository;
    private final PersonRepository personRepository;
    private final OccasionRepository occasionRepository;

    @Transactional
    public GiftIdea createGiftIdea(GiftIdea giftIdea, UUID userId) {
        personRepository.findByIdAndUserId(giftIdea.getPersonId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Person not found with id: " + giftIdea.getPersonId()));

        if (giftIdea.getOccasionId() != null) {
            occasionRepository.findByIdAndUserId(giftIdea.getOccasionId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Occasion not found with id: " + giftIdea.getOccasionId()));
        }

        if(giftIdea.getTitle() == null || giftIdea.getTitle().isEmpty()){
            throw new IllegalArgumentException("Title cannot be empty");
        }

        log.info("Creating gift idea: {} for user: {}", giftIdea.getTitle(), userId);

        giftIdea.setUserId(userId);
        return giftIdeaRepository.save(giftIdea);
    }

    public List<GiftIdea> getAllGiftIdeasByUser(UUID userId) {
        return giftIdeaRepository.findByUserId(userId);
    }

    public GiftIdea getGiftIdeaById(UUID id, UUID userId) {
        return giftIdeaRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Gift idea not found with id: " + id));
    }

    public List<GiftIdea> getGiftIdeasByPerson(UUID userId, UUID personId) {
        personRepository.findByIdAndUserId(personId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Person not found with id: " + personId));

        return giftIdeaRepository.findByUserIdAndPersonId(userId, personId);
    }

    public List<GiftIdea> getGiftIdeasByOccasion(UUID userId, UUID occasionId) {
        occasionRepository.findByIdAndUserId(occasionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Occasion not found with id: " + occasionId));

        return giftIdeaRepository.findByUserIdAndOccasionId(userId, occasionId);
    }

    public List<GiftIdea> getGiftIdeasBySource(UUID userId, GiftSource source) {
        return giftIdeaRepository.findByUserIdAndSource(userId, source);
    }

    public List<GiftIdea> getGiftIdeasByPersonAndOccasion(UUID userId, UUID personId, UUID occasionId) {
        personRepository.findByIdAndUserId(personId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Person not found with id: " + personId));

        if (occasionId != null) {
            occasionRepository.findByIdAndUserId(occasionId, userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Occasion not found with id: " + occasionId));
        }

        return giftIdeaRepository.findByUserIdAndPersonIdAndOccasionId(userId, personId, occasionId);
    }

    public List<GiftIdea> searchGiftIdeas(UUID userId, String searchTerm) {
        return giftIdeaRepository.searchByTitleOrDescription(userId, searchTerm);
    }

    public List<GiftIdea> getRecentGiftIdeas(UUID userId) {
        return giftIdeaRepository.findRecentGiftIdeas(userId);
    }

    @Transactional
    public GiftIdea updateGiftIdea(UUID id, UUID userId, GiftIdea updatedGiftIdea) {
        GiftIdea existing = getGiftIdeaById(id, userId);

        // Validierung: Wenn Person geändert wird
        if (!existing.getPersonId().equals(updatedGiftIdea.getPersonId())) {
                throw new IllegalArgumentException("Person cannot be changed for existing gift idea");
        }

        // Validierung: Wenn Occasion geändert wird
        if (updatedGiftIdea.getOccasionId() != null &&
                !updatedGiftIdea.getOccasionId().equals(existing.getOccasionId())) {
            occasionRepository.findByIdAndUserId(updatedGiftIdea.getOccasionId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Occasion not found with id: " + updatedGiftIdea.getOccasionId()));
        }

        existing.setPersonId(updatedGiftIdea.getPersonId());
        existing.setOccasionId(updatedGiftIdea.getOccasionId());
        existing.setTitle(updatedGiftIdea.getTitle());
        existing.setDescription(updatedGiftIdea.getDescription());
        existing.setLink(updatedGiftIdea.getLink());
        existing.setImageUrl(updatedGiftIdea.getImageUrl());
        existing.setSource(updatedGiftIdea.getSource());

        log.info("Updated gift idea: {} for user: {}", id, userId);
        return giftIdeaRepository.save(existing);
    }

    @Transactional
    public void deleteGiftIdea(UUID id, UUID userId) {
        if (giftIdeaRepository.findByIdAndUserId(id, userId).isEmpty()) {
            throw new ResourceNotFoundException("Gift idea not found with id: " + id);
        }
        giftIdeaRepository.deleteByIdAndUserId(id, userId);
        log.info("Deleted gift idea: {} for user: {}", id, userId);
    }
}
