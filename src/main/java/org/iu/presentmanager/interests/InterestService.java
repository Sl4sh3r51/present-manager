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
public class InterestService {

    private final InterestRepository interestRepository;

    public List<Interest> getAllInterests() {
        return interestRepository.findAll();
    }

    public Interest getInterestsById(UUID interestId) {
        return interestRepository.findById(interestId).filter(interests -> interests.getId().equals(interestId))
                .orElseThrow(() -> new ResourceNotFoundException("Interest not found"));
    }

    public Interest getInterestsByName(String name) {
        return interestRepository.findByNameIgnoreCase(name).filter(interests -> interests.getName().equals(name))
                .orElseThrow(() -> new ResourceNotFoundException("Interest not found"));
    }

    @Transactional
    public Interest createInterests(Interest interest) {
        log.info("Creating interest: {}", interest.getName());

        if (interestRepository.existsByNameIgnoreCase(interest.getName())) {
            throw new DuplicateResourceException("Interest already exists with name: " + interest.getName());
        }

        return interestRepository.save(interest);
    }

    @Transactional
    public void deleteInterests(UUID interestId) {
        if (!interestRepository.existsById(interestId)) {
            throw new ResourceNotFoundException("Interest not found with id: " + interestId);
        }

        interestRepository.deleteById(interestId);
    }

    public boolean existsByName(String name) {
        return interestRepository.existsByNameIgnoreCase(name);
    }
}
