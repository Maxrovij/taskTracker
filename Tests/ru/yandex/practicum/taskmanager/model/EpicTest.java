package ru.yandex.practicum.taskmanager.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.taskmanager.controller.InMemoryTaskManager;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EpicTest {

    InMemoryTaskManager manager;
    Epic task;

    @BeforeEach
    public void createNewManagerAndEpicTask() {
        manager = new InMemoryTaskManager();
        task = new Epic("1", "name", "title");
        manager.tasks.put(task.getId(), task);
    }

   @Test // проверяем, что у эпика нет подзадач и при этом статус Эпика - NEW
    public void shouldBeInStatusNewWhenThereAreNoSubtasks() {
       assertTrue(task.subTasksId.isEmpty());
       assertEquals(Statuses.NEW, task.getStatus());
   }

   @Test // проверяем, что эпик в статусе NEW если все сабтаски в статусе NEW
    public void shouldBeInStatusNewWhenAllSubtasksAreInStatusNew() {
        manager.addSubTask(task, "name", "title");
        manager.addSubTask(task, "name", "title");
        manager.addSubTask(task, "name", "title");
        assertEquals(Statuses.NEW, manager.tasks.get("1").getStatus());
   }

   @Test // проверяем, что Эпик будет в статусе DONE, если выполнить все его сабтаски
    public void shouldBeInStatusDoneWhenAllSubtasksAreInStatusDone() {
       manager.addSubTask(task, "name", "title");
       manager.addSubTask(task, "name", "title");
       manager.addSubTask(task, "name", "title");
       for (String s : manager.tasks.keySet()) {
           try {
               manager.updateTask(s, "Name", "Title", Statuses.DONE);
           }catch (UnsupportedOperationException ex) {
               System.out.println(ex.getMessage());
           }
       }
       assertEquals(Statuses.DONE, manager.tasks.get("1").getStatus());
   }

   @Test // проверяем, что Эпик будетв статусе IN_PROGRESS если его сабтаски в статусах NEW и DONE
    public void shouldBeInStatusInProgressWhenSubTasksAreInStatusNewAndDone() {
       manager.addSubTask(task, "name", "title");
       manager.addSubTask(task, "name", "title");
       manager.addSubTask(task, "name", "title");
       List<Task> subs = new ArrayList<>();
       for (Task t : manager.tasks.values()) {
           if (t.getType() != TaskTypes.EPIC) subs.add(t);
       }
       manager.updateTask(subs.get(1).getId(), "Name", "Title", Statuses.DONE);
       assertEquals(Statuses.IN_PROGRESS, manager.tasks.get("1").getStatus());
   }

   @Test // проверяем, что Эпик будет в статусе IN_PROGRESS если все его подзадачи в статусе IN_PROGRESS
    public void shouldBeInStatusInProgressWhenSubTasksAreInStatusInProgress() {
       manager.addSubTask(task, "name", "title");
       manager.addSubTask(task, "name", "title");
       manager.addSubTask(task, "name", "title");
       for (Task t : manager.tasks.values()) {
            try {
                manager.updateTask(t.getId(), "Name", "Title", Statuses.IN_PROGRESS);
            }catch (UnsupportedOperationException ex) {
                System.out.println(ex.getMessage());
            }
       }
       assertEquals(Statuses.IN_PROGRESS, manager.tasks.get("1").getStatus());
   }
}