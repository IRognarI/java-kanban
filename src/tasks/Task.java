package tasks;

import status.Status;
import status.TaskType;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Класс для создания основной задачи
 */
public class Task implements Comparable<Task> {
    private int id;
    private String title;
    private String description;
    private Status status;
    private final TaskType type = TaskType.TASK;
    private final LocalDateTime startTime;
    private final Duration duration;

    public Task(String title, String description, Status status,
                LocalDateTime startTime, Duration duration) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.startTime = startTime;
        this.duration = duration;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return String.format("%d,%s,%s,%s,%s,", id, type, title, status, description);
    }

    public TaskType getType() {
        return type;
    }

    public Long getDuration() {
        return duration.toMinutes();
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        if (startTime == null || duration == null) {
            return null;
        }
        return startTime.plus(duration);
    }

    @Override
    public int compareTo(Task o) {
        return this.getStartTime().compareTo(o.getStartTime());
    }
}