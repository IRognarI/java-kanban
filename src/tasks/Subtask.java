package tasks;

import enums.Status;
import enums.TaskType;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Класс наследующий Task, а так же является маленькой задачей класса Epic
 */
public class Subtask extends Task {
    private final int epicId;
   

    public Subtask(String title, String description, Status status, int epicId,
                   LocalDateTime startTime, Duration duration) {
        super(title, description, status, startTime, duration);
        this.epicId = epicId;
    }

    @Override
    public TaskType getType() {
        return TaskType.SUBTASK;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        return String.format("%d,%s,%s,%s,%s,%d", getId(), getType(), getTitle(), getStatus(), getDescription(), epicId);
    }
}