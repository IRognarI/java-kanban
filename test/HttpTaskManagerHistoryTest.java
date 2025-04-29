import enums.Status;
import http.HttpTaskServer;
import org.junit.jupiter.api.*;
import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import managers.InMemoryTaskManager;
import managers.TaskManager;
import tasks.Task;

class HttpTaskManagerHistoryTest {
    private TaskManager manager;
    private HttpTaskServer taskServer;
    private HttpClient client;

    @BeforeEach
    void setUp() throws IOException {
        manager = new InMemoryTaskManager();
        taskServer = new HttpTaskServer(manager);
        client = HttpClient.newHttpClient();
        taskServer.start();
    }

    @AfterEach
    void tearDown() {
        taskServer.stop();
        manager.deleteTasks();
        manager.deleteEpics();
        manager.deleteSubtasks();
    }

    @Test
    void testGetHistory() throws IOException, InterruptedException {
       
        Task task = manager.createTask(new Task("Test Task", "Description", Status.NEW,
                null, null));

       
        manager.getTaskById(task.getId());

       
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/history"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

       
        assertEquals(200, response.statusCode(), "Неверный статус код");

       
        List<Task> history = manager.getHistory();
        assertNotNull(history, "История не должна быть null");
        assertEquals(1, history.size(), "В истории должна быть 1 задача");
        assertEquals(task.getId(), history.get(0).getId(), "ID задачи в истории не совпадает");
    }

    @Test
    void testGetEmptyHistory() throws IOException, InterruptedException {
       
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/history"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Неверный статус код");
        assertEquals(0, manager.getHistory().size(), "История должна быть пустой");
    }
}