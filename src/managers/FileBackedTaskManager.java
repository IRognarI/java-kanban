package managers;

import annotations.ClassInformation;
import annotations.MethodInformation;
import annotations.MethodInformation;
import exception.ManagerSaveException;
import enums.Status;
import enums.TaskType;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@ClassInformation("Расширяет InMemoryTaskManager для добавления возможности сохранять состояние задач в файл")
public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    @MethodInformation("Восстанавливает данные из файла")
    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        try {
            String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            String[] parts = content.split("\n\n", 2);

            if (parts.length == 0) {
                return manager;
            }

            String[] lines = parts[0].split("\n");
            for (int i = 1; i < lines.length; i++) {
                Task task;
                try {
                    task = fromString(lines[i]);
                } catch (IllegalArgumentException e) {
                    continue;
                }

                if (task instanceof Epic epic) {
                    manager.epics.put(task.getId(), epic);
                } else if (task instanceof Subtask subtask) {
                    manager.subtasks.put(task.getId(), subtask);
                    Epic epic = manager.epics.get(subtask.getEpicId());
                    if (epic != null) {
                        epic.addSubtask(subtask.getId());
                    }
                } else {
                    manager.tasks.put(task.getId(), task);
                }
                manager.idCounter = Math.max(manager.idCounter, task.getId() + 1);
            }

            if (parts.length > 1) {
                List<Integer> history = historyFromString(parts[1]);
                for (int id : history) {
                    Task task = manager.tasks.get(id);
                    if (task != null) {
                        manager.historyManager.add(task);
                        continue;
                    }

                    Epic epic = manager.epics.get(id);
                    if (epic != null) {
                        manager.historyManager.add(epic);
                        continue;
                    }

                    Subtask subtask = manager.subtasks.get(id);
                    if (subtask != null) {
                        manager.historyManager.add(subtask);
                    }
                }
            }

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка загрузки из файла");
        }

        return manager;
    }

    @MethodInformation("Записывает данные в файл")
    private void save() {
        try {
            StringBuilder builder = new StringBuilder();
            builder.append("id,type,name,status,description,startTime,duration,epic\n");

            for (Task task : getAllTasks()) {
                builder.append(toString(task)).append("\n");
            }
            for (Epic epic : getAllEpics()) {
                builder.append(toString(epic)).append("\n");
            }
            for (Subtask subtask : getAllSubtasks()) {
                builder.append(toString(subtask)).append("\n");
            }

            builder.append("\n").append(historyToString(historyManager));

            Files.writeString(file.toPath(), builder.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка записи в файл");
        }
    }

    @MethodInformation("Десиарилизует объекты в строку")
    private String toString(Task task) {
        String[] fields = {String.valueOf(task.getId()), task.getType().name(), task.getTitle(),
                task.getStatus().name(), task.getDescription(),
                task.getStartTime() != null ? task.getStartTime().toString() : "",
                task.getDuration() != null ? String.valueOf(task.getDuration()) : "",
                task instanceof Subtask ? String.valueOf(((Subtask) task).getEpicId()) : ""};
        return String.join(",", fields);
    }

    @MethodInformation("Сериализует объекты из строки")
    private static Task fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        String[] fields = value.split(",", -1);
        if (fields.length < 7) {
            return null;
        }

        try {
            int id = Integer.parseInt(fields[0]);
            TaskType type = TaskType.valueOf(fields[1]);
            String title = fields[2];
            Status status = Status.valueOf(fields[3]);
            String description = fields[4];
            LocalDateTime startTime = fields[5].isEmpty() ? null : LocalDateTime.parse(fields[5]);
            Duration duration = fields[6].isEmpty() ? null : Duration.ofMinutes(Long.parseLong(fields[6]));

            switch (type) {
                case TASK:
                    Task task = new Task(title, description, status, startTime, duration);
                    task.setId(id);
                    return task;
                case EPIC:
                    Epic epic = new Epic(title, description, status, startTime, duration);
                    epic.setId(id);
                    return epic;
                case SUBTASK:
                    if (fields.length < 8 || fields[7].isEmpty()) return null;
                    int epicId = Integer.parseInt(fields[7]);
                    Subtask subtask = new Subtask(title, description, status, epicId, startTime, duration);
                    subtask.setId(id);
                    return subtask;
                default:
                    return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    @MethodInformation("Десиарилизует объекты в строку")
    private static String historyToString(HistoryManager manager) {
        List<String> ids = new ArrayList<>();
        for (Task task : manager.getHistory()) {
            ids.add(String.valueOf(task.getId()));
        }
        return String.join(",", ids);
    }

    @MethodInformation("Сериализует объекты из строки")
    private static List<Integer> historyFromString(String value) {
        List<Integer> history = new ArrayList<>();
        if (value == null || value.isBlank()) {
            return history;
        }

        for (String id : value.split(",")) {
            try {
                history.add(Integer.parseInt(id.trim()));
            } catch (NumberFormatException e) {
                System.err.println("Ошибка при преобразовании истории: " + e.getMessage());
            }
        }
        return history;
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

    @Override
    public Task getTaskById(int id) {
        Task task = super.getTaskById(id);
        save();
        return task;
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = super.getEpicById(id);
        save();
        return epic;
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = super.getSubtaskById(id);
        save();
        return subtask;
    }
}