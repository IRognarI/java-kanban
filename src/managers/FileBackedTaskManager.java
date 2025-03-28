package managers;

import enums.Status;
import enums.TaskType;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    @Override
    public List<Subtask> getSubtasksByEpicId(int epicId) {
        return super.getSubtasksByEpicId(epicId);
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return super.getAllSubtasks();
    }

    @Override
    public List<Epic> getAllEpics() {
        return super.getAllEpics();
    }

    @Override
    public List<Task> getAllTasks() {
        return super.getAllTasks();
    }

    @Override
    public List<Task> getHistory() {
        return super.getHistory();
    }

    @Override
    public void removeSubtask(int id) {
        super.removeSubtask(id);
        save();
    }

    @Override
    public void removeEpic(int id) {
        super.removeEpic(id);
        save();
    }

    @Override
    public void removeTask(int id) {
        super.removeTask(id);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public Subtask getSubtaskById(int id) {
        return super.getSubtaskById(id);
    }

    @Override
    public Epic getEpicById(int id) {
        return super.getEpicById(id);
    }

    @Override
    public Task getTaskById(int id) {
        return super.getTaskById(id);
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        Subtask newSubTask = super.createSubtask(subtask);
        save();
        return newSubTask;
    }

    @Override
    public Epic createEpic(Epic epic) {
        Epic newEpic = super.createEpic(epic);
        save();
        return newEpic;
    }

    @Override
    public Task createTask(Task task) {
        Task newTask = super.createTask(task);
        save();
        return newTask;
    }

    static class ManagerSaveException extends RuntimeException {

        public ManagerSaveException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private void save() {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file),
                StandardCharsets.UTF_8))) {
            bw.write("id,type,name,status,description,epic\n");

            for (Task task : getAllTasks()) {
                bw.write(task.toString() + "\n");
            }

            for (Epic epic : getAllEpics()) {
                bw.write(epic.toString() + "\n");
            }

            for (Subtask subtask : getAllSubtasks()) {
                bw.write(subtask.toString() + "\n");
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения файла", e.getCause());
        }
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        List<Integer> historyIds = new ArrayList<>();
        boolean readingHistory = false;

        try {
            List<String> lines = Files.readAllLines(file.toPath());

            for (String line : lines.subList(1, lines.size())) {
                line = line.trim();

                if (line.isEmpty()) {
                    readingHistory = true;
                    continue;
                }

                if (readingHistory) {
                    for (String id : line.split(",")) {
                        try {
                            historyIds.add(Integer.parseInt(id.trim()));
                        } catch (NumberFormatException ignored) {
                        }
                    }
                    break;
                }

                Task task = fromString(line);
                if (task != null) {
                    switch (task.getType()) {
                        case TASK:
                            manager.createTask(task);
                            break;
                        case EPIC:
                            manager.createEpic((Epic) task);
                            break;
                        case SUBTASK:
                            manager.createSubtask((Subtask) task);
                            break;
                    }
                }
            }

            for (int id : historyIds) {
                Task task = manager.getTaskById(id);
                if (task == null) task = manager.getEpicById(id);
                if (task == null) task = manager.getSubtaskById(id);

                if (task != null) {
                    manager.getTaskById(id);
                }
            }

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка загрузки из файла", e);
        }

        return manager;
    }

    private static Task fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            String[] parts = value.split(",");
            if (parts.length < 5) return null;

            int id = Integer.parseInt(parts[0].trim());
            TaskType type = TaskType.valueOf(parts[1].trim());
            String title = parts[2].trim();
            Status status = Status.valueOf(parts[3].trim());
            String description = parts[4].trim();

            switch (type) {
                case TASK:
                    return new Task(id, title, description, status);
                case EPIC:
                    return new Epic(id, title, description, status);
                case SUBTASK:
                    int epicId = parts.length > 5 ? Integer.parseInt(parts[5].trim()) : 0;
                    return new Subtask(id, title, description, status, epicId);
                default:
                    return null;
            }
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
