package tasks;

import exception.LocalDateTimeException;
import status.Status;
import status.TaskType;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Класс для создания основной задачи
 */
public class Task implements Comparable<Task>{
    private int id;
    private final String title;
    private final String description;
    private Status status;
    private final TaskType type = TaskType.TASK;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Duration duration;

    public Task(String title, String description, Status status, LocalDateTime startTime, LocalDateTime endTime, Duration duration) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.startTime = startTime;
        this.duration = duration;
        this.endTime = endTime;
    }

    /*public Task(String title, String description) {
        this.title = title;
        this.description = description;
    }*/

    /*public Task(String title, String description, Status status) {
        this.title = title;
        this.description = description;
        this.status = status;
    }*/

    /*public Task(int id, String title, String description, Status status, LocalDateTime startTime, Duration duration) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.startTime = startTime;
        this.duration = duration;
    }*/

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

    public Long getDuration() {
        return duration.toMinutes();
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public LocalDateTime getEndTime() {
        try{
            if (startTime == null) {
                throw new  NullPointerException("Время начала выполнения задачи не указано!");
            }
            if (duration == Duration.ZERO) {
                throw new LocalDateTimeException("Продолжительность выполнения задачи не может быть: " + duration);
            }
        } catch (NullPointerException e) {
            System.out.println(e.getMessage());
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }
        return endTime.plus(duration);
    }

    @Override
    public int compareTo(Task o) {
        return this.getStartTime().compareTo(o.getStartTime());
    }
}