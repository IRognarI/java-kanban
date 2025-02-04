import java.util.ArrayList;

public class Epic extends Task {
    private final ArrayList<Integer> subtaskId;

    public Epic(int id, String title, String description) {
        super(id, title, description, Status.NEW);
        this.subtaskId = new ArrayList<>();
    }

    public void addSubtask(int subtaskId) {
        this.subtaskId.add(subtaskId);
    }

    public ArrayList<Integer> getSubtaskId() {
        return subtaskId;
    }
}