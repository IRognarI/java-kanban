public class Main {

    public static void main(String[] args) {
        TaskManager taskManager = new TaskManager();

        Task task1 = taskManager.createTask("Переезд в другую квартиру", "Упаковать вещи");
        Epic epic1 = taskManager.createEpic("Уборка", "Заказать клининг");
        Subtask subtask1 = taskManager.createSubtask("Распаковать вещи", "Разложить" +
                " вещи по местам", epic1.getId());
        Subtask subtask2 = taskManager.createSubtask("Купить продукты",
                "Приготовить ужин", epic1.getId());

        System.out.println("Task 1 status: " + task1.getStatus());
        System.out.println("Epic 1 status: " + epic1.getStatus());
        System.out.println("Subtask 1 status: " + subtask1.getStatus());

        subtask1.setStatus(Status.DONE);
        taskManager.updateSubtask(subtask1);

        System.out.println("Subtask 1 updated status: " + subtask1.getStatus());
        System.out.println("Epic 1 status after updating subtask 1: " + epic1.getStatus());
    }
}
