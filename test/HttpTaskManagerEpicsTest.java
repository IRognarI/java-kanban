import http.HttpTaskServer;
import org.junit.jupiter.api.*;
import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import static org.junit.jupiter.api.Assertions.*;
import managers.InMemoryTaskManager;
import managers.TaskManager;

class HttpTaskManagerEpicsTest {
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
    void testCreateEpic() throws IOException, InterruptedException {
       
        String epicJson = """
        {
            "title": "Test Epic",
            "description": "Test Description",
            "status": "NEW"
        }
        """;

       
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();

       
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

       
        assertEquals(201, response.statusCode(), "Неверный статус код при создании эпика");

       
        var epics = manager.getAllEpics();
        assertEquals(1, epics.size(), "Должен быть создан ровно один эпик");
        assertEquals("Test Epic", epics.get(0).getTitle(), "Название эпика не совпадает");
    }

    @Test
    void testCreateEpicWithInvalidData() throws IOException, InterruptedException {
       
        String invalidEpicJson = """
        {
            "title": "Invalid Epic",
            "status": "NEW"
        }
        """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(invalidEpicJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(), "Ожидалась ошибка 400 при неполных данных");
    }
}