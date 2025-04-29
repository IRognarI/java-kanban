import com.google.gson.Gson;
import enums.Status;
import http.HttpTaskServer;
import managers.InMemoryTaskManager;
import managers.TaskManager;
import org.junit.jupiter.api.*;
import tasks.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

class HttpTaskManagerSubtasksTest {
    private static TaskManager manager;
    private static HttpTaskServer taskServer;
    private static Gson gson;
    private static HttpClient client;
    private static Epic epic;

    @BeforeAll
    static void beforeAll() throws IOException {
        manager = new InMemoryTaskManager();
        taskServer = new HttpTaskServer(manager);
        gson = taskServer.getGson();
        client = HttpClient.newHttpClient();
        epic = manager.createEpic(new Epic("Epic", "Epic", Status.NEW, null, null));
    }

    @BeforeEach
    void setUp() {
        manager.deleteSubtasks();
        taskServer.start();
    }

    @AfterEach
    void tearDown() {
        taskServer.stop();
    }

    @Test
    void testCreateSubtask() throws IOException, InterruptedException {
        Subtask subtask = new Subtask("Subtask", "Subtask", Status.NEW, epic.getId(),
                LocalDateTime.now(), Duration.ofMinutes(5));
        String subtaskJson = gson.toJson(subtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(201, response.statusCode());

        List<Subtask> subtasks = manager.getAllSubtasks();
        Assertions.assertEquals(1, subtasks.size());
        Assertions.assertEquals(epic.getId(), subtasks.get(0).getEpicId());
    }
}