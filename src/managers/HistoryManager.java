package managers;

import tasks.Task;

import java.util.List;


/**
 * Данный интерфейс создан для управления историей просмотров
 */
public interface HistoryManager {
    void add(Task task);

    void remove(int id); // Добавили новый метод. Нужно реализовать в InMemoryHistoryManager

    List<Task> getHistory();
}
