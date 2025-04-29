import enums.Status;
import http.HttpTaskServer;
import org.junit.jupiter.api.*;
import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;
import managers.InMemoryTaskManager;
import managers.TaskManager;
import tasks.Task;

class HttpTaskManagerTasksTest {
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
    }

    @Test
    void testGetTask() throws IOException, InterruptedException {
       
        Task createdTask = manager.createTask(new Task(
                "Test Task",
                "Test Description",
                Status.NEW,
                LocalDateTime.now(),
                Duration.ofMinutes(30)
        ));

       
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks?id=" + createdTask.getId()))
                .GET()
                .build();

       
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

       
        assertEquals(200, response.statusCode(), "Неверный статус код ответа");

       
        Task retrievedTask = manager.getTaskById(createdTask.getId());
        assertNotNull(retrievedTask, "Задача должна существовать");
        assertEquals(createdTask.getId(), retrievedTask.getId(), "ID задач не совпадают");
        assertEquals("Test Task", retrievedTask.getTitle(), "Название задачи не совпадает");
    }

    @Test
    void testGetNonExistentTask() throws IOException, InterruptedException {
       
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks?id=999"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "Для несуществующей задачи должен быть 404");
    }
}