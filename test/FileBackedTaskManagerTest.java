import managers.FileBackedTaskManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import status.Status;
import tasks.Task;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;

class FileBackedTaskManagerTest {
    private File tempFile;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = File.createTempFile("tasks", ".csv");
        tempFile.deleteOnExit();
    }

    @Test
    void shouldLoadTasksFromFile() throws IOException {
       
        String csvData = """
            id,type,name,status,description,startTime,duration,epic
            1,TASK,Task 1,NEW,Description,2023-01-01T10:00,30,
            """;
        Files.writeString(tempFile.toPath(), csvData);

       
        FileBackedTaskManager manager = FileBackedTaskManager.loadFromFile(tempFile);
        Task task = manager.getTaskById(1);

        Assertions.assertNotNull(task, "Задача должна быть загружена");
        Assertions.assertEquals("Task 1", task.getTitle());
    }

    @Test
    void shouldSaveWhenTaskAdded() {
        FileBackedTaskManager manager = new FileBackedTaskManager(tempFile);

       
        manager.createTask(new Task("Test", "Desc", Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(30)));

       
        Assertions.assertTrue(tempFile.exists());
        Assertions.assertTrue(tempFile.length() > 0);
    }

    @Test
    void shouldHandleEmptyFile() throws IOException {
       
        Files.writeString(tempFile.toPath(), "");

        Assertions.assertDoesNotThrow(() -> {
            FileBackedTaskManager manager = FileBackedTaskManager.loadFromFile(tempFile);
            Assertions.assertTrue(manager.getAllTasks().isEmpty());
        });
    }
}