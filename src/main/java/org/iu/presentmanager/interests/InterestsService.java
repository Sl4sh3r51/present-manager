package org.iu.presentmanager.interests;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iu.presentmanager.exceptions.DuplicateResourceException;
import org.iu.presentmanager.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterestsService {

    private final InterestsRepository interestsRepository;

    public List<Interests> getAllInterests() {
        return interestsRepository.findAll();
    }

    public Interests getInterestsById(UUID interestId) {
        return interestsRepository.findById(interestId).filter(interests -> interests.getId().equals(interestId))
                .orElseThrow(() -> new ResourceNotFoundException("Interest not found"));
    }

    public Interests getInterestsByName(String name) {
        return interestsRepository.findByNameIgnoreCase(name).filter(interests -> interests.getName().equals(name))
                .orElseThrow(() -> new ResourceNotFoundException("Interest not found"));
    }

    @Transactional
    public Interests createInterests(Interests interests) {
        log.info("Creating interest: {}", interests.getName());

        if (interestsRepository.existsByNameIgnoreCase(interests.getName())) {
            throw new DuplicateResourceException("Interest already exists with name: " + interests.getName());
        }

        return interestsRepository.save(interests);
    }

    @Transactional
    public void deleteInterests(UUID interestId) {
        if (!interestsRepository.existsById(interestId)) {
            throw new ResourceNotFoundException("Interest not found with id: " + interestId);
        }

        interestsRepository.deleteById(interestId);
    }

    public boolean existsByName(String name) {
        return interestsRepository.existsByNameIgnoreCase(name);
    }
}
