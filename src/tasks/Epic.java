package tasks;

import enums.Status;
import enums.TaskType;

import java.util.ArrayList;
import java.util.List;

/**
 * Класс наследующий Task и являющийся подзадачей главной задачи Task
 */
public class Epic extends Task {
    private int id;
    private final List<Integer> subtaskId;
    private TaskType type = TaskType.EPIC;

    public Epic(String title, String description, Status status) {
        super(title, description, status);
        this.subtaskId = new ArrayList<>();
    }

    public Epic(String title, String description) {
        super(title, description);
        this.subtaskId = new ArrayList<>();
    }

    public Epic(int id, String title, String description, Status status) {
        super(id, title, description, status);
        this.id = id;
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
}