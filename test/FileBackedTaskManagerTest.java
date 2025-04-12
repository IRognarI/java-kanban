import exception.ManagerSaveException;
import managers.FileBackedTaskManager;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import status.Status;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

class FileBackedTaskManagerTest {
    private File tempFile;
    private FileBackedTaskManager manager;
    private LocalDateTime testTime;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = File.createTempFile("tasks", ".csv");
        manager = new FileBackedTaskManager(tempFile);
        testTime = LocalDateTime.now();
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(tempFile.toPath());
    }

    @Test
    void loadFromFileShouldRestoreEmptyManagerFromEmptyFile() {
        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);

        assertTrue(loaded.getAllTasks().isEmpty(), "Нет задач");
        assertTrue(loaded.getAllEpics().isEmpty(), "Нет эпиков");
        assertTrue(loaded.getAllSubtasks().isEmpty(), "Нет подзадач");
        assertTrue(loaded.getHistory().isEmpty(), "Нет истории");
    }

    @Test
    void loadFromFileShouldRestoreTasksCorrectly() {
        Task task = new Task("Task", "Desc", Status.NEW,
                testTime, Duration.ofMinutes(30));
        manager.createTask(task);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);
        Task loadedTask = loaded.getTaskById(task.getId());

        assertEquals(task.getTitle(), loadedTask.getTitle(), "Название задачи совпадает");
        assertEquals(task.getDescription(), loadedTask.getDescription(), "Описание совпадает");
        assertEquals(task.getStartTime(), loadedTask.getStartTime(), "Время начала совпадает");
    }

    @Test
    void loadFromFileShouldRestoreEpicSubtasksRelation() {
        Epic epic = new Epic("Epic", "Desc", Status.NEW, null, null);
        manager.createEpic(epic);
        Subtask subtask = new Subtask("Subtask", "Desc", Status.NEW, epic.getId(),
                testTime, Duration.ofMinutes(15));
        manager.createSubtask(subtask);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);
        List<Subtask> epicSubtasks = loaded.getSubtasksByEpicId(epic.getId());

        assertEquals(1, epicSubtasks.size(), "Эпик должен содержать подзадачу");
        assertEquals(subtask.getId(), epicSubtasks.get(0).getId(), "ID подзадачи совпадает");
    }

    @Test
    void loadFromFileShouldRestoreHistory() {
        Task task = new Task("Task", "Desc", Status.NEW,
                testTime, Duration.ofMinutes(30));
        manager.createTask(task);
        manager.getTaskById(task.getId()); // Добавляем в историю

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);

        assertEquals(1, loaded.getHistory().size(), "История должна восстановиться");
        assertEquals(task.getId(), loaded.getHistory().get(0).getId(), "ID задачи в истории совпадает");
    }

    /*@Test
    void saveShouldWorkWithEmptyManager() {
        assertDoesNotThrow(() -> manager.save(),
                "Сохранение пустого менеджера не должно вызывать ошибок");
    }*/

    /*@Test
    void saveShouldWorkWithNullValues() {
        Epic epic = new Epic("Epic", null, Status.NEW, null, null);
        assertDoesNotThrow(() -> {
            manager.createEpic(epic);
            manager.save();
        }, "Сохранение null-значений не должно вызывать ошибок");
    }*/

    @Test
    void saveShouldPreserveTaskOrder() {
        Task task1 = new Task("Task1", "Desc", Status.NEW,
                testTime, Duration.ofMinutes(30));
        Task task2 = new Task("Task2", "Desc", Status.NEW,
                testTime.plusHours(1), Duration.ofMinutes(30));

        manager.createTask(task1);
        manager.createTask(task2);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);
        List<Task> tasks = loaded.getAllTasks();

        assertEquals(2, tasks.size(), "Должны быть две задачи");
        assertEquals(task1.getId(), tasks.get(0).getId(), "Порядок задач должен сохраняться");
    }

    @Test
    void loadFromFileShouldHandleCorruptedFile() throws IOException {
        // Создаем битый файл
        Files.writeString(tempFile.toPath(), "id,type,name\ncorrupted,data,here");

        assertDoesNotThrow(() -> FileBackedTaskManager.loadFromFile(tempFile),
                "Загрузка битого файла не должна вызывать исключений");
    }

    @Test
    void loadFromFileShouldHandleMissingFile() {
        File notExists = new File("not_exists.csv");
        assertThrows(ManagerSaveException.class, () -> FileBackedTaskManager.loadFromFile(notExists),
                "Попытка загрузить несуществующий файл должна вызывать исключение");
    }
}