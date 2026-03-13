package org.iu.presentmanager.tasks;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    List<Task> findByUserId(UUID userId);

    Optional<Task> findByIdAndUserId(UUID id, UUID userId);

    List<Task> findByUserIdAndPersonId(UUID userId, UUID personId);

    // Nach Person und isDone filtern
    List<Task> findByUserIdAndPersonIdAndIsDone(UUID userId, UUID personId, Boolean isDone);

    // Nur offene Tasks (isDone = false)
    List<Task> findByUserIdAndIsDone(UUID userId, Boolean isDone);

    void deleteByIdAndUserId(UUID id, UUID userId);

    @Query("SELECT t FROM Task t WHERE t.userId = :userId ORDER BY t.isDone ASC, t.createdAt DESC")
    List<Task> findByUserIdOrderByDoneStatus(@Param("userId") UUID userId);

    @Query("SELECT t FROM Task t WHERE t.userId = :userId AND t.personId = :personId AND t.isDone = false ORDER BY t.createdAt DESC")
    List<Task> findOpenTasksByPerson(@Param("userId") UUID userId, @Param("personId") UUID personId);
}
