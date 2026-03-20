package org.iu.presentmanager.interests;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InterestRepository extends JpaRepository<Interest, UUID> {

    Optional<Interest> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}
