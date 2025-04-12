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
    private final TaskManager inMemoryTaskManager = Managers.getDefault();
    List<Subtask> subtaskList = inMemoryTaskManager.getAllSubtasks();

    public Epic(String title, String description, Status status,
                LocalDateTime startTime, Duration duration) {
        super(title, description, status, startTime, duration);
        this.subtaskId = new ArrayList<>();
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
        if (subtaskId.isEmpty()) {
            return null;
        }
        LocalDateTime earliest = subtaskList.get(subtaskId.get(0)).getStartTime();
        for (int id : subtaskId) {
            Subtask subtask = subtaskList.get(id);
            if (subtask != null && subtask.getStartTime() != null &&
                    (earliest == null || subtask.getStartTime().isBefore(earliest))) {
                earliest = subtask.getStartTime();
            }
        }
        return earliest;
    }

    @Override
    public LocalDateTime getEndTime() {
        if (subtaskId.isEmpty()) {
            return null;
        }
        LocalDateTime latest = subtaskList.get(subtaskId.get(0)).getEndTime();
        for (int id : subtaskId) {
            Subtask subtask = subtaskList.get(id);
            if (subtask != null && subtask.getEndTime() != null &&
                    (latest == null || subtask.getEndTime().isAfter(latest))) {
                latest = subtask.getEndTime();
            }
        }
        return latest;
    }

    @Override
    public Long getDuration() {
        if (subtaskId.isEmpty()) {
            return 0L;
        }
        long total = 0;
        for (int id : subtaskId) {
            Subtask subtask = subtaskList.get(id);
            if (subtask != null && subtask.getDuration() != null) {
                total += subtask.getDuration();
            }
        }
        return total;
    }
}