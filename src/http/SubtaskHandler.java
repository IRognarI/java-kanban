package http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import managers.TaskManager;
import tasks.Subtask;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

class SubtaskHandler implements HttpHandler {
    private final TaskManager taskManager;
    private final Gson gson;

    public SubtaskHandler(TaskManager taskManager, Gson gson) {
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
            sendResponse(exchange, 200, gson.toJson(taskManager.getAllSubtasks()));
        } else {
            Optional<Integer> subtaskId = parseIdFromQuery(query);
            if (subtaskId.isEmpty()) {
                sendResponse(exchange, 400, "Invalid subtask id");
                return;
            }

            Subtask subtask = taskManager.getSubtaskById(subtaskId.get());
            if (subtask == null) {
                sendResponse(exchange, 404, "Subtask not found");
            } else {
                sendResponse(exchange, 200, gson.toJson(subtask));
            }
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        InputStream input = exchange.getRequestBody();
        String body = new String(input.readAllBytes(), StandardCharsets.UTF_8);

        try {
            Subtask subtask = gson.fromJson(body, Subtask.class);
            if (subtask == null) {
                sendResponse(exchange, 400, "Invalid subtask data");
                return;
            }

            if (subtask.getId() != 0) {
                taskManager.updateSubtask(subtask);
                sendResponse(exchange, 200, "Subtask updated");
            } else {
                taskManager.createSubtask(subtask);
                sendResponse(exchange, 201, "Subtask created");
            }
        } catch (Exception e) {
            sendResponse(exchange, 400, "Invalid subtask format");
        }
    }

    private void handleDelete(HttpExchange exchange, String query) throws IOException {

        Optional<Integer> subtaskId = parseIdFromQuery(query);
        if (subtaskId.isEmpty()) {
            sendResponse(exchange, 400, "Invalid subtask id");
            return;
        }

        taskManager.removeSubtask(subtaskId.get());
        sendResponse(exchange, 200, "Subtask deleted");
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