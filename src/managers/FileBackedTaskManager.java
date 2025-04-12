package managers;

import exception.ManagerSaveException;
import status.Status;
import status.TaskType;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    private static Task fromString(String value) throws IllegalArgumentException {

        try {
            if (value == null || value.isEmpty()) {
                throw new NullPointerException("Не корректная инициализации строки!");
            }
        } catch (NullPointerException e) {
            System.out.println(e.getMessage());
        }

        String[] param = value.split("\n");
        for (int i = 0; i < param.length; i++) {
            String[] values = param[i].split(",");
            String someTask = values[0];

            if (someTask.equals("TASK")) {

                String title = values[1];
                String description = values[2];
                Status status = Status.valueOf(values[3]);
                LocalDateTime startTime = LocalDateTime.parse(values[4]);
                LocalDateTime endTime = LocalDateTime.parse(values[5]);
                Duration duration = Duration.parse(values[6]);

                Task task = new Task(title, description, status, startTime, endTime, duration);
                return task;
            }

            if (someTask.equals("EPIC")) {

                String title = values[1];
                String description = values[2];
                Status status = Status.valueOf(values[3]);
                LocalDateTime startTime = LocalDateTime.parse(values[4]);
                LocalDateTime endTime = LocalDateTime.parse(values[5]);
                Duration duration = Duration.parse(values[6]);

                Epic epic = new Epic(title, description, status, startTime, endTime, duration);
                return epic;
            }

            if (someTask.equals("SUBTASK")){
                String title = values[1];
                String description = values[2];
                Status status = Status.valueOf(values[3]);
                int epicID = Integer.parseInt(values[4]);
                LocalDateTime startTime = LocalDateTime.parse(values[5]);
                LocalDateTime endTime = LocalDateTime.parse(values[6]);
                Duration duration = Duration.parse(values[7]);

                Subtask subtask = new Subtask(title, description, status, epicID, startTime, endTime, duration);
                return subtask;
            }
        }
        throw new IllegalArgumentException("Не известный тип задачи");
    }

    private void save() {
        try {
            if (!file.exists()) {
                throw new FileNotFoundException("Файл не существует!");
            }
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            for (Task task : getAllTasks()) {
                writer.write(String.format("%s," + "%s,%s,%s,%s,%s,%s%n", TaskType.TASK, task.getTitle(),
                        task.getDescription(), task.getStatus(), task.getStartTime(), task.getEndTime(),
                        task.getDuration()));
            }
            for (Epic epic : getAllEpics()) {
                writer.write(String.format("%n%s," + "%s,%s,%s,%s,%s,%s%n", TaskType.EPIC, epic.getTitle(),
                        epic.getDescription(), epic.getStatus(), epic.getStartTime(), epic.getEndTime(),
                        epic.getDuration()));
            }
            for (Subtask subtask : getAllSubtasks()) {
                writer.write(String.format("%n%s," + "%s,%s,%s,%s,%s,%s,%s%n", TaskType.SUBTASK, subtask.getTitle(),
                        subtask.getDescription(), subtask.getStatus(), subtask.getEpicId(), subtask.getStartTime(),
                        subtask.getEndTime(), subtask.getDuration()));
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка записи файла" + e.getMessage());
        }
    }

    public void loadFromFile(File file) {
        try (BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {

            String line;
            while (reader.ready()) {
                line = reader.readLine();

                if (line.isEmpty()) {
                    continue;
                }

                try {
                    if (fromString(line) instanceof Task task) {
                    super.tasks.put(getIdCounter(), task);
                    }
                    if (fromString(line) instanceof Epic epic) {
                        super.epics.put(getIdCounter(), epic);
                    }
                    if (fromString(line) instanceof Subtask subtask) {
                        super.subtasks.put(getIdCounter(), subtask);
                    }
                } catch (IllegalArgumentException e) {
                    System.out.println(e.getMessage());
                    break;
                }
            }

        } catch (IOException e) {
            System.out.println("Ошибка чтения файла: " + e.getMessage());
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

    public void getMethodSave() {
        save();
    }
}