package org.iu.presentmanager.occasions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iu.presentmanager.exceptions.DuplicateResourceException;
import org.iu.presentmanager.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OccasionService {

    private final OccasionRepository occasionRepository;

    @Transactional
    public Occasion createOccasion(Occasion occasion, UUID userId) {
        log.info("Creating occasion: {} for user: {}", occasion.getName(), userId);

        // Validierung: Name bereits vorhanden?
        if (occasionRepository.existsByUserIdAndName(userId, occasion.getName())) {
            throw new DuplicateResourceException("Occasion already exists with name: " + occasion.getName());
        }

        // Validierung: FIXED muss fixedMonth und fixedDay haben
        if (occasion.getType() == OccasionType.FIXED) {
            if (occasion.getFixedMonth() == null || occasion.getFixedDay() == null) {
                throw new IllegalArgumentException("Fixed occasions must have fixedMonth and fixedDay");
            }
        }

        occasion.setUserId(userId);
        return occasionRepository.save(occasion);
    }

    public List<Occasion> getAllOccasionsByUser(UUID userId) {
        return occasionRepository.findByUserId(userId);
    }

    public Occasion getOccasionById(UUID id, UUID userId) {
        return occasionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Occasion not found with id: " + id));
    }

    public List<Occasion> getOccasionsByType(UUID userId, OccasionType type) {
        return occasionRepository.findByUserIdAndType(userId, type);
    }

    public List<Occasion> getRecurringOccasions(UUID userId, Boolean isRecurring) {
        return occasionRepository.findByUserIdAndIsRecurring(userId, isRecurring);
    }

    public List<Occasion> searchOccasionsByName(UUID userId, String searchTerm) {
        return occasionRepository.searchByName(userId, searchTerm);
    }

    public List<Occasion> getFixedOccasionsByMonth(UUID userId, int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12");
        }
        return occasionRepository.findFixedOccasionByMonth(userId, month);
    }

    public List<Occasion> getTodayFixedOccasions(UUID userId) {
        LocalDate today = LocalDate.now();
        return occasionRepository.findFixedOccasionByDate(userId, today.getMonthValue(), today.getDayOfMonth());
    }

    public List<Occasion> getRecurringFixedOccasions(UUID userId) {
        return occasionRepository.findRecurringFixedOccasion(userId);
    }

    @Transactional
    public Occasion updateOccasion(UUID id, UUID userId, Occasion updatedOccasion) {
        Occasion existing = getOccasionById(id, userId);

        // Validierung: Name bereits von anderem Occasion verwendet?
        if (!existing.getName().equals(updatedOccasion.getName()) &&
                occasionRepository.existsByUserIdAndName(userId, updatedOccasion.getName())) {
            throw new DuplicateResourceException("Occasion already exists with name: " + updatedOccasion.getName());
        }

        existing.setName(updatedOccasion.getName());
        existing.setType(updatedOccasion.getType());
        existing.setFixedMonth(updatedOccasion.getFixedMonth());
        existing.setFixedDay(updatedOccasion.getFixedDay());
        existing.setIsRecurring(updatedOccasion.getIsRecurring());

        log.info("Updated occasion: {} for user: {}", id, userId);
        return occasionRepository.save(existing);
    }

    @Transactional
    public void deleteOccasion(UUID id, UUID userId) {
        if (occasionRepository.findByIdAndUserId(id, userId).isEmpty()) {
            throw new ResourceNotFoundException("Occasion not found with id: " + id);
        }
        occasionRepository.deleteByIdAndUserId(id, userId);
        log.info("Deleted occasion: {} for user: {}", id, userId);
    }


    //Helper-Methoden

    public long countOccasionsByUser(UUID userId) {
        return occasionRepository.findByUserId(userId).size();
    }

    public long countOccasionsByType(UUID userId, OccasionType type) {
        return occasionRepository.findByUserIdAndType(userId, type).size();
    }

    public boolean existsByName(UUID userId, String name) {
        return occasionRepository.existsByUserIdAndName(userId, name);
    }
}
