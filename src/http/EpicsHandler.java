package http;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import enums.Status;
import managers.TaskManager;
import tasks.Epic;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

class EpicsHandler implements HttpHandler {
    private final TaskManager taskManager;
    private final Gson gson;

    public EpicsHandler(TaskManager taskManager, Gson gson) {
        this.taskManager = taskManager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            if ("POST".equals(method)) {
                handlePost(exchange);
            } else {
                sendResponse(exchange, 405, "Method Not Allowed");
            }
        } catch (Exception e) {
            sendResponse(exchange, 500, "Internal Server Error");
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        InputStream inputStream = exchange.getRequestBody();
        String body = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

        try {
            JsonElement jsonElement = JsonParser.parseString(body);
            JsonObject jsonObject = jsonElement.getAsJsonObject();


            if (!jsonObject.has("title") || !jsonObject.has("description") || !jsonObject.has("status")) {
                sendResponse(exchange, 400, "Missing required fields");
                return;
            }

            String title = jsonObject.get("title").getAsString();
            String description = jsonObject.get("description").getAsString();
            Status status = Status.valueOf(jsonObject.get("status").getAsString());

            Epic epic = new Epic(title, description, status, null, null);
            Epic createdEpic = taskManager.createEpic(epic);

            String response = gson.toJson(createdEpic);
            sendResponse(exchange, 201, response);
        } catch (Exception e) {
            sendResponse(exchange, 400, "Invalid request body");
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