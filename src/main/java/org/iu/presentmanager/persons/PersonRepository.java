package org.iu.presentmanager.persons;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PersonRepository extends JpaRepository<Person, UUID> {

    List<Person> findByUserId(UUID userId);

    List<Person> findByUserIdAndStatus(UUID userId, PersonStatus status);

    Optional<Person> findByIdAndUserId(UUID id, UUID userId);

    @Query("SELECT p FROM Person p WHERE p.userId = :userId " +
            "AND FUNCTION('EXTRACT', MONTH FROM p.birthday) = :month " +
            "AND FUNCTION('EXTRACT', DAY FROM p.birthday) = :day")
    List<Person> findUpcomingBirthdays(
            @Param("userId") UUID userId,
            @Param("month") int month,
            @Param("day") int day
    );

    @Query("SELECT p FROM Person p WHERE p.userId = :userId " +
            "AND FUNCTION('EXTRACT', MONTH FROM p.birthday) = :month " +
            "ORDER BY FUNCTION('EXTRACT', DAY FROM p.birthday)")
    List<Person> findBirthdaysInMonth(
            @Param("userId") UUID userId,
            @Param("month") int month
    );

    long countByUserIdAndStatus(UUID userId, PersonStatus status);

    void deleteByIdAndUserId(UUID id, UUID userId);

    @Query("SELECT p FROM Person p JOIN p.personInterests pi WHERE p.userId = :userId AND pi.interests.id = :interestId")
    List<Person> findByUserIdAndInterestId(@Param("userId") UUID userId, @Param("interestId") UUID interestId);

    @Query("SELECT p FROM Person p JOIN p.personInterests pi WHERE p.userId = :userId AND LOWER(pi.interests.name) = LOWER(:interestName)")
    List<Person> findByUserIdAndInterestName(@Param("userId") UUID userId, @Param("interestName") String interestName);
}
