package tasks;

import managers.Managers;
import managers.TaskManager;
import enums.Status;
import enums.TaskType;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Класс наследующий Task и являющийся подзадачей главной задачи Task
 */
public class Epic extends Task {
    private final List<Integer> subtaskId;
   
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
    public TaskType getType() {
        return TaskType.EPIC;
    }

    @Override
    public String toString() {
        return String.format("%d,%s,%s,%s,%s,", getId(), getType(), getTitle(), getStatus(), getDescription());
    }

    @Override
    public LocalDateTime getStartTime() {
        if (subtaskId.isEmpty()) {
            return null;
        }
        LocalDateTime earliest = null;
        for (int id : subtaskId) {
            Subtask subtask = inMemoryTaskManager.getSubtaskById(id);
            if (subtask != null && subtask.getStartTime() != null) {
                if (earliest == null || subtask.getStartTime().isBefore(earliest)) {
                    earliest = subtask.getStartTime();
                }
            }
        }
        return earliest;
    }

    @Override
    public LocalDateTime getEndTime() {
        if (subtaskId.isEmpty()) {
            return null;
        }
        LocalDateTime latest = null;
        for (int id : subtaskId) {
            Subtask subtask = inMemoryTaskManager.getSubtaskById(id);
            if (subtask != null && subtask.getEndTime() != null) {
                if (latest == null || subtask.getEndTime().isAfter(latest)) {
                    latest = subtask.getEndTime();
                }
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
            Subtask subtask = inMemoryTaskManager.getSubtaskById(id);
            if (subtask != null && subtask.getDuration() != null) {
                total += subtask.getDuration();
            }
        }
        return total;
    }
}