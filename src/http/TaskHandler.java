package http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import managers.TaskManager;
import tasks.Task;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

class TaskHttpHandler implements HttpHandler {
    private final TaskManager taskManager;
    private final Gson gson;

    public TaskHttpHandler(TaskManager taskManager, Gson gson) {
        this.taskManager = taskManager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String query = exchange.getRequestURI().getQuery();

        try {
            switch (method) {
                case "GET":
                    handleGet(exchange, query);
                    break;
                case "POST":
                    handlePost(exchange);
                    break;
                case "DELETE":
                    handleDelete(exchange, query);
                    break;
                default:
                    sendResponse(exchange, 405, "Method Not Allowed");
            }
        } catch (Exception e) {
            sendResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }

    private void handleGet(HttpExchange exchange, String query) throws IOException {
        if (query == null) {
           
            String response = gson.toJson(taskManager.getAllTasks());
            sendResponse(exchange, 200, response);
        } else {
           
            Optional<Integer> taskId = parseIdFromQuery(query);
            if (taskId.isEmpty()) {
                sendResponse(exchange, 400, "Invalid task id");
                return;
            }

            Task task = taskManager.getTaskById(taskId.get());
            if (task == null) {
                sendResponse(exchange, 404, "Task not found");
            } else {
                sendResponse(exchange, 200, gson.toJson(task));
            }
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        InputStream input = exchange.getRequestBody();
        String body = new String(input.readAllBytes(), StandardCharsets.UTF_8);

        try {
            Task task = gson.fromJson(body, Task.class);
            if (task == null) {
                sendResponse(exchange, 400, "Invalid task data");
                return;
            }

            if (task.getId() != 0) {
                taskManager.updateTask(task);
                sendResponse(exchange, 200, "Task updated");
            } else {
                taskManager.createTask(task);
                sendResponse(exchange, 201, "Task created");
            }
        } catch (Exception e) {
            sendResponse(exchange, 400, "Invalid task format");
        }
    }

    private void handleDelete(HttpExchange exchange, String query) throws IOException {
           
            Optional<Integer> taskId = parseIdFromQuery(query);
            if (taskId.isEmpty()) {
                sendResponse(exchange, 400, "Invalid task id");
                return;
            }

            taskManager.removeTask(taskId.get());
            sendResponse(exchange, 200, "Task deleted");
    }

    private Optional<Integer> parseIdFromQuery(String query) {
        try {
            String[] parts = query.split("=");
            if (parts.length != 2 || !parts[0].equals("id")) {
                return Optional.empty();
            }
            return Optional.of(Integer.parseInt(parts[1]));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
}