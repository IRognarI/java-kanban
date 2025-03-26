package managerTest;

import managers.InMemoryTaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import status.Status;
import tasks.Subtask;
import tasks.Task;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {

    private InMemoryTaskManager taskManager;

    @BeforeEach
    void setUp() {
        taskManager = new InMemoryTaskManager();
    }

    @Test
    void createTask() {
        Task task = new Task("Task 1", "Description 1", Status.NEW);
        Task createdTask = taskManager.createTask(task);

        assertNotNull(createdTask);
        assertEquals(task.getTitle(), createdTask.getTitle());
        assertEquals(task.getDescription(), createdTask.getDescription());
        assertEquals(task.getStatus(), createdTask.getStatus());
    }

    @Test
    void createEpic() {
        Epic epic = new Epic("Epic 1", "Description 1", Status.NEW);
        Epic createdEpic = taskManager.createEpic(epic);

        assertNotNull(createdEpic);
        assertEquals(epic.getTitle(), createdEpic.getTitle());
        assertEquals(epic.getDescription(), createdEpic.getDescription());
        assertEquals(epic.getStatus(), createdEpic.getStatus());
    }

    @Test
    void createSubtask() {
        Epic epic = new Epic("Epic 1", "Description 1", Status.NEW);
        taskManager.createEpic(epic);

        Subtask subtask = new Subtask("Subtask 1", "Description 1", Status.NEW, epic.getId());
        Subtask createdSubtask = taskManager.createSubtask(subtask);

        assertNotNull(createdSubtask);
        assertEquals(subtask.getTitle(), createdSubtask.getTitle());
        assertEquals(subtask.getDescription(), createdSubtask.getDescription());
        assertEquals(subtask.getStatus(), createdSubtask.getStatus());
        assertEquals(subtask.getEpicId(), createdSubtask.getEpicId());
    }

    @Test
    void getTaskById() {
        Task task = new Task("Task 1", "Description 1", Status.NEW);
        Task createdTask = taskManager.createTask(task);

        Task retrievedTask = taskManager.getTaskById(createdTask.getId());
        assertEquals(createdTask, retrievedTask);
    }

    @Test
    void updateTask() {
        Task task = new Task("Task 1", "Description 1", Status.NEW);
        Task createdTask = taskManager.createTask(task);

        createdTask.setStatus(Status.IN_PROGRESS);
        taskManager.updateTask(createdTask);

        Task updatedTask = taskManager.getTaskById(createdTask.getId());
        assertEquals(Status.IN_PROGRESS, updatedTask.getStatus());
    }

    @Test
    void removeTask() {
        Task task = new Task("Task 1", "Description 1", Status.NEW);
        Task createdTask = taskManager.createTask(task);

        taskManager.removeTask(createdTask.getId());
        assertNull(taskManager.getTaskById(createdTask.getId()));
    }

    @Test
    void getAllTasks() {
        Task task1 = new Task("Task 1", "Description 1", Status.NEW);
        Task task2 = new Task("Task 2", "Description 2", Status.NEW);

        taskManager.createTask(task1);
        taskManager.createTask(task2);

        List<Task> tasks = taskManager.getAllTasks();
        assertEquals(2, tasks.size());
    }

    @Test
    void updateEpicStatus() {
        Epic epic = new Epic("Epic 1", "Description 1", Status.NEW);
        taskManager.createEpic(epic);

        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", Status.NEW, epic.getId());
        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", Status.DONE, epic.getId());

        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);

        Epic updatedEpic = taskManager.getEpicById(epic.getId());
        assertEquals(Status.IN_PROGRESS, updatedEpic.getStatus());
    }

    @Test
    void removedSubtasksShouldNotKeepOldIds() {
        Epic epic = new Epic("Epic 1", "Description 1", Status.NEW);
        taskManager.createEpic(epic);

        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", Status.NEW, epic.getId());
        Subtask createdSubtask1 = taskManager.createSubtask(subtask1);

        int subtaskId = createdSubtask1.getId();
        taskManager.removeSubtask(subtaskId);

        // Проверяем, что подзадача больше не существует
        assertNull(taskManager.getSubtaskById(subtaskId));
    }

    @Test
    void epicShouldNotKeepOutdatedSubtaskIds() {
        Epic epic = new Epic("Epic 1", "Description 1", Status.NEW);
        taskManager.createEpic(epic);

        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", Status.NEW, epic.getId());
        Subtask createdSubtask1 = taskManager.createSubtask(subtask1);

        int subtaskId = createdSubtask1.getId();
        taskManager.removeSubtask(subtaskId);

        // Проверяем, что id подзадачи удален из списка подзадач эпика
        Epic updatedEpic = taskManager.getEpicById(epic.getId());
        assertFalse(updatedEpic.getSubtaskId().contains(subtaskId));
    }
}