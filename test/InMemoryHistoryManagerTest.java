import managers.InMemoryHistoryManager;
import org.junit.jupiter.api.*;
import tasks.Task;
import status.Status;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    private InMemoryHistoryManager historyManager;
    private Task task1;
    private Task task2;
    private Task task3;

    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager();

        LocalDateTime now = LocalDateTime.now();
        task1 = new Task("Task 1", "Description 1", Status.NEW,
                now, Duration.ofMinutes(30));
        task2 = new Task("Task 2", "Description 2", Status.IN_PROGRESS,
                now.plusHours(1), Duration.ofHours(1));
        task3 = new Task("Task 3", "Description 3", Status.DONE,
                now.plusHours(3), Duration.ofHours(1));

       
        task1.setId(1);
        task2.setId(2);
        task3.setId(3);
    }

    @Test
    @DisplayName("Добавление задачи в историю")
    void shouldAddTaskToHistory() {
        historyManager.add(task1);
        List<Task> history = historyManager.getHistory();

        assertEquals(1, history.size());
        assertEquals(task1, history.get(0));
    }

    @Test
    @DisplayName("Добавление null задачи")
    void shouldNotAddNullTask() {
        historyManager.add(null);
        List<Task> history = historyManager.getHistory();

        assertTrue(history.isEmpty());
    }

    @Test
    @DisplayName("Дублирование задачи в истории")
    void shouldMoveTaskToEndWhenAddingDuplicate() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task1);

        List<Task> history = historyManager.getHistory();

        assertEquals(2, history.size());
        assertEquals(task2, history.get(0));
        assertEquals(task1, history.get(1));
    }

    @Test
    @DisplayName("Удаление задачи из истории")
    void shouldRemoveTaskFromHistory() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.remove(task1.getId());

        List<Task> history = historyManager.getHistory();

        assertEquals(1, history.size());
        assertEquals(task2, history.get(0));
    }

    @Test
    @DisplayName("Удаление несуществующей задачи")
    void shouldDoNothingWhenRemovingNonExistentTask() {
        historyManager.add(task1);
        historyManager.remove(999);

        List<Task> history = historyManager.getHistory();

        assertEquals(1, history.size());
    }

    @Test
    @DisplayName("Получение истории после удаления всех задач")
    void shouldReturnEmptyHistoryAfterRemovingAllTasks() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.remove(task1.getId());
        historyManager.remove(task2.getId());

        List<Task> history = historyManager.getHistory();

        assertTrue(history.isEmpty());
    }

    @Test
    @DisplayName("Порядок задач в истории")
    void shouldMaintainInsertionOrder() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        List<Task> history = historyManager.getHistory();

        assertEquals(3, history.size());
        assertEquals(task1, history.get(0));
        assertEquals(task2, history.get(1));
        assertEquals(task3, history.get(2));
    }

    @Test
    @DisplayName("Ограничение на количество задач в истории")
    void shouldHandleLargeNumberOfTasks() {
       
        for (int i = 1; i <= 15; i++) {
            Task task = new Task("Task " + i, "Description " + i, Status.NEW,
                    LocalDateTime.now(), Duration.ofHours(1));
            task.setId(i);
            historyManager.add(task);
        }

        List<Task> history = historyManager.getHistory();

        assertEquals(15, history.size());
       
        assertEquals(15, history.get(history.size() - 1).getId());
    }
}
