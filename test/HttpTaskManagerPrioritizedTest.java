import enums.Status;
import http.HttpTaskServer;
import org.junit.jupiter.api.*;
import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
import managers.InMemoryTaskManager;
import managers.TaskManager;
import tasks.Task;

class HttpTaskManagerPrioritizedTest {
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
    void testGetPrioritizedTasks() throws IOException, InterruptedException {
       
        Task task = new Task(
                "Test Task",
                "Test Description",
                Status.NEW,
                LocalDateTime.now(),
                Duration.ofMinutes(30)
        );
        Task createdTask = manager.createTask(task);

       
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/prioritized"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

       
        assertEquals(200, response.statusCode(), "Неверный статус код");
        assertFalse(response.body().isEmpty(), "Тело ответа не должно быть пустым");

       
        Set<Task> prioritizedTasks = (Set<Task>) manager.getPrioritizedTasks(manager.getAllTasks());
        assertEquals(1, prioritizedTasks.size(), "В списке должна быть 1 задача");
        assertTrue(prioritizedTasks.contains(createdTask), "Список должен содержать созданную задачу");
    }

    @Test
    void testGetEmptyPrioritizedTasks() throws IOException, InterruptedException {
       
        manager.createTask(new Task(
                "Task without time",
                "Description",
                Status.NEW,
                null,
                null
        ));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/prioritized"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Неверный статус код");

        Set<Task> prioritizedTasks = (Set<Task>) manager.getPrioritizedTasks(manager.getAllTasks());
        assertEquals(0, prioritizedTasks.size(), "Задачи без времени не должны попадать в список");
    }
}