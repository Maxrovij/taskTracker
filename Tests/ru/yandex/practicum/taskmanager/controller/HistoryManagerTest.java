package ru.yandex.practicum.taskmanager.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.taskmanager.model.Epic;
import ru.yandex.practicum.taskmanager.model.SubTask;
import ru.yandex.practicum.taskmanager.model.Task;

class HistoryManagerTest {
    protected HistoryManager manager;

    @BeforeEach
    void createManager() {
        this.manager = new InMemoryHistoryManager();
    }

    @Test // проверяем, что задача добавляется в пустой список
    void shouldAddTaskToEmptyHistory() {
        Task task = new Task("1", "name", "title");
        manager.add(task);
        Assertions.assertTrue(manager.getHistory().contains(task));
    }

    @Test // задача удаляется из середины списка
    void shouldRemoveFromMiddle() {
        Task task = new Task("1", "name", "title");
        Epic epic = new Epic("2", "name", "title");
        SubTask sub = new SubTask("3","name", "title", epic.getId());
        manager.add(task);
        manager.add(epic);
        manager.add(sub);
        Assertions.assertTrue(manager.getHistory().size() == 3
                &&manager.getHistory().get(0).getId().equals("1")
                && manager.getHistory().get(1).getId().equals("2")
                && manager.getHistory().get(2).getId().equals("3"));
        manager.remove(epic.getId());
        Assertions.assertTrue(manager.getHistory().size() == 2
                && manager.getHistory().get(0).getId().equals("1")
                && manager.getHistory().get(1).getId().equals("3"));
     }

    @Test // задача удаляется из начала списка
    void shouldRemoveFromHead() {
        Task task = new Task("1", "name", "title");
        Epic epic = new Epic("2", "name", "title");
        SubTask sub = new SubTask("3", "name", "title", epic.getId());
        manager.add(task);
        manager.add(epic);
        manager.add(sub);
        Assertions.assertTrue(manager.getHistory().size() == 3
                &&manager.getHistory().get(0).getId().equals("1")
                && manager.getHistory().get(1).getId().equals("2")
                && manager.getHistory().get(2).getId().equals("3"));
        manager.remove(task.getId());
        Assertions.assertTrue(manager.getHistory().size() == 2
                && manager.getHistory().get(0).getId().equals("2")
                && manager.getHistory().get(1).getId().equals("3"));
    }

    @Test // задача удаляется из конца списка
    void shouldRemoveFromTail() {
        Task task = new Task("1", "name", "title");
        Epic epic = new Epic("2", "name", "title");
        SubTask sub = new SubTask("3", "name", "title", epic.getId());
        manager.add(task);
        manager.add(epic);
        manager.add(sub);
        Assertions.assertTrue(manager.getHistory().size() == 3
                &&manager.getHistory().get(0).getId().equals("1")
                && manager.getHistory().get(1).getId().equals("2")
                && manager.getHistory().get(2).getId().equals("3"));
        manager.remove(sub.getId());
        Assertions.assertTrue(manager.getHistory().size() == 2
                && manager.getHistory().get(0).getId().equals("1")
                && manager.getHistory().get(1).getId().equals("2"));
    }

    @Test // проверяем, что задачи в истории не дублируются
    void shouldNotSaveTaskInHistoryIfTiIsAlreadyThere() {
        Task task = new Task("1", "name", "title");
        Epic epic = new Epic("2", "name", "title");
        SubTask sub = new SubTask("3", "name", "title", epic.getId());
        manager.add(task);
        manager.add(epic);
        manager.add(sub);
        manager.add(task);
        manager.add(sub);
        Assertions.assertEquals(3, manager.getHistory().size());
    }
}