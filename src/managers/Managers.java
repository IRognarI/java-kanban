package managers;

/**
 * Утилитарный класс. Далее на нём будет лежать вся ответственность за создание менеджера задач
 */
public class Managers {
    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}