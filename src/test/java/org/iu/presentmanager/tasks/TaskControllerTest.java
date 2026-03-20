package org.iu.presentmanager.tasks;

import org.iu.presentmanager.config.WebConfig;
import org.iu.presentmanager.exceptions.ResourceNotFoundException;
import org.iu.presentmanager.security.SecurityConfig;
import org.iu.presentmanager.security.SupabaseJwtConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import tools.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
@ActiveProfiles("test")
@Import({SecurityConfig.class, WebConfig.class})
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TaskService taskService;

    @MockitoBean
    private SupabaseJwtConverter supabaseJwtConverter;

    private final UUID userId   = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private final UUID personId = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private final UUID taskId   = UUID.fromString("00000000-0000-0000-0000-000000000003");

    private Task testTask;

    @BeforeEach
    void setUp() {
        testTask = createTestTask("Test Task", false);
    }

    private Task createTestTask(String title, boolean isDone) {
        Task task = new Task();
        task.setId(taskId);
        task.setUserId(userId);
        task.setPersonId(personId);
        task.setTitle(title);
        task.setIsDone(isDone);
        return task;
    }

    @Test
    void shouldCreateTaskAndReturnCreatedStatus() throws Exception {
        // GIVEN
        Task newTask = new Task();
        newTask.setPersonId(personId);
        newTask.setTitle("New Task");
        newTask.setIsDone(false);

        when(taskService.createTask(any(Task.class), eq(userId))).thenReturn(testTask);

        // WHEN & THEN
        mockMvc.perform(post("/tasks")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newTask)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(taskId.toString()))
                .andExpect(jsonPath("$.title").value("Test Task"))
                .andExpect(jsonPath("$.isDone").value(false));

        verify(taskService).createTask(any(Task.class), eq(userId));
    }

    @Test
    void shouldReturnBadRequestWhenCreatingTaskWithMissingTitle() throws Exception {
        // GIVEN — title fehlt (@NotBlank)
        String invalidTaskJson = "{\"personId\":\"" + personId + "\",\"isDone\":false}";

        // WHEN & THEN
        mockMvc.perform(post("/tasks")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON)
                        .content(invalidTaskJson))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

        verify(taskService, never()).createTask(any(), any());
    }

    @Test
    void shouldReturnBadRequestWhenCreatingTaskWithMissingPersonId() throws Exception {
        // GIVEN — personId fehlt (@NotNull)
        String invalidTaskJson = "{\"title\":\"Task\",\"isDone\":false}";

        // WHEN & THEN
        mockMvc.perform(post("/tasks")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON)
                        .content(invalidTaskJson))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

        verify(taskService, never()).createTask(any(), any());
    }

    @Test
    void shouldReturnNotFoundWhenPersonDoesNotExistOnCreate() throws Exception {
        // GIVEN
        Task newTask = new Task();
        newTask.setPersonId(personId);
        newTask.setTitle("Task");
        newTask.setIsDone(false);

        when(taskService.createTask(any(Task.class), eq(userId)))
                .thenThrow(new ResourceNotFoundException("Person not found with id: " + personId));

        // WHEN & THEN
        mockMvc.perform(post("/tasks")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newTask)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound());

        verify(taskService).createTask(any(Task.class), eq(userId));
    }

    @Test
    void shouldReturnAllTasksWhenNoFilterProvided() throws Exception {
        // GIVEN
        when(taskService.getAllTasksByUser(userId)).thenReturn(List.of(testTask));

        // WHEN & THEN
        mockMvc.perform(get("/tasks")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(taskId.toString()))
                .andExpect(jsonPath("$[0].title").value("Test Task"));

        verify(taskService).getAllTasksByUser(userId);
        verifyNoMoreInteractions(taskService);
    }

    @Test
    void shouldReturnEmptyListWhenNoTasksExist() throws Exception {
        // GIVEN
        when(taskService.getAllTasksByUser(userId)).thenReturn(Collections.emptyList());

        // WHEN & THEN
        mockMvc.perform(get("/tasks")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(taskService).getAllTasksByUser(userId);
    }

    @Test
    void shouldFilterTasksByPersonId() throws Exception {
        // GIVEN
        when(taskService.getTasksByPerson(userId, personId)).thenReturn(List.of(testTask));

        // WHEN & THEN
        mockMvc.perform(get("/tasks")
                        .param("personId", personId.toString())
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(taskService).getTasksByPerson(userId, personId);
        verify(taskService, never()).getAllTasksByUser(any());
    }

    @Test
    void shouldFilterTasksByIsDone() throws Exception {
        // GIVEN
        when(taskService.getTasksByStatus(userId, false)).thenReturn(List.of(testTask));

        // WHEN & THEN
        mockMvc.perform(get("/tasks")
                        .param("isDone", "false")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].isDone").value(false));

        verify(taskService).getTasksByStatus(userId, false);
    }

    @Test
    void shouldFilterTasksByPersonAndIsDone() throws Exception {
        // GIVEN
        when(taskService.getTasksByPersonAndStatus(userId, personId, false)).thenReturn(List.of(testTask));

        // WHEN & THEN
        mockMvc.perform(get("/tasks")
                        .param("personId", personId.toString())
                        .param("isDone", "false")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(taskService).getTasksByPersonAndStatus(userId, personId, false);
    }

    @Test
    void shouldReturnNotFoundWhenPersonDoesNotExistOnFilter() throws Exception {
        // GIVEN
        when(taskService.getTasksByPerson(userId, personId))
                .thenThrow(new ResourceNotFoundException("Person not found with id: " + personId));

        // WHEN & THEN
        mockMvc.perform(get("/tasks")
                        .param("personId", personId.toString())
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound());

        verify(taskService).getTasksByPerson(userId, personId);
    }

    @Test
    void shouldReturnTaskById() throws Exception {
        // GIVEN
        when(taskService.getTaskById(taskId, userId)).thenReturn(testTask);

        // WHEN & THEN
        mockMvc.perform(get("/tasks/{id}", taskId)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId.toString()))
                .andExpect(jsonPath("$.title").value("Test Task"));

        verify(taskService).getTaskById(taskId, userId);
    }

    @Test
    void shouldReturnNotFoundWhenTaskDoesNotExist() throws Exception {
        // GIVEN
        when(taskService.getTaskById(taskId, userId))
                .thenThrow(new ResourceNotFoundException("Task not found with id: " + taskId));

        // WHEN & THEN
        mockMvc.perform(get("/tasks/{id}", taskId)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound());

        verify(taskService).getTaskById(taskId, userId);
    }

    @Test
    void shouldReturnTasksOrderedByStatus() throws Exception {
        // GIVEN
        Task doneTask = createTestTask("Done Task", true);
        when(taskService.getTasksOrderedByStatus(userId))
                .thenReturn(List.of(testTask, doneTask));

        // WHEN & THEN
        mockMvc.perform(get("/tasks/ordered")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].isDone").value(false))
                .andExpect(jsonPath("$[1].isDone").value(true));

        verify(taskService).getTasksOrderedByStatus(userId);
    }

    @Test
    void shouldReturnOpenTasksByPerson() throws Exception {
        // GIVEN
        when(taskService.getOpenTasksByPerson(userId, personId)).thenReturn(List.of(testTask));

        // WHEN & THEN
        mockMvc.perform(get("/tasks/open/person/{personId}", personId)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].isDone").value(false));

        verify(taskService).getOpenTasksByPerson(userId, personId);
    }

    @Test
    void shouldReturnEmptyListWhenNoOpenTasksForPerson() throws Exception {
        // GIVEN
        when(taskService.getOpenTasksByPerson(userId, personId)).thenReturn(Collections.emptyList());

        // WHEN & THEN
        mockMvc.perform(get("/tasks/open/person/{personId}", personId)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(taskService).getOpenTasksByPerson(userId, personId);
    }

    @Test
    void shouldToggleTaskStatusFromFalseToTrue() throws Exception {
        // GIVEN
        Task toggledTask = createTestTask("Test Task", true);
        when(taskService.toggleTaskStatus(taskId, userId)).thenReturn(toggledTask);

        // WHEN & THEN
        mockMvc.perform(patch("/tasks/{id}/toggle", taskId)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isDone").value(true));

        verify(taskService).toggleTaskStatus(taskId, userId);
    }

    @Test
    void shouldReturnNotFoundWhenTogglingNonExistentTask() throws Exception {
        // GIVEN
        when(taskService.toggleTaskStatus(taskId, userId))
                .thenThrow(new ResourceNotFoundException("Task not found with id: " + taskId));

        // WHEN & THEN
        mockMvc.perform(patch("/tasks/{id}/toggle", taskId)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound());

        verify(taskService).toggleTaskStatus(taskId, userId);
    }

    @Test
    void shouldMarkTaskAsDone() throws Exception {
        // GIVEN
        Task doneTask = createTestTask("Test Task", true);
        when(taskService.markTaskAsDone(taskId, userId)).thenReturn(doneTask);

        // WHEN & THEN
        mockMvc.perform(patch("/tasks/{id}/done", taskId)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isDone").value(true));

        verify(taskService).markTaskAsDone(taskId, userId);
    }

    @Test
    void shouldReturnNotFoundWhenMarkingDoneNonExistentTask() throws Exception {
        // GIVEN
        when(taskService.markTaskAsDone(taskId, userId))
                .thenThrow(new ResourceNotFoundException("Task not found with id: " + taskId));

        // WHEN & THEN
        mockMvc.perform(patch("/tasks/{id}/done", taskId)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound());

        verify(taskService).markTaskAsDone(taskId, userId);
    }

    @Test
    void shouldMarkTaskAsNotDone() throws Exception {
        // GIVEN
        Task notDoneTask = createTestTask("Test Task", false);
        when(taskService.markTaskAsNotDone(taskId, userId)).thenReturn(notDoneTask);

        // WHEN & THEN
        mockMvc.perform(patch("/tasks/{id}/not-done", taskId)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isDone").value(false));

        verify(taskService).markTaskAsNotDone(taskId, userId);
    }

    @Test
    void shouldReturnNotFoundWhenMarkingNotDoneNonExistentTask() throws Exception {
        // GIVEN
        when(taskService.markTaskAsNotDone(taskId, userId))
                .thenThrow(new ResourceNotFoundException("Task not found with id: " + taskId));

        // WHEN & THEN
        mockMvc.perform(patch("/tasks/{id}/not-done", taskId)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound());

        verify(taskService).markTaskAsNotDone(taskId, userId);
    }

    @Test
    void shouldDeleteTaskAndReturnNoContent() throws Exception {
        // GIVEN
        doNothing().when(taskService).deleteTask(taskId, userId);

        // WHEN & THEN
        mockMvc.perform(delete("/tasks/{id}", taskId)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNoContent());

        verify(taskService).deleteTask(taskId, userId);
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentTask() throws Exception {
        // GIVEN
        doThrow(new ResourceNotFoundException("Task not found with id: " + taskId))
                .when(taskService).deleteTask(taskId, userId);

        // WHEN & THEN
        mockMvc.perform(delete("/tasks/{id}", taskId)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound());

        verify(taskService).deleteTask(taskId, userId);
    }

    @Test
    void shouldDeleteAllCompletedTasksAndReturnNoContent() throws Exception {
        // GIVEN
        doNothing().when(taskService).deleteAllCompletedTasks(userId);

        // WHEN & THEN
        mockMvc.perform(delete("/tasks/completed")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNoContent());

        verify(taskService).deleteAllCompletedTasks(userId);
    }
}