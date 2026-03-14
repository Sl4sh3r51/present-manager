package org.iu.presentmanager.gifts;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GiftRepository extends JpaRepository<Gift, UUID> {

    List<Gift> findByUserId(UUID userId);

    Optional<Gift> findByIdAndUserId(UUID id, UUID userId);

    List<Gift> findByUserIdAndPersonId(UUID userId, UUID personId);

    List<Gift> findByUserIdAndOccasionId(UUID userId, UUID occasionId);

    List<Gift> findByUserIdAndStatus(UUID userId, GiftStatus status);

    List<Gift> findByUserIdAndPersonIdAndOccasionId(UUID userId, UUID personId, UUID occasionId);

    List<Gift> findByUserIdAndPersonIdAndStatus(UUID userId, UUID personId, GiftStatus status);

    List<Gift> findByUserIdAndGiftIdeaId(UUID userId, UUID giftIdeaId);

    boolean existsByUserIdAndGiftIdeaId(UUID userId, UUID giftIdeaId);

    @Query("SELECT g FROM Gift g WHERE g.userId = :userId AND " +
            "(LOWER(g.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(g.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Gift> searchByTitleOrDescription(@Param("userId") UUID userId, @Param("searchTerm") String searchTerm);

    @Query("SELECT g FROM Gift g WHERE g.userId = :userId AND g.purchaseDate IS NULL")
    List<Gift> findNotPurchased(@Param("userId") UUID userId);

    @Query("SELECT g FROM Gift g WHERE g.userId = :userId AND g.givenDate IS NULL")
    List<Gift> findNotGiven(@Param("userId") UUID userId);

    @Query("SELECT g FROM Gift g WHERE g.userId = :userId ORDER BY g.createdAt DESC")
    List<Gift> findRecentGifts(@Param("userId") UUID userId);

    // Gifts sortiert nach Status-Workflow (PLANNED → PURCHASED → GIVEN)
    @Query("SELECT g FROM Gift g WHERE g.userId = :userId ORDER BY " +
            "CASE g.status " +
            "WHEN 'PLANNED' THEN 1 " +
            "WHEN 'BOUGHT' THEN 2 " +
            "WHEN 'GIFTED' THEN 3 " +
            "END")
    List<Gift> findByUserIdOrderByStatusWorkflow(@Param("userId") UUID userId);

    void deleteByUserIdAndId(UUID userId, UUID giftId);
}
