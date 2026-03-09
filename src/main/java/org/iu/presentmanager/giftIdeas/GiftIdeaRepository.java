package org.iu.presentmanager.giftIdeas;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GiftIdeaRepository extends JpaRepository<GiftIdea, UUID> {

    List<GiftIdea> findByUserId(UUID userId);

    Optional<GiftIdea> findByIdAndUserId(UUID id, UUID userId);

    List<GiftIdea> findByUserIdAndPersonId(UUID userId, UUID personId);

    List<GiftIdea> findByUserIdAndOccasionId(UUID userId, UUID occasionId);

    List<GiftIdea> findByUserIdAndSource(UUID userId, GiftSource source);

    List<GiftIdea> findByUserIdAndPersonIdAndOccasionId(UUID userId, UUID personId, UUID occasionId);

    // Search for title or description as search term (case-insensitive)
    @Query("SELECT g FROM GiftIdea g WHERE g.userId = :userId AND " +
            "(LOWER(g.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(g.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<GiftIdea> searchByTitleOrDescription(@Param("userId") UUID userId, @Param("searchTerm") String searchTerm);

    void deleteByIdAndUserId(UUID id, UUID userId);

    // Get recent gift ideas sorted by createdAt in descending order
    @Query("SELECT g FROM GiftIdea g WHERE g.userId = :userId ORDER BY g.createdAt DESC")
    List<GiftIdea> findRecentGiftIdeas(@Param("userId") UUID userId);
}
