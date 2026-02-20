package org.iu.presentmanager.interests;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Optional;
import java.util.UUID;

@ResponseBody
public interface InterestsRepository extends JpaRepository<Interests, UUID> {

    Optional<Interests> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}
