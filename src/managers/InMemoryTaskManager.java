package managers;

import com.sun.jdi.InvalidTypeException;
import exception.LocalDateTimeException;
import status.Status;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;

import java.util.*;
import java.util.stream.*;

/**
 * Данный клас реализует интерфейс TaskManager
 */
public class InMemoryTaskManager implements TaskManager {
    protected Map<Integer, Task> tasks = new HashMap<>();
    protected Map<Integer, Epic> epics = new HashMap<>();
    protected Map<Integer, Subtask> subtasks = new HashMap<>();
    private final HistoryManager historyManager = Managers.getDefaultHistory();
    private int idCounter = 1;


    protected int getIdCounter() {
        return idCounter;
    }

    @Override
    public Task createTask(Task task) {
        task.setId(idCounter++);
        tasks.put(task.getId(), task);
        return task;
    }

    @Override
    public Epic createEpic(Epic epic) {
        epic.setId(idCounter++);
        epics.put(epic.getId(), epic);
        return epic;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        int epicId = subtask.getEpicId();
        if (!epics.containsKey(epicId)) {
            throw new IllegalArgumentException("tasks.Epic with the given ID does not exist.");
        }
        subtask.setId(idCounter++);
        subtasks.put(subtask.getId(), subtask);
        epics.get(epicId).addSubtask(subtask.getId());
        updateEpicStatus(epicId);
        return subtask;
    }

    @Override
    public Task getTaskById(int id) {
        return tasks.get(id);
    }

    @Override
    public Epic getEpicById(int id) {
        return epics.get(id);
    }

    @Override
    public Subtask getSubtaskById(int id) {
        return subtasks.get(id);
    }

    @Override
    public void updateTask(Task task) {
        tasks.put(task.getId(), task);
    }

    @Override
    public void updateEpic(Epic epic) {
        epics.put(epic.getId(), epic);
        updateEpicStatus(epic.getId());
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        subtasks.put(subtask.getId(), subtask);
        updateEpicStatus(subtask.getEpicId());
    }

    @Override
    public void removeTask(int id) {
        tasks.remove(id);
        historyManager.remove(id);
    }

    @Override
    public void removeEpic(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            for (int subtaskId : epic.getSubtaskId()) {
                subtasks.remove(subtaskId);
                historyManager.remove(subtaskId);
            }
        }
        historyManager.remove(id);
    }

    @Override
    public void removeSubtask(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.getSubtaskId().remove((Integer) id);
                updateEpicStatus(subtask.getEpicId());
            }
        }
        historyManager.remove(id);
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public List<Subtask> getSubtasksByEpicId(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return new ArrayList<>();
        }

        List<Subtask> epicSubtasks = new ArrayList<>();
        for (int subtaskId : epic.getSubtaskId()) {
            epicSubtasks.add(subtasks.get(subtaskId));
        }
        return epicSubtasks;
    }

    private void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return;
        }

        List<Subtask> subtasksList = getSubtasksByEpicId(epicId);

        if (subtasksList.isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }

        boolean allNew = true;
        boolean allDone = true;

        for (Subtask subtask : subtasksList) {
            Status status = subtask.getStatus();
            if (status != Status.NEW) {
                allNew = false;
            }
            if (status != Status.DONE) {
                allDone = false;
            }
        }

        if (allNew) {
            epic.setStatus(Status.NEW);
        } else if (allDone) {
            epic.setStatus(Status.DONE);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }

    @Override
    public Set<? extends Task> getPrioritizedTasks(List<? extends Task> tasksList) {

        Set<? extends Task> someTasks = new TreeSet<>();

        try {
            if (tasksList == null) {
                throw new NullPointerException("Список не может содержать null объекты!");
            }
            if (!tasksList.isEmpty()) {

                if (tasksList instanceof Task) {
                    someTasks = new TreeSet<>(getAllTasks());
                    return someTasks;
                }

                if (tasksList instanceof Epic) {
                    someTasks = new TreeSet<>(getAllEpics());
                    return someTasks;
                }

                if (tasksList instanceof Subtask) {
                    someTasks = new TreeSet<>(getAllSubtasks());
                    return someTasks;
                }
            }
        } catch (NullPointerException e) {
            System.out.println(e.getMessage());
        }
        return someTasks;
    }

    @Override
    public boolean lookingForTemporaryIntersectionsInTasks() {
        Set<? extends Task> listOfSomeKindOfClass = getPrioritizedTasks(getAllTasks());

        return listOfSomeKindOfClass.stream()
                .anyMatch(task1 ->
                        listOfSomeKindOfClass.stream()
                                .filter(task2 -> !task1.equals(task2))
                                .anyMatch(task2 ->
                                        task1.getStartTime().isBefore(task2.getEndTime()) &&
                                        task2.getStartTime().isBefore(task1.getEndTime())));
    }

    @Override
    public boolean lookingForTemporaryIntersectionsInEpics() {
        Set<? extends Task> listOfSomeKindOfClass = getPrioritizedTasks(getAllEpics());

        return listOfSomeKindOfClass.stream()
                .anyMatch(epic1 ->
                        listOfSomeKindOfClass.stream()
                                .filter(epic2 -> !epic1.equals(epic2))
                                .anyMatch(epic2 ->
                                        epic1.getStartTime().isBefore(epic2.getEndTime()) &&
                                                epic2.getStartTime().isBefore(epic1.getEndTime())));
    }

    @Override
    public boolean lookingForTemporaryIntersectionsInSubTasks() {
        Set<? extends Task> listOfSomeKindOfClass = getPrioritizedTasks(getAllSubtasks());

        return listOfSomeKindOfClass.stream()
                .anyMatch(subTask1 ->
                        listOfSomeKindOfClass.stream()
                                .filter(subTask2 -> !subTask1.equals(subTask2))
                                .anyMatch(subTask2 ->
                                        subTask1.getStartTime().isBefore(subTask2.getEndTime()) &&
                                                subTask2.getStartTime().isBefore(subTask1.getEndTime())));
    }
}