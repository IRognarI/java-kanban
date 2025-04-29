package managers;

import enums.Status;
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
    protected HistoryManager historyManager = Managers.getDefaultHistory();
    protected int idCounter = 1;

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
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);
        }
        return epic;
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.add(subtask);
        }
        return subtask;
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
    public void deleteTasks() {
        tasks.clear();
    }

    @Override
    public void deleteSubtasks() {
        subtasks.clear();
       
        for (Epic epic : epics.values()) {
            epic.getSubtaskId().clear();
            updateEpicStatus(epic.getId());
        }
    }

    @Override
    public void deleteEpics() {
        subtasks.clear();
        epics.clear();
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
    public Set<Task> getPrioritizedTasks(List<? extends Task> tasksList) {
        if (tasksList == null || tasksList.isEmpty()) {
            return new TreeSet<>(Comparator.comparing(Task::getStartTime));
        }

        TreeSet<Task> prioritizedTasks = new TreeSet<>(Comparator.comparing(Task::getStartTime));
        tasksList.stream()
                .filter(task -> task.getStartTime() != null)
                .forEach(prioritizedTasks::add);
        return prioritizedTasks;
    }

    @Override
    public boolean lookingForTemporaryIntersectionsInTasks() {
        List<Task> tasksWithTime = getAllTasks().stream()
                .filter(t -> t.getStartTime() != null && t.getEndTime() != null)
                .toList();

        return IntStream.range(0, tasksWithTime.size())
                .anyMatch(i -> IntStream.range(i + 1, tasksWithTime.size())
                        .anyMatch(j -> isOverlapping(
                                tasksWithTime.get(i),
                                tasksWithTime.get(j)
                        )));
    }

    private boolean isOverlapping(Task a, Task b) {
        return a.getStartTime().isBefore(b.getEndTime()) &&
                b.getStartTime().isBefore(a.getEndTime());
    }

    private boolean lookingForTemporaryIntersectionsInEpics() {
        Set<? extends Task> listOfSomeKindOfClass = getPrioritizedTasks(getAllEpics());

        return listOfSomeKindOfClass.stream()
                .anyMatch(epic1 ->
                        listOfSomeKindOfClass.stream()
                                .filter(epic2 -> !epic1.equals(epic2))
                                .anyMatch(epic2 ->
                                        epic1.getStartTime().isBefore(epic2.getEndTime()) &&
                                                epic2.getStartTime().isBefore(epic1.getEndTime())));
    }

    private boolean lookingForTemporaryIntersectionsInSubTasks() {
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