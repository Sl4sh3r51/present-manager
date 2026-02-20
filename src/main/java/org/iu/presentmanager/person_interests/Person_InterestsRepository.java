package org.iu.presentmanager.person_interests;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface Person_InterestsRepository extends JpaRepository<Person_Interests, PersonInterestsId> {

    List<Person_Interests> findByPersonId(UUID personId);

    List<Person_Interests> findByInterestsId(UUID interestId);

    boolean existsByPersonIdAndInterestsId(UUID personId, UUID interestId);

    void deleteByPersonIdAndInterestsId(UUID personId, UUID interestId);

    @Query("SELECT pi FROM Person_Interests pi WHERE pi.person.userId = :userId AND pi.person.id = :personId")
    List<Person_Interests> findByUserIdAndPersonId(@Param("userId") UUID userId, @Param("personId") UUID personId);
}
