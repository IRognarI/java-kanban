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

        assertNotNull(createdTask.getId(), "ID задачи должен быть установлен");
        assertEquals("Task 1", createdTask.getTitle());
        assertEquals("Description 1", createdTask.getDescription());
        assertEquals(Status.NEW, createdTask.getStatus());
    }

    @Test
    void createEpic() {
        Epic epic = new Epic("Epic 1", "Description 1");
        Epic createdEpic = taskManager.createEpic(epic);

        assertNotNull(createdEpic.getId(), "ID эпика должен быть установлен");
        assertEquals("Epic 1", createdEpic.getTitle());
        assertEquals("Description 1", createdEpic.getDescription());
        assertEquals(Status.NEW, createdEpic.getStatus(), "Новый эпик должен иметь статус NEW");
        assertTrue(createdEpic.getSubtaskId().isEmpty(), "У нового эпика не должно быть подзадач");
    }

    @Test
    void createSubtask() {
        Epic epic = taskManager.createEpic(new Epic("Epic 1", "Description 1"));
        Subtask subtask = new Subtask("Subtask 1", "Description 1", Status.NEW, epic.getId());

        Subtask createdSubtask = taskManager.createSubtask(subtask);

        assertNotNull(createdSubtask.getId(), "ID подзадачи должен быть установлен");
        assertEquals("Subtask 1", createdSubtask.getTitle());
        assertEquals("Description 1", createdSubtask.getDescription());
        assertEquals(Status.NEW, createdSubtask.getStatus());
        assertEquals(epic.getId(), createdSubtask.getEpicId());

        // Проверяем, что эпик знает о своей подзадаче
        assertTrue(taskManager.getEpicById(epic.getId()).getSubtaskId().contains(createdSubtask.getId()));
    }

    @Test
    void getTaskById() {
        Task task = taskManager.createTask(new Task("Task 1", "Description 1"));
        Task retrievedTask = taskManager.getTaskById(task.getId());

        assertEquals(task, retrievedTask);
        assertEquals(task.toString(), retrievedTask.toString(), "Метод toString() должен возвращать одинаковые значения");
    }

    @Test
    void updateTask() {
        Task task = taskManager.createTask(new Task("Task 1", "Description 1"));
        task.setStatus(Status.IN_PROGRESS);
        taskManager.updateTask(task);

        assertEquals(Status.IN_PROGRESS, taskManager.getTaskById(task.getId()).getStatus());
    }

    @Test
    void removeTask() {
        Task task = taskManager.createTask(new Task("Task 1", "Description 1"));
        int taskId = task.getId();

        taskManager.removeTask(taskId);
        assertNull(taskManager.getTaskById(taskId));
    }

    @Test
    void getAllTasks() {
        taskManager.createTask(new Task("Task 1", "Description 1"));
        taskManager.createTask(new Task("Task 2", "Description 2"));

        List<Task> tasks = taskManager.getAllTasks();
        assertEquals(2, tasks.size());
        assertEquals("Task 1", tasks.get(0).getTitle());
        assertEquals("Task 2", tasks.get(1).getTitle());
    }

    @Test
    void updateEpicStatus() {
        Epic epic = taskManager.createEpic(new Epic("Epic 1", "Description 1"));
        Subtask subtask1 = taskManager.createSubtask(new Subtask("Subtask 1", "Desc 1", Status.NEW, epic.getId()));
        Subtask subtask2 = taskManager.createSubtask(new Subtask("Subtask 2", "Desc 2", Status.DONE, epic.getId()));

        Epic updatedEpic = taskManager.getEpicById(epic.getId());
        assertEquals(Status.IN_PROGRESS, updatedEpic.getStatus());

        // Обновляем статус подзадачи и проверяем эпик
        subtask1.setStatus(Status.DONE);
        taskManager.updateSubtask(subtask1);
        assertEquals(Status.DONE, taskManager.getEpicById(epic.getId()).getStatus());
    }

    @Test
    void removedSubtasksShouldNotKeepOldIds() {
        Epic epic = taskManager.createEpic(new Epic("Epic 1", "Description 1"));
        Subtask subtask = taskManager.createSubtask(new Subtask("Subtask 1", "Desc 1", Status.NEW, epic.getId()));
        int subtaskId = subtask.getId();

        taskManager.removeSubtask(subtaskId);
        assertNull(taskManager.getSubtaskById(subtaskId));
    }

    @Test
    void epicShouldNotKeepOutdatedSubtaskIds() {
        Epic epic = taskManager.createEpic(new Epic("Epic 1", "Description 1"));
        Subtask subtask = taskManager.createSubtask(new Subtask("Subtask 1", "Desc 1", Status.NEW, epic.getId()));
        int subtaskId = subtask.getId();

        taskManager.removeSubtask(subtaskId);
        assertFalse(taskManager.getEpicById(epic.getId()).getSubtaskId().contains(subtaskId));
    }

    @Test
    void subtaskToStringShouldContainAllFields() {
        Epic epic = taskManager.createEpic(new Epic("Epic 1", "Description 1"));
        Subtask subtask = taskManager.createSubtask(new Subtask("Subtask 1", "Desc 1", Status.NEW, epic.getId()));

        String subtaskString = subtask.toString();
        assertTrue(subtaskString.contains(String.valueOf(subtask.getId())));
        assertTrue(subtaskString.contains("Subtask 1"));
        assertTrue(subtaskString.contains("NEW"));
        assertTrue(subtaskString.contains("Desc 1"));
        assertTrue(subtaskString.contains(String.valueOf(epic.getId())));
    }
}