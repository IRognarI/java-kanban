package tasks;

import enums.Status;
import enums.TaskType;

/**
 * Класс для создания основной задачи
 */
public class Task {
    private int id;
    private String title;
    private String description;
    private Status status;
    private TaskType type = TaskType.TASK;

    public Task(String title, String description, Status status) {
        this.title = title;
        this.description = description;
        this.status = status;
    }

    public Task(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public Task(int id, String title, String description, Status status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
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

    @Override
    public String toString() {
        return String.format("%d,%s,%s,%s,%s,", id, type, title, status, description);
    }

    public TaskType getType() {
        return type;
    }
}