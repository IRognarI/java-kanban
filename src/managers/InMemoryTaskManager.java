package managers;

import annotations.ClassInformation;
import annotations.MethodInformation;
import enums.Status;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;

import java.util.*;
import java.util.stream.*;

@ClassInformation("Реализует методы интерфейса TaskManager")
public class InMemoryTaskManager implements TaskManager {
    protected Map<Integer, Task> tasks = new HashMap<>();
    protected Map<Integer, Epic> epics = new HashMap<>();
    protected Map<Integer, Subtask> subtasks = new HashMap<>();
    protected HistoryManager historyManager = Managers.getDefaultHistory();
    protected int idCounter = 1;

    @Override
    @MethodInformation("Создание Task")
    public Task createTask(Task task) {
        task.setId(idCounter++);
        tasks.put(task.getId(), task);
        return task;
    }

    @Override
    @MethodInformation("Создание Epic")
    public Epic createEpic(Epic epic) {
        epic.setId(idCounter++);
        epics.put(epic.getId(), epic);
        return epic;
    }

    @Override
    @MethodInformation("Создание SubTask")
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
    @MethodInformation("Получение Task по id")
    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    @Override
    @MethodInformation("Получение Epic по id")
    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);
        }
        return epic;
    }

    @Override
    @MethodInformation("Получение SubTask по id")
    public Subtask getSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.add(subtask);
        }
        return subtask;
    }

    @Override
    @MethodInformation("Обновление Task")
    public void updateTask(Task task) {
        tasks.put(task.getId(), task);
    }

    @Override
    @MethodInformation("Обновление Epic")
    public void updateEpic(Epic epic) {
        epics.put(epic.getId(), epic);
        updateEpicStatus(epic.getId());
    }

    @Override
    @MethodInformation("Обновление SubTask")
    public void updateSubtask(Subtask subtask) {
        subtasks.put(subtask.getId(), subtask);
        updateEpicStatus(subtask.getEpicId());
    }

    @Override
    @MethodInformation("Удаление Task по id")
    public void removeTask(int id) {
        tasks.remove(id);
        historyManager.remove(id);
    }

    @Override
    @MethodInformation("Удаление Epic по id")
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
    @MethodInformation("Удаление SubTask по id")
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
    @MethodInformation("Удаление всех Task")
    public void deleteTasks() {
        tasks.clear();
    }

    @Override
    @MethodInformation("Удаление всех SubTask")
    public void deleteSubtasks() {
        subtasks.clear();

        for (Epic epic : epics.values()) {
            epic.getSubtaskId().clear();
            updateEpicStatus(epic.getId());
        }
    }

    @Override
    @MethodInformation("Удаление всех Epic")
    public void deleteEpics() {
        subtasks.clear();
        epics.clear();
    }

    @Override
    @MethodInformation("Получение истории просмотров Object<? extends Task>")
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    @MethodInformation("Получение листа Task")
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    @MethodInformation("Получение листа Epic")
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    @MethodInformation("Получение листа SubTask")
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    @MethodInformation("Получение SubTask по id Epic")
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

    @MethodInformation("Вспомогательный метод по обновлению статуса Epic")
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
    @MethodInformation("Вычисление приоритетных Задач")
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
    @MethodInformation("Проверка задач (Task) на пересечение")
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

    @MethodInformation("Вспомогательный метод для метода проверки задач на пересечение. Проверяет, что" +
            " пересечение нет.")
    private boolean isOverlapping(Task a, Task b) {
        return a.getStartTime().isBefore(b.getEndTime()) &&
                b.getStartTime().isBefore(a.getEndTime());
    }

    @MethodInformation("Проверка задач (Epic) на пересечение")
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

    @MethodInformation("Проверка задач (SubTask) на пересечение")
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