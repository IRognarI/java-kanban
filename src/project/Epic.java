package project;

import java.util.ArrayList;

public class Epic extends Task {
    private int id;
    private final ArrayList<Integer> subtaskId;

    public Epic(String title, String description, Status status) {
        super(title, description, Status.NEW);
        this.subtaskId = new ArrayList<>();
    }

    public Epic(String title, String description) {
        super(title, description);
        this.subtaskId = new ArrayList<>();
    }

    public Epic(int id ,String title, String description, Status status) {
        super(title, description, Status.NEW);
        this.id = id;
        this.subtaskId = new ArrayList<>();
    }

    public void addSubtask(int subtaskId) {
        this.subtaskId.add(subtaskId);
    }

    public ArrayList<Integer> getSubtaskId() {
        return subtaskId;
    }
}