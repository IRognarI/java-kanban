import java.util.ArrayList;

public class Epic extends Task {
    private final ArrayList<Integer> subtaskId;

    public Epic(String title, String description, Status status) {
        super(title, description, Status.NEW);
        this.subtaskId = new ArrayList<>();
    }

    public void addSubtask(int subtaskId) {
        this.subtaskId.add(subtaskId);
    }

    public ArrayList<Integer> getSubtaskId() {
        return subtaskId;
    }
}