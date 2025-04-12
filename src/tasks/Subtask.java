package tasks;

import status.Status;
import status.TaskType;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Класс наследующий Task, а так же является маленькой задачей класса Epic
 */
public class Subtask extends Task {
    private int epicId;
    private final TaskType type = TaskType.SUBTASK;

    public Subtask(String title, String description, Status status, int epicId,
                   LocalDateTime startTime, Duration duration) {
        super(title, description, status, startTime, duration);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }

    @Override
    public String toString() {
        return String.format("%d,%s,%s,%s,%s,%d", getId(), type, getTitle(), getStatus(), getDescription(), epicId);
    }
}