package tasks;

import managers.Managers;
import managers.TaskManager;
import status.Status;
import status.TaskType;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Класс наследующий Task и являющийся подзадачей главной задачи Task
 */
public class Epic extends Task {
    private final List<Integer> subtaskId;
    private final TaskType type = TaskType.EPIC;
    private TaskManager inMemoryTaskManager;
    List<Subtask> subtaskList = inMemoryTaskManager.getAllSubtasks();

    public Epic(String title, String description, Status status, LocalDateTime startTime, LocalDateTime endTime,
                Duration duration) {
        super(title, description, status, startTime, endTime, duration);
        this.subtaskId = new ArrayList<>();
        inMemoryTaskManager = Managers.getDefault();
    }

    public void addSubtask(int subtaskId) {
        this.subtaskId.add(subtaskId);
    }

    public List<Integer> getSubtaskId() {
        return subtaskId;
    }

    @Override
    public String toString() {
        return String.format("%d,%s,%s,%s,%s,", getId(), type, getTitle(), getStatus(), getDescription());
    }

    @Override
    public LocalDateTime getStartTime() {
        for (int i = 0; i < subtaskList.size() - 1; i++) {
            for (int j = i + 1; j < subtaskList.size() - 1; j++) {
                if (subtaskList.get(i).getStartTime().isBefore(subtaskList.get(j).getStartTime())) {
                    LocalDateTime actualStartTime = subtaskList.get(i).getStartTime();
                    setStartTime(actualStartTime);
                    return getStartTime();
                }
            }
        }
        return getStartTime();
    }

    @Override
    public LocalDateTime getEndTime() {
        for (int i = 0; i < subtaskList.size() - 1; i++) {
            for (int j = i + 1; j < subtaskList.size() - 1; j++) {
                if (subtaskList.get(i).getEndTime().isAfter(subtaskList.get(j).getEndTime())) {
                    LocalDateTime actualEndTime = subtaskList.get(i).getEndTime();
                    setEndTime(actualEndTime);
                    return getEndTime();
                }
            }
        }
        return getEndTime();
    }

    @Override
    public Long getDuration() {
        Duration actualDuration = Duration.ofMinutes(getDuration());

        try {
            if (subtaskList == null) {
                throw new NullPointerException("Коллекция Subtasks пустая!");
            }
            for (Subtask subtask : subtaskList) {
                Duration durationToMinutes = Duration.ofMinutes(subtask.getDuration());
                actualDuration = actualDuration.plus(durationToMinutes);
                setDuration(actualDuration);
            }
        } catch (NullPointerException e) {
            System.out.println(e.getMessage());
        }
        return getDuration();
    }
}