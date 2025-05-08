package managers;

import annotations.ClassInformation;
import tasks.Task;

import java.util.*;

@ClassInformation("Интерфейс управляющий историей просмотров")
public interface HistoryManager {
    void add(Task task);

    void remove(int id);

    List<Task> getHistory();
}