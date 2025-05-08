package managers;

import annotations.ClassInformation;
import annotations.MethodInformation;

@ClassInformation("Утилитарный класс")
public class Managers {

    @MethodInformation("Создает объект класса InMemoryTaskManager")
    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    @MethodInformation("Создает объект класса InMemoryHistoryManager")
    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}