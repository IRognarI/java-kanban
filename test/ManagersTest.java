import managers.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ManagersTest {

    @Test
    void returnInMemoryTaskManagerClass() {
        TaskManager manager = Managers.getDefault();
        Assertions.assertTrue(manager instanceof InMemoryTaskManager);
    }

    @Test
    void returnInMemoryHistoryManagerClass() {
        HistoryManager manager = Managers.getDefaultHistory();
        Assertions.assertTrue(manager instanceof InMemoryHistoryManager);
    }
}