package org.iu.presentmanager.tasks;

import org.iu.presentmanager.exceptions.ResourceNotFoundException;
import org.iu.presentmanager.persons.PersonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private PersonRepository personRepository;

    @InjectMocks
    private TaskService taskService;

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
    void shouldCreateTaskSuccessfully() {
        // GIVEN
        Task newTask = new Task();
        newTask.setPersonId(personId);
        newTask.setTitle("New Task");
        newTask.setIsDone(false);

        when(personRepository.findByIdAndUserId(personId, userId)).thenReturn(Optional.of(new org.iu.presentmanager.persons.Person()));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        // WHEN
        Task result = taskService.createTask(newTask, userId);

        // THEN
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals("Test Task", result.getTitle());
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenPersonNotFoundDuringCreate() {
        // GIVEN
        Task newTask = new Task();
        newTask.setPersonId(personId);
        newTask.setTitle("New Task");

        when(personRepository.findByIdAndUserId(personId, userId)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(ResourceNotFoundException.class, () -> taskService.createTask(newTask, userId));
        verify(taskRepository, never()).save(any());
    }

    @Test
    void shouldSetUserIdOnCreatedTask() {
        // GIVEN
        Task newTask = new Task();
        newTask.setPersonId(personId);
        newTask.setTitle("New Task");

        when(personRepository.findByIdAndUserId(personId, userId)).thenReturn(Optional.of(new org.iu.presentmanager.persons.Person()));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);

        // WHEN
        taskService.createTask(newTask, userId);

        // THEN
        verify(taskRepository).save(captor.capture());
        assertEquals(userId, captor.getValue().getUserId());
    }

    @Test
    void shouldGetAllTasksByUser() {
        // GIVEN
        List<Task> tasks = List.of(testTask);
        when(taskRepository.findByUserId(userId)).thenReturn(tasks);

        // WHEN
        List<Task> result = taskService.getAllTasksByUser(userId);

        // THEN
        assertEquals(1, result.size());
        assertEquals(taskId, result.get(0).getId());
        verify(taskRepository, times(1)).findByUserId(userId);
    }

    @Test
    void shouldGetTaskByIdSuccessfully() {
        // GIVEN
        when(taskRepository.findByIdAndUserId(taskId, userId)).thenReturn(Optional.of(testTask));

        // WHEN
        Task result = taskService.getTaskById(taskId, userId);

        // THEN
        assertNotNull(result);
        assertEquals(taskId, result.getId());
        assertEquals(userId, result.getUserId());
    }

    @Test
    void shouldThrowExceptionWhenTaskNotFound() {
        // GIVEN
        when(taskRepository.findByIdAndUserId(taskId, userId)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(ResourceNotFoundException.class, () -> taskService.getTaskById(taskId, userId));
    }

    @Test
    void shouldGetTasksByPerson() {
        // GIVEN
        List<Task> tasks = List.of(testTask);
        when(personRepository.findByIdAndUserId(personId, userId)).thenReturn(Optional.of(new org.iu.presentmanager.persons.Person()));
        when(taskRepository.findByUserIdAndPersonId(userId, personId)).thenReturn(tasks);

        // WHEN
        List<Task> result = taskService.getTasksByPerson(userId, personId);

        // THEN
        assertEquals(1, result.size());
    }

    @Test
    void shouldThrowExceptionWhenPersonNotFoundInGetTasksByPerson() {
        // GIVEN
        when(personRepository.findByIdAndUserId(personId, userId)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(ResourceNotFoundException.class, () -> taskService.getTasksByPerson(userId, personId));
        verify(taskRepository, never()).findByUserIdAndPersonId(any(), any());
    }

    @Test
    void shouldGetTasksByStatusOpen() {
        // GIVEN
        List<Task> tasks = List.of(testTask);
        when(taskRepository.findByUserIdAndIsDone(userId, false)).thenReturn(tasks);

        // WHEN
        List<Task> result = taskService.getTasksByStatus(userId, false);

        // THEN
        assertEquals(1, result.size());
        assertFalse(result.get(0).getIsDone());
    }

    @Test
    void shouldGetTasksByStatusCompleted() {
        // GIVEN
        Task completedTask = new Task();
        completedTask.setId(taskId);
        completedTask.setIsDone(true);
        List<Task> tasks = List.of(completedTask);
        when(taskRepository.findByUserIdAndIsDone(userId, true)).thenReturn(tasks);

        // WHEN
        List<Task> result = taskService.getTasksByStatus(userId, true);

        // THEN
        assertEquals(1, result.size());
        assertTrue(result.get(0).getIsDone());
    }

    @Test
    void shouldGetTasksByPersonAndStatus() {
        // GIVEN
        List<Task> tasks = List.of(testTask);
        when(taskRepository.findByUserIdAndPersonIdAndIsDone(userId, personId, false)).thenReturn(tasks);

        // WHEN
        List<Task> result = taskService.getTasksByPersonAndStatus(userId, personId, false);

        // THEN
        assertEquals(1, result.size());
    }

    @Test
    void shouldGetTasksOrderedByStatus() {
        // GIVEN
        List<Task> tasks = List.of(testTask);
        when(taskRepository.findByUserIdOrderByDoneStatus(userId)).thenReturn(tasks);

        // WHEN
        List<Task> result = taskService.getTasksOrderedByStatus(userId);

        // THEN
        assertEquals(1, result.size());
        verify(taskRepository, times(1)).findByUserIdOrderByDoneStatus(userId);
    }

    @Test
    void shouldGetOpenTasksByPerson() {
        // GIVEN
        List<Task> tasks = List.of(testTask);
        when(taskRepository.findOpenTasksByPerson(userId, personId)).thenReturn(tasks);

        // WHEN
        List<Task> result = taskService.getOpenTasksByPerson(userId, personId);

        // THEN
        assertEquals(1, result.size());
        verify(taskRepository, times(1)).findOpenTasksByPerson(userId, personId);
    }

    @Test
    void shouldToggleTaskStatusFromFalseToTrue() {
        // GIVEN
        testTask.setIsDone(false);
        when(taskRepository.findByIdAndUserId(taskId, userId)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);

        // WHEN
        taskService.toggleTaskStatus(taskId, userId);

        // THEN
        verify(taskRepository).save(captor.capture());
        assertTrue(captor.getValue().getIsDone());
    }

    @Test
    void shouldToggleTaskStatusFromTrueToFalse() {
        // GIVEN
        testTask.setIsDone(true);
        when(taskRepository.findByIdAndUserId(taskId, userId)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);

        // WHEN
        taskService.toggleTaskStatus(taskId, userId);

        // THEN
        verify(taskRepository).save(captor.capture());
        assertFalse(captor.getValue().getIsDone());
    }

    @Test
    void shouldThrowExceptionWhenTogglingNonExistentTask() {
        // GIVEN
        when(taskRepository.findByIdAndUserId(taskId, userId)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(ResourceNotFoundException.class, () -> taskService.toggleTaskStatus(taskId, userId));
        verify(taskRepository, never()).save(any());
    }

    @Test
    void shouldMarkTaskAsDone() {
        // GIVEN
        testTask.setIsDone(false);
        when(taskRepository.findByIdAndUserId(taskId, userId)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);

        // WHEN
        taskService.markTaskAsDone(taskId, userId);

        // THEN
        verify(taskRepository).save(captor.capture());
        assertTrue(captor.getValue().getIsDone());
    }

    @Test
    void shouldThrowExceptionWhenMarkingNonExistentTaskAsDone() {
        // GIVEN
        when(taskRepository.findByIdAndUserId(taskId, userId)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(ResourceNotFoundException.class, () -> taskService.markTaskAsDone(taskId, userId));
        verify(taskRepository, never()).save(any());
    }

    @Test
    void shouldMarkTaskAsNotDone() {
        // GIVEN
        testTask.setIsDone(true);
        when(taskRepository.findByIdAndUserId(taskId, userId)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);

        // WHEN
        taskService.markTaskAsNotDone(taskId, userId);

        // THEN
        verify(taskRepository).save(captor.capture());
        assertFalse(captor.getValue().getIsDone());
    }

    @Test
    void shouldThrowExceptionWhenMarkingNonExistentTaskAsNotDone() {
        // GIVEN
        when(taskRepository.findByIdAndUserId(taskId, userId)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(ResourceNotFoundException.class, () -> taskService.markTaskAsNotDone(taskId, userId));
        verify(taskRepository, never()).save(any());
    }

    @Test
    void shouldDeleteTaskSuccessfully() {
        // GIVEN
        when(taskRepository.findByIdAndUserId(taskId, userId)).thenReturn(Optional.of(testTask));

        // WHEN
        taskService.deleteTask(taskId, userId);

        // THEN
        verify(taskRepository, times(1)).deleteByIdAndUserId(taskId, userId);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentTask() {
        // GIVEN
        when(taskRepository.findByIdAndUserId(taskId, userId)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(ResourceNotFoundException.class, () -> taskService.deleteTask(taskId, userId));
        verify(taskRepository, never()).deleteByIdAndUserId(any(), any());
    }

    @Test
    void shouldDeleteAllCompletedTasks() {
        // GIVEN
        Task completedTask1 = new Task();
        completedTask1.setId(UUID.randomUUID());
        completedTask1.setIsDone(true);
        completedTask1.setUserId(userId);

        Task completedTask2 = new Task();
        completedTask2.setId(UUID.randomUUID());
        completedTask2.setIsDone(true);
        completedTask2.setUserId(userId);

        List<Task> completedTasks = List.of(completedTask1, completedTask2);
        when(taskRepository.findByUserIdAndIsDone(userId, true)).thenReturn(completedTasks);

        // WHEN
        taskService.deleteAllCompletedTasks(userId);

        // THEN
        verify(taskRepository, times(1)).findByUserIdAndIsDone(userId, true);
        verify(taskRepository, times(1)).deleteAll(completedTasks);
    }

    @Test
    void shouldHandleDeletionWhenNoCompletedTasksExist() {
        // GIVEN
        when(taskRepository.findByUserIdAndIsDone(userId, true)).thenReturn(List.of());

        // WHEN
        taskService.deleteAllCompletedTasks(userId);

        // THEN
        verify(taskRepository, times(1)).findByUserIdAndIsDone(userId, true);
        verify(taskRepository, times(1)).deleteAll(List.of());
    }
}

