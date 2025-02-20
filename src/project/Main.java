package project;

public class Main {

    public static void main(String[] args) {
        TaskManager taskManager = new InMemoryTaskManager();

        Task task1 = new Task("Переезд в другую квартиру", "Упаковать вещи", Status.NEW);
        taskManager.createTask(task1);

        Epic epic1 = new Epic("Уборка", "Заказать клининг", Status.NEW);
        taskManager.createEpic(epic1);

        Subtask subtask1 = new Subtask("Распаковать вещи", "Разложить вещи по местам", Status.NEW, epic1.getId());
        taskManager.createSubtask(subtask1);

        Subtask subtask2 = new Subtask("Купить продукты", "Приготовить ужин", Status.NEW, epic1.getId());
        taskManager.createSubtask(subtask2);

        System.out.println("project.Task 1 status: " + task1.getStatus());
        System.out.println("project.Epic 1 status: " + epic1.getStatus());
        System.out.println("project.Subtask 1 status: " + subtask1.getStatus());
        System.out.println("project.Subtask 2 status: " + subtask2.getStatus());

        subtask1.setStatus(Status.DONE);
        taskManager.updateSubtask(subtask1);

        System.out.println("project.Subtask 1 updated status: " + subtask1.getStatus());
        System.out.println("project.Epic 1 status after updating subtask 1: " + epic1.getStatus());

        subtask2.setStatus(Status.IN_PROGRESS);
        taskManager.updateSubtask(subtask2);

        System.out.println("project.Subtask 2 updated status: " + subtask2.getStatus());
        System.out.println("project.Epic 1 status after updating subtask 2: " + epic1.getStatus());

        taskManager.removeSubtask(subtask1.getId());
        System.out.println("project.Epic 1 status after removing subtask 1: " + epic1.getStatus());

        taskManager.removeEpic(epic1.getId());
        System.out.println("project.Epic 1 removed. Subtasks count: " + taskManager.getAllSubtasks().size());
    }
}