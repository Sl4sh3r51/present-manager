package org.iu.presentmanager.tasks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

    @Mock
    private TaskService taskService;

    @InjectMocks
    private TaskController taskController;

    private UUID userId;
    private UUID personId;
    private UUID taskId;
    private Task testTask;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        personId = UUID.randomUUID();
        taskId = UUID.randomUUID();

        testTask = new Task();
        testTask.setId(taskId);
        testTask.setUserId(userId);
        testTask.setPersonId(personId);
        testTask.setTitle("Test Task");
        testTask.setIsDone(false);
    }

    @Test
    void shouldCreateTaskAndReturnCreatedStatus() {
        // GIVEN
        Task newTask = new Task();
        newTask.setPersonId(personId);
        newTask.setTitle("New Task");

        when(taskService.createTask(any(Task.class), eq(userId))).thenReturn(testTask);

        // WHEN
        ResponseEntity<Task> response = taskController.createTask(newTask, userId);

        // THEN
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(taskId, response.getBody().getId());
        verify(taskService, times(1)).createTask(any(Task.class), eq(userId));
    }

    @Test
    void shouldReturnCreatedTaskObject() {
        // GIVEN
        Task newTask = new Task();
        newTask.setTitle("Test Task");
        when(taskService.createTask(any(Task.class), eq(userId))).thenReturn(testTask);

        // WHEN
        ResponseEntity<Task> response = taskController.createTask(newTask, userId);

        // THEN
        assertEquals(testTask, response.getBody());
    }

    @Test
    void shouldGetAllTasksWhenNoFilterParametersProvided() {
        // GIVEN
        List<Task> tasks = List.of(testTask);
        when(taskService.getAllTasksByUser(userId)).thenReturn(tasks);

        // WHEN
        ResponseEntity<List<Task>> response = taskController.getAllTasks(userId, null, null);

        // THEN
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(taskId, response.getBody().getFirst().getId());
        verify(taskService, times(1)).getAllTasksByUser(userId);
    }

    @Test
    void shouldGetTasksByPersonWhenPersonIdProvided() {
        // GIVEN
        List<Task> tasks = List.of(testTask);
        when(taskService.getTasksByPerson(userId, personId)).thenReturn(tasks);

        // WHEN
        ResponseEntity<List<Task>> response = taskController.getAllTasks(userId, personId, null);

        // THEN
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(taskService, times(1)).getTasksByPerson(userId, personId);
        verify(taskService, never()).getAllTasksByUser(any());
    }

    @Test
    void shouldGetTasksByStatusWhenIsDoneProvided() {
        // GIVEN
        List<Task> tasks = List.of(testTask);
        when(taskService.getTasksByStatus(userId, false)).thenReturn(tasks);

        // WHEN
        ResponseEntity<List<Task>> response = taskController.getAllTasks(userId, null, false);

        // THEN
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(taskService, times(1)).getTasksByStatus(userId, false);
    }

    @Test
    void shouldGetTasksByPersonAndStatusWhenBothProvided() {
        // GIVEN
        List<Task> tasks = List.of(testTask);
        when(taskService.getTasksByPersonAndStatus(userId, personId, false)).thenReturn(tasks);

        // WHEN
        ResponseEntity<List<Task>> response = taskController.getAllTasks(userId, personId, false);

        // THEN
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(taskService, times(1)).getTasksByPersonAndStatus(userId, personId, false);
    }

    @Test
    void shouldReturnEmptyListWhenNoTasksFound() {
        // GIVEN
        when(taskService.getAllTasksByUser(userId)).thenReturn(List.of());

        // WHEN
        ResponseEntity<List<Task>> response = taskController.getAllTasks(userId, null, null);

        // THEN
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void shouldGetTaskByIdSuccessfully() {
        // GIVEN
        when(taskService.getTaskById(taskId, userId)).thenReturn(testTask);

        // WHEN
        ResponseEntity<Task> response = taskController.getTaskById(taskId, userId);

        // THEN
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(taskId, response.getBody().getId());
        verify(taskService, times(1)).getTaskById(taskId, userId);
    }

    @Test
    void shouldGetTasksOrderedByStatus() {
        // GIVEN
        List<Task> tasks = List.of(testTask);
        when(taskService.getTasksOrderedByStatus(userId)).thenReturn(tasks);

        // WHEN
        ResponseEntity<List<Task>> response = taskController.getTasksOrderedByStatus(userId);

        // THEN
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(taskService, times(1)).getTasksOrderedByStatus(userId);
    }

    @Test
    void shouldGetOpenTasksByPerson() {
        // GIVEN
        List<Task> tasks = List.of(testTask);
        when(taskService.getOpenTasksByPerson(userId, personId)).thenReturn(tasks);

        // WHEN
        ResponseEntity<List<Task>> response = taskController.getOpenTasksByPerson(personId, userId);

        // THEN
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(taskService, times(1)).getOpenTasksByPerson(userId, personId);
    }

    @Test
    void shouldReturnEmptyListWhenNoOpenTasksFound() {
        // GIVEN
        when(taskService.getOpenTasksByPerson(userId, personId)).thenReturn(List.of());

        // WHEN
        ResponseEntity<List<Task>> response = taskController.getOpenTasksByPerson(personId, userId);

        // THEN
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void shouldToggleTaskStatusSuccessfully() {
        // GIVEN
        testTask.setIsDone(true);
        when(taskService.toggleTaskStatus(taskId, userId)).thenReturn(testTask);

        // WHEN
        ResponseEntity<Task> response = taskController.toggleTaskStatus(taskId, userId);

        // THEN
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getIsDone());
        verify(taskService, times(1)).toggleTaskStatus(taskId, userId);
    }

    @Test
    void shouldMarkTaskAsDone() {
        // GIVEN
        testTask.setIsDone(true);
        when(taskService.markTaskAsDone(taskId, userId)).thenReturn(testTask);

        // WHEN
        ResponseEntity<Task> response = taskController.markTaskAsDone(taskId, userId);

        // THEN
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getIsDone());
        verify(taskService, times(1)).markTaskAsDone(taskId, userId);
    }

    @Test
    void shouldMarkTaskAsNotDone() {
        // GIVEN
        testTask.setIsDone(false);
        when(taskService.markTaskAsNotDone(taskId, userId)).thenReturn(testTask);

        // WHEN
        ResponseEntity<Task> response = taskController.markTaskAsNotDone(taskId, userId);

        // THEN
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().getIsDone());
        verify(taskService, times(1)).markTaskAsNotDone(taskId, userId);
    }

    @Test
    void shouldDeleteTaskAndReturnNoContentStatus() {
        // GIVEN
        doNothing().when(taskService).deleteTask(taskId, userId);

        // WHEN
        ResponseEntity<Void> response = taskController.deleteTask(taskId, userId);

        // THEN
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(taskService, times(1)).deleteTask(taskId, userId);
    }

    @Test
    void shouldDeleteAllCompletedTasksAndReturnNoContentStatus() {
        // GIVEN
        doNothing().when(taskService).deleteAllCompletedTasks(userId);

        // WHEN
        ResponseEntity<Void> response = taskController.deleteAllCompletedTasks(userId);

        // THEN
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(taskService, times(1)).deleteAllCompletedTasks(userId);
    }
}

