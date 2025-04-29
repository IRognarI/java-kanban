package http;

import com.google.gson.*;
import com.sun.net.httpserver.HttpServer;
import managers.Managers;
import managers.TaskManager;
import enums.Status;
import tasks.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private final HttpServer server;
    private final TaskManager taskManager;
    private final Gson gson;

    public HttpTaskServer(TaskManager taskManager) throws IOException {
        this.taskManager = taskManager;
        this.gson = createCustomGson();
        this.server = HttpServer.create(new InetSocketAddress(PORT), 0);
        configureEndpoints();
    }

    private Gson createCustomGson() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .registerTypeAdapter(Status.class, new StatusAdapter())
                .registerTypeAdapter(Task.class, new TaskInterfaceAdapter())
                .registerTypeAdapter(Epic.class, new EpicInterfaceAdapter())
                .registerTypeAdapter(Subtask.class, new SubtaskInterfaceAdapter())
                .setPrettyPrinting()
                .create();
    }

    private void configureEndpoints() {
        server.createContext("/tasks", new TaskHandler(taskManager, gson));
        server.createContext("/subtasks", new SubtaskHandler(taskManager, gson));
        server.createContext("/epics", new EpicsHandler(taskManager, gson));
        server.createContext("/history", new HistoryHandler(taskManager, gson));
        server.createContext("/prioritized", new PrioritizedHandler(taskManager, gson));
    }

    public void start() {
        server.start();
        System.out.println("HTTP Task Server started on port " + PORT);
    }

    public void stop() {
        server.stop(0);
        System.out.println("HTTP Task Server stopped");
    }

    public Gson getGson() {
        return gson;
    }

    private static class LocalDateTimeAdapter implements JsonSerializer<LocalDateTime>,
            JsonDeserializer<LocalDateTime> {
        @Override
        public JsonElement serialize(LocalDateTime src, Type typeOfSrc,
                                     JsonSerializationContext context) {
            return src == null ? JsonNull.INSTANCE : new JsonPrimitive(src.toString());
        }

        @Override
        public LocalDateTime deserialize(JsonElement json, Type typeOfT,
                                         JsonDeserializationContext context) throws JsonParseException {
            return json.isJsonNull() ? null : LocalDateTime.parse(json.getAsString());
        }
    }

    private static class DurationAdapter implements JsonSerializer<Duration>,
            JsonDeserializer<Duration> {
        @Override
        public JsonElement serialize(Duration src, Type typeOfSrc,
                                     JsonSerializationContext context) {
            return src == null ? JsonNull.INSTANCE : new JsonPrimitive(src.toString());
        }

        @Override
        public Duration deserialize(JsonElement json, Type typeOfT,
                                    JsonDeserializationContext context) throws JsonParseException {
            return json.isJsonNull() ? null : Duration.parse(json.getAsString());
        }
    }

    private static class StatusAdapter implements JsonSerializer<Status>,
            JsonDeserializer<Status> {
        @Override
        public JsonElement serialize(Status src, Type typeOfSrc,
                                     JsonSerializationContext context) {
            return new JsonPrimitive(src.name());
        }

        @Override
        public Status deserialize(JsonElement json, Type typeOfT,
                                  JsonDeserializationContext context) throws JsonParseException {
            try {
                return Status.valueOf(json.getAsString());
            } catch (IllegalArgumentException e) {
                return Status.NEW;
            }
        }
    }

    private static class TaskInterfaceAdapter implements JsonDeserializer<Task> {
        @Override
        public Task deserialize(JsonElement json, Type typeOfT,
                                JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            String title = jsonObject.get("title").getAsString();
            String description = jsonObject.get("description").getAsString();
            Status status = Status.valueOf(jsonObject.get("status").getAsString());

            LocalDateTime startTime = jsonObject.has("startTime")
                    ? LocalDateTime.parse(jsonObject.get("startTime").getAsString())
                    : null;

            Duration duration = jsonObject.has("duration")
                    ? Duration.parse(jsonObject.get("duration").getAsString())
                    : null;

            return new Task(title, description, status, startTime, duration);
        }
    }

    private static class EpicInterfaceAdapter implements JsonDeserializer<Epic> {
        @Override
        public Epic deserialize(JsonElement json, Type typeOfT,
                                JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            String title = jsonObject.get("title").getAsString();
            String description = jsonObject.get("description").getAsString();
            Status status = Status.valueOf(jsonObject.get("status").getAsString());

            return new Epic(title, description, status, null, null);
        }
    }

    private static class SubtaskInterfaceAdapter implements JsonDeserializer<Subtask> {
        @Override
        public Subtask deserialize(JsonElement json, Type typeOfT,
                                   JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            String title = jsonObject.get("title").getAsString();
            String description = jsonObject.get("description").getAsString();
            Status status = Status.valueOf(jsonObject.get("status").getAsString());
            int epicId = jsonObject.get("epicId").getAsInt();

            LocalDateTime startTime = jsonObject.has("startTime")
                    ? LocalDateTime.parse(jsonObject.get("startTime").getAsString())
                    : null;

            Duration duration = jsonObject.has("duration")
                    ? Duration.parse(jsonObject.get("duration").getAsString())
                    : null;

            return new Subtask(title, description, status, epicId, startTime, duration);
        }
    }

    public static void main(String[] args) throws IOException {
        TaskManager manager = Managers.getDefault();
        HttpTaskServer server = new HttpTaskServer(manager);
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
    }
}