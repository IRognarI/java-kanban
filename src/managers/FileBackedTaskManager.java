package managers;

import status.Status;
import status.TaskType;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    public static class ManagerSaveException extends RuntimeException {
        public ManagerSaveException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        try {
            List<String> lines = Files.readAllLines(file.toPath());

            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.isEmpty()) break;

                Task task = fromString(line);
                if (task != null) {
                    switch (task.getType()) {
                        case TASK:
                            manager.tasks.put(task.getId(), task);
                            break;
                        case EPIC:
                            manager.epics.put(task.getId(), (Epic) task);
                            break;
                        case SUBTASK:
                            manager.subtasks.put(task.getId(), (Subtask) task);
                            break;
                    }
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка загрузки файла", e);
        }
        return manager;
    }

    public void save() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("id,type,name,status,description,epic\n");
            for (Task task : getAllTasks()) {
                sb.append(task.toString()).append("\n");
            }
            for (Epic epic : getAllEpics()) {
                sb.append(epic.toString()).append("\n");
            }
            for (Subtask subtask : getAllSubtasks()) {
                sb.append(subtask.toString()).append("\n");
            }
            Files.writeString(file.toPath(), sb.toString());
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка записи файла", e.getCause());
        }
    }

    private static Task fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            String[] parts = value.split(",", -1);
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
                    Epic epic = new Epic(id, title, description, status);

                    if (parts.length > 5 && !parts[5].trim().isEmpty()) {
                        String[] subtaskIds = parts[5].trim().split(";");
                        for (String sid : subtaskIds) {
                            try {
                                epic.addSubtask(Integer.parseInt(sid));
                            } catch (NumberFormatException e) {
                                System.out.println(e.getMessage());
                            }
                        }
                    }
                    return epic;
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

    @Override
    public Task createTask(Task task) {
        Task newTask = super.createTask(task);
        save();
        return newTask;
    }

    @Override
    public Epic createEpic(Epic epic) {
        Epic newEpic = super.createEpic(epic);
        save();
        return newEpic;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        Subtask newSubtask = super.createSubtask(subtask);
        save();
        return newSubtask;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void removeTask(int id) {
        super.removeTask(id);
        save();
    }

    @Override
    public void removeEpic(int id) {
        super.removeEpic(id);
        save();
    }

    @Override
    public void removeSubtask(int id) {
        super.removeSubtask(id);
        save();
    }
}