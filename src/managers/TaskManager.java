package managers;

import tasks.Epic;
import tasks.Subtask;
import tasks.Task;

import java.util.*;

/**
 * Данный интерфейс создан для реализации логики приложения. В нем декларируются основные методы.
 */
public interface TaskManager {
    Task createTask(Task task);

    Epic createEpic(Epic epic);

    Subtask createSubtask(Subtask subtask);

    Task getTaskById(int id);

    Epic getEpicById(int id);

    Subtask getSubtaskById(int id);

    void updateTask(Task task);

    void updateEpic(Epic epic);

    void updateSubtask(Subtask subtask);

    void removeTask(int id);

    void removeEpic(int id);

    void removeSubtask(int id);

    List<Task> getAllTasks();

    List<Epic> getAllEpics();

    List<Subtask> getAllSubtasks();

    List<Subtask> getSubtasksByEpicId(int epicId);

    List<Task> getHistory();

    Set<? extends Task> getPrioritizedTasks(List<? extends Task> list);

    boolean lookingForTemporaryIntersectionsInTasks();

    boolean lookingForTemporaryIntersectionsInEpics();

    boolean lookingForTemporaryIntersectionsInSubTasks();
}
