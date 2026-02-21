package org.iu.presentmanager.occasions;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OccasionRepository extends JpaRepository<Occasion, UUID> {

    List<Occasion> findByUserId(UUID userId);

    Optional<Occasion> findByIdAndUserId(UUID occasionId, UUID userId);

    List<Occasion> findByUserIdAndType(UUID userId, OccasionType type);

    List<Occasion> findByUserIdAndIsRecurring(UUID userId, boolean isRecurring);

    boolean existsByUserIdAndName(UUID userId, String name);

    // Name-basierte Suche (case-insensitive)
    @Query("SELECT o FROM Occasion o WHERE o.userId = :userId AND LOWER(o.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Occasion> searchByName(@Param("userId") UUID userId, @Param("searchTerm") String searchTerm);

    // Alle FIXED Occasions in einem bestimmten Monat
    @Query("SELECT o FROM Occasion o WHERE o.userId = :userId AND o.type = 'FIXED' AND o.fixedMonth = :month ORDER BY o.fixedDay")
    List<Occasion> findFixedOccasionsByMonth(@Param("userId") UUID userId, @Param("month") int month);

    // Alle FIXED Occasions an einem bestimmten Datum (Monat + Tag)
    @Query("SELECT o FROM Occasion o WHERE o.userId = :userId AND o.type = 'FIXED' AND o.fixedMonth = :month AND o.fixedDay = :day")
    List<Occasion> findFixedOccasionsByDate(@Param("userId") UUID userId, @Param("month") int month, @Param("day") int day);

    // Alle wiederkehrenden FIXED Occasions
    @Query("SELECT o FROM Occasion o WHERE o.userId = :userId AND o.type = 'FIXED' AND o.isRecurring = true ORDER BY o.fixedMonth, o.fixedDay")
    List<Occasion> findRecurringFixedOccasions(@Param("userId") UUID userId);

    void deleteByIdAndUserId(UUID id, UUID userId);
}
