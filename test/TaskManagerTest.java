import exception.ManagerSaveException;
import managers.*;
import org.junit.jupiter.api.*;
import tasks.*;
import status.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.*;
import java.util.*;

public abstract class TaskManagerTest<T extends TaskManager> {
    protected T taskManager;
    protected LocalDateTime testTime;

    protected abstract T createTaskManager();

    @BeforeEach
    void setUp() {
        taskManager = createTaskManager();
        testTime = LocalDateTime.now();
    }

    @Test
    void shouldHandleSubtasksWithNewStatus() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Desc", Status.NEW, null, null));
        Subtask subtask = taskManager.createSubtask(
                new Subtask("Subtask", "Desc", Status.NEW, epic.getId(),
                        testTime, Duration.ofMinutes(30)));

        Assertions.assertEquals(Status.NEW, subtask.getStatus());
        Assertions.assertEquals(epic.getId(), subtask.getEpicId());
        Assertions.assertEquals(Status.NEW, epic.getStatus());
    }

    @Test
    void shouldHandleSubtasksWithMixedStatuses() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Desc", Status.NEW, null, null));

        Subtask sub1 = taskManager.createSubtask(
                new Subtask("Sub1", "Desc", Status.NEW, epic.getId(),
                        testTime, Duration.ofMinutes(30)));
        Subtask sub2 = taskManager.createSubtask(
                new Subtask("Sub2", "Desc", Status.DONE, epic.getId(),
                        testTime.plusHours(1), Duration.ofMinutes(30)));

        Assertions.assertEquals(Status.IN_PROGRESS, epic.getStatus());
    }

    @Test
    void shouldHandleSubtasksWithInProgressStatus() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Desc", Status.NEW, null, null));
        Subtask subtask = taskManager.createSubtask(
                new Subtask("Subtask", "Desc", Status.IN_PROGRESS, epic.getId(),
                        testTime, Duration.ofMinutes(30)));

        Assertions.assertEquals(Status.IN_PROGRESS, subtask.getStatus());
        Assertions.assertEquals(Status.IN_PROGRESS, epic.getStatus());
    }

    @Test
    void shouldDetectTimeIntersections() {
        Task task1 = taskManager.createTask(
                new Task("Task1", "Desc", Status.NEW,
                        testTime, Duration.ofMinutes(30)));
        Task task2 = taskManager.createTask(
                new Task("Task2", "Desc", Status.NEW,
                        testTime.plusMinutes(15), Duration.ofMinutes(30)));

        Assertions.assertTrue(taskManager.lookingForTemporaryIntersectionsInTasks());
    }

    @Test
    void shouldPrioritizeTasksCorrectly() {
        LocalDateTime earlierTime = LocalDateTime.of(2023, 1, 1, 10, 0);
        LocalDateTime laterTime = earlierTime.plusHours(1);


        Task laterTask = taskManager.createTask(
                new Task("Later", "Desc", Status.NEW, laterTime, Duration.ofMinutes(30)));
        Task earlierTask = taskManager.createTask(
                new Task("Earlier", "Desc", Status.NEW, earlierTime, Duration.ofMinutes(30)));


        Set<? extends Task> prioritized = taskManager.getPrioritizedTasks(taskManager.getAllTasks());


        Assertions.assertFalse(prioritized.isEmpty(), "Список приоритетов не должен быть пустым");


        List<Task> prioritizedList = new ArrayList<>(prioritized);


        Assertions.assertEquals(earlierTask.getId(), prioritizedList.get(0).getId(),
                "Первой должна быть задача с более ранним временем начала");
        Assertions.assertEquals(laterTask.getId(), prioritizedList.get(1).getId(),
                "Второй должна быть задача с более поздним временем начала");
    }
}

class InMemoryTaskManagerTestTwo extends TaskManagerTest<InMemoryTaskManager> {
    @Override
    protected InMemoryTaskManager createTaskManager() {
        return new InMemoryTaskManager();
    }
}

class HistoryManagerTest {
    private HistoryManager historyManager;

    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager();
    }

    @Test
    void shouldHandleEmptyHistory() {
        Assertions.assertTrue(historyManager.getHistory().isEmpty());
    }

    @Test
    void shouldNotDuplicateTasks() {
        Task task = new Task("Task", "Desc", Status.NEW, null, null);
        task.setId(1);

        historyManager.add(task);
        historyManager.add(task);

        Assertions.assertEquals(1, historyManager.getHistory().size());
    }

    @Test
    void shouldRemoveFromHistory() {
        Task task1 = new Task("Task1", "Desc", Status.NEW, null, null);
        Task task2 = new Task("Task2", "Desc", Status.NEW, null, null);
        Task task3 = new Task("Task3", "Desc", Status.NEW, null, null);

        task1.setId(1);
        task2.setId(2);
        task3.setId(3);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);


        historyManager.remove(1);
        Assertions.assertEquals(List.of(task2, task3), historyManager.getHistory());


        historyManager.add(task1);
        historyManager.remove(2);
        Assertions.assertEquals(List.of(task3, task1), historyManager.getHistory());


        historyManager.remove(1);
        Assertions.assertEquals(List.of(task3), historyManager.getHistory());
    }
}

class FileBackedTaskManagerTestTwo {
    private File tempFile;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = File.createTempFile("tasks", ".csv");
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(tempFile.toPath());
    }

    @Test
    void shouldThrowExceptionWhenFileNotExists() {
        File notExists = new File("not_exists.csv");
        Assertions.assertThrows(ManagerSaveException.class,
                () -> FileBackedTaskManager.loadFromFile(notExists));
    }
}