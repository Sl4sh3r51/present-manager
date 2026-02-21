package org.iu.presentmanager.person_interests;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PersonInterestRepository extends JpaRepository<PersonInterest, PersonInterestId> {

    List<PersonInterest> findByPersonId(UUID personId);

    List<PersonInterest> findByInterestId(UUID interestId);

    boolean existsByPersonIdAndInterestId(UUID personId, UUID interestId);

    void deleteByPersonIdAndInterestId(UUID personId, UUID interestId);

    @Query("SELECT pi FROM PersonInterest pi WHERE pi.person.userId = :userId AND pi.person.id = :personId")
    List<PersonInterest> findByUserIdAndPersonId(@Param("userId") UUID userId, @Param("personId") UUID personId);
}
