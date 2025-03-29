package tasks;

import status.Status;
import status.TaskType;

/**
 * Класс наследующий Task, а так же является маленькой задачей класса Epic
 */
public class Subtask extends Task {
    private int epicId;
    private int updateID;
    private final TaskType type = TaskType.SUBTASK;

    public Subtask(String title, String description, Status status, int epicId) {
        super(title, description, status);
        this.epicId = epicId;
    }

    public Subtask(int updateID, String title, String description, Status status, int epicId) {
        super(title, description, status);
        this.updateID = updateID;
        this.epicId = epicId;
    }

    public Subtask(String title, String description, int updateID) {
        super(title, description);
        this.updateID = updateID;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        return String.format("%d,%s,%s,%s,%s,%d", getId(), type, getTitle(), getStatus(), getDescription(), epicId);
    }
}