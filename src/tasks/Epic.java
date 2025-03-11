package tasks;

import status.Status;

import java.util.ArrayList;
import java.util.List;

/**
 * Класс наследующий Task и являющийся подзадачей главной задачи Task
 */
public class Epic extends Task {
    private int id;
    private final List<Integer> subtaskId;

    public Epic(String title, String description, Status status) {
        super(title, description, status);
        this.subtaskId = new ArrayList<>();
    }

    public Epic(String title, String description) {
        super(title, description);
        this.subtaskId = new ArrayList<>();
    }

    public Epic(int id, String title, String description, Status status) {
        super(title, description, status);
        this.id = id;
        this.subtaskId = new ArrayList<>();
    }

    public void addSubtask(int subtaskId) {
        this.subtaskId.add(subtaskId);
    }

    public List<Integer> getSubtaskId() {
        return subtaskId;
    }
}