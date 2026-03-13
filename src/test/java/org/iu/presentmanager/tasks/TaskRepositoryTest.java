package org.iu.presentmanager.tasks;

import org.iu.presentmanager.persons.Person;
import org.iu.presentmanager.persons.PersonStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class TaskRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TaskRepository taskRepository;

    private UUID userId;
    private UUID otherUserId;
    private UUID personId;
    private UUID personId2;
    private Task testTask1;

    private Person createTestPerson(String name, UUID userId) {
        Person person = new Person();
        person.setName(name);
        person.setUserId(userId);
        person.setStatus(PersonStatus.PLANNED);
        person.setBirthday(LocalDate.of(1990, 1, 1));
        return person;
    }

    private Task createTestTask(UUID personId, UUID userId, String title, Boolean isDone) {
        Task task = new Task();
        task.setPersonId(personId);
        task.setUserId(userId);
        task.setTitle(title);
        task.setIsDone(isDone);
        return task;
    }

    @BeforeEach
    void setUp() {
        userId = UUID.fromString("10000000-0000-0000-0000-000000000001");
        otherUserId = UUID.fromString("20000000-0000-0000-0000-000000000002");

        // STEP 1: Persiste Persons zuerst
        Person person1 = createTestPerson("Alice", userId);
        Person person2 = createTestPerson("Bob", userId);
        Person person3 = createTestPerson("Charlie", otherUserId);

        entityManager.persistAndFlush(person1);
        entityManager.persistAndFlush(person2);
        entityManager.persistAndFlush(person3);

        // STEP 2: Hole generierte Person-IDs
        personId = person1.getId();
        personId2 = person2.getId();

        // STEP 3: Erstelle und persiste Tasks mit den echten Person-IDs
        testTask1 = createTestTask(personId, userId, "Buy flowers", false);
        Task task2 = createTestTask(personId, userId, "Write card", true);
        Task task3 = createTestTask(personId2, userId, "Plan party", false);
        Task task4 = createTestTask(personId, otherUserId, "Buy gift", false);

        entityManager.persistAndFlush(testTask1);
        entityManager.persistAndFlush(task2);
        entityManager.persistAndFlush(task3);
        entityManager.persistAndFlush(task4);
    }

    @Test
    void shouldFindAllTasksByUserId() {
        // WHEN
        List<Task> result = taskRepository.findByUserId(userId);

        // THEN
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.stream().allMatch(t -> t.getUserId().equals(userId)));
    }

    @Test
    void shouldReturnEmptyListForUserWithoutTasks() {
        // GIVEN
        UUID nonExistentUserId = UUID.fromString("99999999-0000-0000-0000-000000000099");

        // WHEN
        List<Task> result = taskRepository.findByUserId(nonExistentUserId);

        // THEN
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldIsolateBetweenUsers() {
        // WHEN
        List<Task> userResult = taskRepository.findByUserId(userId);
        List<Task> otherUserResult = taskRepository.findByUserId(otherUserId);

        // THEN
        assertEquals(3, userResult.size());
        assertEquals(1, otherUserResult.size());
    }

    @Test
    void shouldFindTaskByIdAndUserId() {
        // WHEN
        Optional<Task> result = taskRepository.findByIdAndUserId(testTask1.getId(), userId);

        // THEN
        assertTrue(result.isPresent());
        assertEquals("Buy flowers", result.get().getTitle());
    }

    @Test
    void shouldNotFindTaskWithWrongUserId() {
        // WHEN
        Optional<Task> result = taskRepository.findByIdAndUserId(testTask1.getId(), otherUserId);

        // THEN
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldFindTasksByPersonId() {
        // WHEN
        List<Task> result = taskRepository.findByUserIdAndPersonId(userId, personId);

        // THEN
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(t -> t.getPersonId().equals(personId)));
    }

    @Test
    void shouldReturnEmptyListForNonExistentPerson() {
        // WHEN
        List<Task> result = taskRepository.findByUserIdAndPersonId(userId, UUID.randomUUID());

        // THEN
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldFindTasksByStatus() {
        // WHEN
        List<Task> openTasks = taskRepository.findByUserIdAndIsDone(userId, false);
        List<Task> completedTasks = taskRepository.findByUserIdAndIsDone(userId, true);

        // THEN
        assertEquals(2, openTasks.size());
        assertEquals(1, completedTasks.size());
        assertTrue(openTasks.stream().allMatch(t -> !t.getIsDone()));
        assertTrue(completedTasks.stream().allMatch(Task::getIsDone));
    }

    @Test
    void shouldFindTasksByPersonAndStatus() {
        // WHEN
        List<Task> result = taskRepository.findByUserIdAndPersonIdAndIsDone(userId, personId, false);

        // THEN
        assertEquals(1, result.size());
        assertEquals("Buy flowers", result.getFirst().getTitle());
        assertFalse(result.getFirst().getIsDone());
    }

    @Test
    void shouldFindTasksOrderedByStatus() {
        // WHEN
        List<Task> result = taskRepository.findByUserIdOrderByDoneStatus(userId);

        // THEN
        assertEquals(3, result.size());
        assertFalse(result.get(0).getIsDone());
        assertFalse(result.get(1).getIsDone());
        assertTrue(result.get(2).getIsDone());
    }

    @Test
    void shouldFindOpenTasksByPerson() {
        // WHEN
        List<Task> result = taskRepository.findOpenTasksByPerson(userId, personId);

        // THEN
        assertEquals(1, result.size());
        assertEquals("Buy flowers", result.getFirst().getTitle());
        assertFalse(result.getFirst().getIsDone());
    }

    @Test
    void shouldDeleteTaskByUserIdAndId() {
        // GIVEN
        UUID taskIdToDelete = testTask1.getId();

        // WHEN
        taskRepository.deleteByIdAndUserId(taskIdToDelete, userId);

        // THEN
        Optional<Task> result = taskRepository.findByIdAndUserId(taskIdToDelete, userId);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldNotDeleteTaskWithWrongUserId() {
        // GIVEN
        UUID taskIdToDelete = testTask1.getId();

        // WHEN
        taskRepository.deleteByIdAndUserId(taskIdToDelete, otherUserId);

        // THEN
        Optional<Task> result = taskRepository.findByIdAndUserId(taskIdToDelete, userId);
        assertTrue(result.isPresent());
    }
}





