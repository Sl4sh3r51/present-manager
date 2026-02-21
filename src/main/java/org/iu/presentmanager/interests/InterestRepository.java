package org.iu.presentmanager.interests;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Optional;
import java.util.UUID;

@ResponseBody
public interface InterestRepository extends JpaRepository<Interest, UUID> {

    Optional<Interest> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}
