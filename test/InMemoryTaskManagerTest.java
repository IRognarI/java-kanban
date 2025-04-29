import managers.InMemoryTaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import enums.Status;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {
    private InMemoryTaskManager taskManager;
    private LocalDateTime testTime;

    @BeforeEach
    void setUp() {
        taskManager = new InMemoryTaskManager();
        testTime = LocalDateTime.now();
    }

   
    @Test
    void createTaskShouldAddTaskWithGeneratedId() {
        Task task = new Task("Task 1", "Description", Status.NEW,
                testTime, Duration.ofMinutes(30));
        Task createdTask = taskManager.createTask(task);

        assertNotNull(createdTask.getId(), "Задача должна получить ID");
        assertEquals(task, taskManager.getTaskById(createdTask.getId()),
                "Задача должна сохраняться в менеджере");
    }

   
    @Test
    void createEpicShouldInitializeEmptySubtasksList() {
        Epic epic = new Epic("Epic 1", "Description", Status.NEW, null, null);
        Epic createdEpic = taskManager.createEpic(epic);

        assertTrue(taskManager.getSubtasksByEpicId(createdEpic.getId()).isEmpty(),
                "Новый эпик должен иметь пустой список подзадач");
    }

   
    @Test
    void createSubtaskShouldAddToEpic() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Desc", Status.NEW, null, null));
        Subtask subtask = new Subtask("Subtask", "Desc", Status.NEW, epic.getId(),
                testTime, Duration.ofMinutes(15));

        Subtask createdSubtask = taskManager.createSubtask(subtask);
        assertEquals(1, taskManager.getSubtasksByEpicId(epic.getId()).size(),
                "Подзадача должна добавляться в эпик");
    }

   
    @Test
    void getTaskByIdShouldReturnCorrectTask() {
        Task task = taskManager.createTask(new Task("Task", "Desc", Status.NEW,
                testTime, Duration.ofMinutes(30)));

        Task retrieved = taskManager.getTaskById(task.getId());
        assertEquals(task, retrieved, "Должна возвращаться корректная задача");
    }

   
    @Test
    void getEpicByIdShouldReturnCorrectEpic() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Desc", Status.NEW, null, null));

        Epic retrieved = taskManager.getEpicById(epic.getId());
        assertEquals(epic, retrieved, "Должен возвращаться корректный эпик");
    }

   
    @Test
    void getSubtaskByIdShouldReturnCorrectSubtask() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Desc", Status.NEW, null, null));
        Subtask subtask = taskManager.createSubtask(
                new Subtask("Subtask", "Desc", Status.NEW, epic.getId(),
                        testTime, Duration.ofMinutes(15)));

        Subtask retrieved = taskManager.getSubtaskById(subtask.getId());
        assertEquals(subtask, retrieved, "Должна возвращаться корректная подзадача");
    }

   
    @Test
    void updateTaskShouldChangeTaskData() {
        Task task = taskManager.createTask(new Task("Task", "Desc", Status.NEW,
                testTime, Duration.ofMinutes(30)));
        Task updated = new Task("Updated", "New Desc", Status.DONE,
                testTime.plusHours(1), Duration.ofHours(1));
        updated.setId(task.getId());

        taskManager.updateTask(updated);
        Task result = taskManager.getTaskById(task.getId());

        assertEquals("Updated", result.getTitle(), "Название должно обновиться");
        assertEquals(Status.DONE, result.getStatus(), "Статус должен обновиться");
    }

   
    @Test
    void updateEpicShouldChangeEpicData() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Desc", Status.NEW, null, null));
        Epic updated = new Epic("Updated", "New Desc", Status.DONE, null, null);
        updated.setId(epic.getId());

        taskManager.updateEpic(updated);
        Epic result = taskManager.getEpicById(epic.getId());

        assertEquals("Updated", result.getTitle(), "Название должно обновиться");
    }

   
    @Test
    void updateSubtaskShouldChangeSubtaskAndEpicStatus() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Desc", Status.NEW, null, null));
        Subtask subtask = taskManager.createSubtask(
                new Subtask("Subtask", "Desc", Status.NEW, epic.getId(),
                        testTime, Duration.ofMinutes(15)));

        Subtask updated = new Subtask("Updated", "New Desc", Status.DONE, epic.getId(),
                testTime, Duration.ofMinutes(15));
        updated.setId(subtask.getId());
        taskManager.updateSubtask(updated);

        assertEquals(Status.DONE, taskManager.getEpicById(epic.getId()).getStatus(),
                "Статус эпика должен обновиться");
    }

   
    @Test
    void removeTaskShouldDeleteTask() {
        Task task = taskManager.createTask(new Task("Task", "Desc", Status.NEW,
                testTime, Duration.ofMinutes(30)));
        taskManager.removeTask(task.getId());

        assertNull(taskManager.getTaskById(task.getId()), "Задача должна удалиться");
    }

   
    @Test
    void removeEpicShouldDeleteEpicAndSubtasks() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Desc", Status.NEW, null, null));
        Subtask subtask = taskManager.createSubtask(
                new Subtask("Subtask", "Desc", Status.NEW, epic.getId(),
                        testTime, Duration.ofMinutes(15)));

        taskManager.removeEpic(epic.getId());
        assertNull(taskManager.getEpicById(epic.getId()), "Эпик должен удалиться");
        assertNull(taskManager.getSubtaskById(subtask.getId()), "Подзадачи должны удалиться");
    }

   
    @Test
    void removeSubtaskShouldDeleteSubtaskAndUpdateEpic() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Desc", Status.NEW, null, null));
        Subtask subtask = taskManager.createSubtask(
                new Subtask("Subtask", "Desc", Status.NEW, epic.getId(),
                        testTime, Duration.ofMinutes(15)));

        taskManager.removeSubtask(subtask.getId());
        assertNull(taskManager.getSubtaskById(subtask.getId()), "Подзадача должна удалиться");
        assertTrue(taskManager.getSubtasksByEpicId(epic.getId()).isEmpty(),
                "Эпик должен обновиться после удаления подзадачи");
    }

   
    @Test
    void getAllTasksShouldReturnAllCreatedTasks() {
        Task task1 = taskManager.createTask(new Task("Task 1", "Desc", Status.NEW,
                testTime, Duration.ofMinutes(30)));
        Task task2 = taskManager.createTask(new Task("Task 2", "Desc", Status.NEW,
                testTime.plusHours(1), Duration.ofMinutes(30)));

        List<Task> tasks = taskManager.getAllTasks();
        assertEquals(2, tasks.size(), "Должны возвращаться все задачи");
        assertTrue(tasks.contains(task1) && tasks.contains(task2),
                "Список должен содержать созданные задачи");
    }

   
    @Test
    void getAllEpicsShouldReturnAllCreatedEpics() {
        Epic epic1 = taskManager.createEpic(new Epic("Epic 1", "Desc", Status.NEW, null, null));
        Epic epic2 = taskManager.createEpic(new Epic("Epic 2", "Desc", Status.NEW, null, null));

        List<Epic> epics = taskManager.getAllEpics();
        assertEquals(2, epics.size(), "Должны возвращаться все эпики");
        assertTrue(epics.contains(epic1) && epics.contains(epic2),
                "Список должен содержать созданные эпики");
    }

   
    @Test
    void getAllSubtasksShouldReturnAllCreatedSubtasks() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Desc", Status.NEW, null, null));
        Subtask sub1 = taskManager.createSubtask(
                new Subtask("Sub 1", "Desc", Status.NEW, epic.getId(),
                        testTime, Duration.ofMinutes(15)));
        Subtask sub2 = taskManager.createSubtask(
                new Subtask("Sub 2", "Desc", Status.NEW, epic.getId(),
                        testTime.plusHours(1), Duration.ofMinutes(15)));

        List<Subtask> subtasks = taskManager.getAllSubtasks();
        assertEquals(2, subtasks.size(), "Должны возвращаться все подзадачи");
        assertTrue(subtasks.contains(sub1) && subtasks.contains(sub2),
                "Список должен содержать созданные подзадачи");
    }

   
    @Test
    void getSubtasksByEpicIdShouldReturnOnlyEpicSubtasks() {
        Epic epic1 = taskManager.createEpic(new Epic("Epic 1", "Desc", Status.NEW, null, null));
        Epic epic2 = taskManager.createEpic(new Epic("Epic 2", "Desc", Status.NEW, null, null));

        Subtask sub1 = taskManager.createSubtask(
                new Subtask("Sub 1", "Desc", Status.NEW, epic1.getId(),
                        testTime, Duration.ofMinutes(15)));
        Subtask sub2 = taskManager.createSubtask(
                new Subtask("Sub 2", "Desc", Status.NEW, epic2.getId(),
                        testTime.plusHours(1), Duration.ofMinutes(15)));

        List<Subtask> epic1Subtasks = taskManager.getSubtasksByEpicId(epic1.getId());
        assertEquals(1, epic1Subtasks.size(), "Должны возвращаться только подзадачи эпика 1");
        assertTrue(epic1Subtasks.contains(sub1) && !epic1Subtasks.contains(sub2),
                "Список должен содержать только подзадачи указанного эпика");
    }

   
    @Test
    void getHistoryShouldReturnViewedTasksInOrder() {
       
        Task task = taskManager.createTask(new Task("Task", "Desc", Status.NEW,
                testTime, Duration.ofMinutes(30)));
        Epic epic = taskManager.createEpic(new Epic("Epic", "Desc", Status.NEW, null, null));

       
        taskManager.getTaskById(task.getId());
        taskManager.getEpicById(epic.getId());

       
        List<Task> history = taskManager.getHistory();

       
        assertEquals(2, history.size(), "История должна содержать 2 просмотренные задачи");
        assertEquals(task.getId(), history.get(0).getId(),
                "Первой должна быть задача, которую просмотрели первой");
        assertEquals(epic.getId(), history.get(1).getId(),
                "Второй должен быть эпик, который просмотрели вторым");
    }
}