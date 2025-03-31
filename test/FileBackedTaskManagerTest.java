import status.Status;
import managers.FileBackedTaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class FileBackedTaskManagerTest {
    private File tempFile;
    private FileBackedTaskManager manager;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = File.createTempFile("tasks", ".csv");
        manager = new FileBackedTaskManager(tempFile);
    }

    @AfterEach
    void tearDown() {
        tempFile.delete();
    }

    @Test
    void shouldSaveAndLoadEmptyFile() {
        manager.getMethodSave();

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        assertTrue(loadedManager.getAllTasks().isEmpty());
        assertTrue(loadedManager.getAllEpics().isEmpty());
        assertTrue(loadedManager.getAllSubtasks().isEmpty());
    }

    @Test
    void shouldSaveMultipleTasks() {

        Epic epic = manager.createEpic(new Epic("Epic 1", "Description"));

        Subtask subtask = manager.createSubtask(new Subtask("Subtask 1", "Description", Status.NEW, epic.getId()));

        Task task = manager.createTask(new Task("Task 1", "Description"));

        manager.getMethodSave();

        try {
            String content = Files.readString(tempFile.toPath());
            assertTrue(content.contains("Task 1"));
            assertTrue(content.contains("Epic 1"));
            assertTrue(content.contains("Subtask 1"));
        } catch (IOException e) {
            fail("Ошибка чтения файла", e);
        }
    }
}