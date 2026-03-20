package org.iu.presentmanager.tasks;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<Task> createTask(
            @Valid @RequestBody Task task,
            @AuthenticationPrincipal UUID userId
    ) {
        Task created = taskService.createTask(task, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks(
            @AuthenticationPrincipal UUID userId,
            @RequestParam(required = false) UUID personId,
            @RequestParam(required = false) Boolean isDone
    ) {
        List<Task> tasks;

        if (personId != null && isDone != null) {
            tasks = taskService.getTasksByPersonAndStatus(userId, personId, isDone);
        } else if (personId != null) {
            tasks = taskService.getTasksByPerson(userId, personId);
        } else if (isDone != null) {
            tasks = taskService.getTasksByStatus(userId, isDone);
        } else {
            tasks = taskService.getAllTasksByUser(userId);
        }

        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID userId
    ) {
        Task task = taskService.getTaskById(id, userId);
        return ResponseEntity.ok(task);
    }

    @GetMapping("/ordered")
    public ResponseEntity<List<Task>> getTasksOrderedByStatus(
            @AuthenticationPrincipal UUID userId
    ) {
        List<Task> tasks = taskService.getTasksOrderedByStatus(userId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/open/person/{personId}")
    public ResponseEntity<List<Task>> getOpenTasksByPerson(
            @PathVariable UUID personId,
            @AuthenticationPrincipal UUID userId
    ) {
        List<Task> tasks = taskService.getOpenTasksByPerson(userId, personId);
        return ResponseEntity.ok(tasks);
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<Task> toggleTaskStatus(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID userId
    ) {
        Task updated = taskService.toggleTaskStatus(id, userId);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/done")
    public ResponseEntity<Task> markTaskAsDone(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID userId
    ) {
        Task updated = taskService.markTaskAsDone(id, userId);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/not-done")
    public ResponseEntity<Task> markTaskAsNotDone(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID userId
    ) {
        Task updated = taskService.markTaskAsNotDone(id, userId);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID userId
    ) {
        taskService.deleteTask(id, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/completed")
    public ResponseEntity<Void> deleteAllCompletedTasks(
            @AuthenticationPrincipal UUID userId
    ) {
        taskService.deleteAllCompletedTasks(userId);
        return ResponseEntity.noContent().build();
    }
}