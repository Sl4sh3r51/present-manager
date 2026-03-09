package org.iu.presentmanager.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iu.presentmanager.exceptions.ResourceNotFoundException;
import org.iu.presentmanager.persons.PersonRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;
    private final PersonRepository personRepository;

    @Transactional
    public Task createTask(Task task, UUID userId) {
        log.info("Creating task: {} for user: {}", task.getTitle(), userId);

        // Validierung: Person existiert und gehört dem User
        personRepository.findByIdAndUserId(task.getPersonId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Person not found with id: " + task.getPersonId()));

        task.setUserId(userId);
        return taskRepository.save(task);
    }

    public List<Task> getAllTasksByUser(UUID userId) {
        return taskRepository.findByUserId(userId);
    }

    public Task getTaskById(UUID id, UUID userId) {
        return taskRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
    }

    public List<Task> getTasksByPerson(UUID userId, UUID personId) {
        // Validierung: Person gehört dem User
        personRepository.findByIdAndUserId(personId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Person not found with id: " + personId));

        return taskRepository.findByUserIdAndPersonId(userId, personId);
    }

    public List<Task> getTasksByStatus(UUID userId, Boolean isDone) {
        return taskRepository.findByUserIdAndIsDone(userId, isDone);
    }

    public List<Task> getTasksByPersonAndStatus(UUID userId, UUID personId, Boolean isDone) {
        return taskRepository.findByUserIdAndPersonIdAndIsDone(userId, personId, isDone);
    }

    public List<Task> getTasksOrderedByStatus(UUID userId) {
        return taskRepository.findByUserIdOrderByDoneStatus(userId);
    }

    public List<Task> getOpenTasksByPerson(UUID userId, UUID personId) {
        return taskRepository.findOpenTasksByPerson(userId, personId);
    }

    @Transactional
    public Task toggleTaskStatus(UUID id, UUID userId) {
        Task existing = getTaskById(id, userId);
        existing.setIsDone(!existing.getIsDone());

        log.info("Toggled task status: {} to {} for user: {}", id, existing.getIsDone(), userId);
        return taskRepository.save(existing);
    }

    @Transactional
    public Task markTaskAsDone(UUID id, UUID userId) {
        Task existing = getTaskById(id, userId);
        existing.setIsDone(true);

        log.info("Marked task as done: {} for user: {}", id, userId);
        return taskRepository.save(existing);
    }

    @Transactional
    public Task markTaskAsNotDone(UUID id, UUID userId) {
        Task existing = getTaskById(id, userId);
        existing.setIsDone(false);

        log.info("Marked task as not done: {} for user: {}", id, userId);
        return taskRepository.save(existing);
    }

    @Transactional
    public void deleteTask(UUID id, UUID userId) {
        if (!taskRepository.findByIdAndUserId(id, userId).isPresent()) {
            throw new ResourceNotFoundException("Task not found with id: " + id);
        }
        taskRepository.deleteByIdAndUserId(id, userId);
        log.info("Deleted task: {} for user: {}", id, userId);
    }

    @Transactional
    public void deleteAllCompletedTasks(UUID userId) {
        List<Task> completedTasks = taskRepository.findByUserIdAndIsDone(userId, true);
        taskRepository.deleteAll(completedTasks);
        log.info("Deleted {} completed tasks for user: {}", completedTasks.size(), userId);
    }
}