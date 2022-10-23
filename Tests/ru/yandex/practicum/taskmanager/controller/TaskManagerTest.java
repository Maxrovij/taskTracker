package ru.yandex.practicum.taskmanager.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.taskmanager.model.*;

import java.time.*;
import java.time.format.DateTimeFormatter;

public abstract class TaskManagerTest<T extends TaskManager> {

    protected T manager;

    // вспомогательный метод для установки времени начала задачи
    private Instant createInstantStartTime (String year, String month, String day, String hours, String minutes) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm");
        String enteredTime = day + "." + month + "." + year + ", " + hours + ":" + minutes;
        ZonedDateTime zonedStart = ZonedDateTime.of(
                LocalDateTime.parse(enteredTime, formatter),
                ZoneId.of("Europe/Moscow")
        );
        return zonedStart.toInstant();
    }

    // вспомогательный метод для установки продолжительности задачи
    private Duration createDuration(String days, String hours, String minutes) {
        return Duration
                .ofDays(Integer.parseInt(days))
                .plusHours(Integer.parseInt(hours))
                .plusMinutes(Integer.parseInt(minutes));
    }

    @Test // проверяем, что добавленные задачи хранятся и выводятся именно в порядке приоритета по времени начала
    void shouldGetPrioritizedTasksList() {
        manager.addSimpleTask("name", "title");
        manager.addEpicTask("name", "title");
        manager.addSimpleTask("name", "title");

        manager.setTaskStartTime(
                manager.getPrioritizedTasks().get(1).getId(),
                createInstantStartTime(
                        "2022",
                        "03",
                        "23",
                        "14",
                        "48"));

        manager.setTaskStartTime(
                manager.getPrioritizedTasks().get(2).getId(),
                createInstantStartTime(
                        "2022",
                        "03",
                        "23",
                        "13",
                        "48"));

        Assertions.assertTrue(
                manager.getPrioritizedTasks().get(0).getStartTime().get()
                        .isBefore(manager.getPrioritizedTasks().get(1).getStartTime().get())
                        && manager.getPrioritizedTasks().get(2).getType() == TaskTypes.EPIC
                        && manager.getPrioritizedTasks().get(2).getStartTime().isEmpty()
        );
    }

    @Test // проверяем, что возвращаемый список пуст, если не добавлять задачи
    void shouldGetEmptyPrioritizedTasksListIfTAsksAreNotAdded() {
        Assertions.assertTrue(manager.getPrioritizedTasks().isEmpty());
    }

    @Test // устанавливает время начала задачи и проверяет, что оно установилось и соответствует ожидаемому
    void shouldSetTaskStartTime() {
        manager.addSimpleTask("name", "title");
        manager.setTaskStartTime(
                manager.getPrioritizedTasks().get(0).getId(),
                createInstantStartTime(
                        "2022",
                        "03",
                        "23",
                        "12",
                        "52"));
        Instant instant = ZonedDateTime.of(
                LocalDateTime.of(
                        2022,
                        03,
                        23,
                        12,
                        52),
                ZoneId.of("Europe/Moscow")
        ).toInstant();
        Assertions.assertEquals(instant, manager.getPrioritizedTasks().get(0).getStartTime().get());
    }

    @Test // проверяет, что при установке с неправильным Айди, метод выбрасывает исключение
    void shouldThrowNPETryingToSetTaskStartTimeWithWrongID() {
        manager.addSimpleTask("name", "title");
        NullPointerException ex = Assertions.assertThrows(NullPointerException.class, () -> {
            manager.setTaskStartTime(
                    "manager.getPrioritizedTasks().get(0).getId()",
                    createInstantStartTime(
                            "2022",
                            "03",
                            "23",
                            "12",
                            "52"));
        });
        Assertions.assertEquals(ex.getClass(), NullPointerException.class);
    }

    @Test // проверяет, что при установке с пустым списком задач, метод выбрасывает исключение
    void shouldThrowOutOfBoundsExceptionTryingToSetTaskStartTimeWithEmptyTAsks() {
        IndexOutOfBoundsException ex = Assertions.assertThrows(IndexOutOfBoundsException.class,
                () -> {manager.setTaskStartTime(
                manager.getPrioritizedTasks().get(0).getId(),
                createInstantStartTime(
                        "2022",
                        "03",
                        "23",
                        "12",
                        "52"));});
        Assertions.assertEquals(ex.getClass(), IndexOutOfBoundsException.class);
    }

    @Test // устанавливаем продолжительность задачи и проверяем, что она установилась и совпадает с той, что мы задали
    void shouldSetTaskDuration() {
        manager.addSimpleTask("name", "title");
        manager.setTaskDuration(
                manager.getPrioritizedTasks().get(0).getId(), createDuration("0", "0", "15"));
        Duration dur = Duration.ofMinutes(15);
        Assertions.assertEquals(dur, manager.getPrioritizedTasks().get(0).getDuration().get());
    }

    @Test // устанавливаем продолжительность задачи c неверным Айди и проверяем, что метод выбросил исключение
    void shouldThrowNPETryingToSetTaskDurationWithWrongID() {
        manager.addSimpleTask("name", "title");
        NullPointerException ex = Assertions.assertThrows(NullPointerException.class, () -> {manager.setTaskDuration(
                "manager.getPrioritizedTasks().get(0).getId()",
                createDuration("0", "0", "15"));});
        Assertions.assertEquals(NullPointerException.class, ex.getClass());
    }

    @Test // устанавливаем продолжительность задачи c пустым списком задач и проверяем, что метод выбросил исключение
    void shouldThrowNPETryingToSetTaskDurationWithEmptyTasks() {
        IndexOutOfBoundsException ex = Assertions.assertThrows(IndexOutOfBoundsException.class,
                () -> {manager.setTaskDuration(
                manager.getPrioritizedTasks().get(0).getId(),
                createDuration("0", "0", "15"));});
        Assertions.assertEquals(IndexOutOfBoundsException.class, ex.getClass());
    }

    @Test // добавляем простую задачу и проверяем, что в менеджере лежит именно простая задача
    void shouldAddSimpleTask() {
        manager.addSimpleTask("name", "title");
        Assertions.assertSame(manager.getPrioritizedTasks().get(0).getType(), TaskTypes.TASK);
    }

    @Test // добавляем новый эпик и проверяем, что в менеджере лежит именно Эпик
    void shouldAddEpicTask() {
        manager.addEpicTask("name", "title");
        Assertions.assertSame(manager.getPrioritizedTasks().get(0).getType(), TaskTypes.EPIC);
    }

    @Test // добавляем новую сабтаску и проверяем ее тип и наличие у нее ID родительского эпика
    void shouldAddSubTask() {
        manager.addEpicTask("name", "title");
        Epic epic = (Epic) manager.getPrioritizedTasks().get(0);
        manager.addSubTask(epic, "name", "title");
        SubTask sub = null;
        for (Task t : manager.getPrioritizedTasks()) if (t.getType() == TaskTypes.SUBTASK) sub = (SubTask) t;
        Assertions.assertSame(sub.getType(), TaskTypes.SUBTASK);
        Assertions.assertEquals(sub.getMotherTaskId(), epic.getId());
    }

    @Test // проверяем, что задача обновляется и все обновления соответствуют заданным
    void shouldUpdateTask() {
        manager.addSimpleTask("name", "title");
        Task task = manager.getPrioritizedTasks().get(0);
        manager.updateTask(
                task.getId(),
                "NewName",
                "NewTitle",
                Statuses.DONE);
        Assertions.assertTrue(
                task.getName().equals("NewName")
                        && task.getTitle().equals("NewTitle")
                        && task.getStatus() == Statuses.DONE);
    }

    @Test // проверяем, что метод выбрасывает исключение, если Айди неверный
    void shouldThrowNPETryingToUpdateTaskIfIdIsWrong() {
        manager.addSimpleTask("name", "title");
        NullPointerException ex = Assertions.assertThrows(NullPointerException.class, () -> {manager.updateTask(
                "task.getId()",
                "NewName",
                "NewTitle",
                Statuses.DONE);});
        Assertions.assertEquals(
                NullPointerException.class, ex.getClass());
    }

    @Test // проверяем, что метод выбрасывает исключение, если Айди неверный
    void shouldThrowOutOfBoundsExceptionTryingToUpdateTaskIfTasksEmpty() {
        IndexOutOfBoundsException ex = Assertions.assertThrows(IndexOutOfBoundsException.class,
                () -> {manager.updateTask(
                manager.getPrioritizedTasks().get(0).getId(),
                "NewName",
                "NewTitle",
                Statuses.DONE);});
        Assertions.assertEquals(
                IndexOutOfBoundsException.class, ex.getClass());
    }

    @Test // проверяем, что задача сначала добавляется в менеджер, а затем удаляется из него
    void shouldDeleteTask() {
        manager.addSimpleTask("name", "title");
        Assertions.assertFalse(manager.getPrioritizedTasks().isEmpty());
        manager.deleteTask(manager.getPrioritizedTasks().get(0).getId());
        Assertions.assertTrue(manager.getPrioritizedTasks().isEmpty());
    }

    @Test // проверяем, что метод выбрасывает исключение если Айди неверный
    void shouldThrowNPETryingToDeleteTaskWithWrongId() {
        manager.addSimpleTask("name", "title");
        Assertions.assertFalse(manager.getPrioritizedTasks().isEmpty());
        NullPointerException ex = Assertions.assertThrows(NullPointerException.class,
                () -> {manager.deleteTask("manager.getPrioritizedTasks().get(0).getId()");});
        Assertions.assertEquals(NullPointerException.class, ex.getClass());
    }

    @Test // проверяем, что метод выбрасывает исключение если не добавить задачи
    void shouldThrowOutOfBoundsExceptionTryingToDeleteTaskWithEmptyTAsks() {
        Assertions.assertTrue(manager.getPrioritizedTasks().isEmpty());
        IndexOutOfBoundsException ex = Assertions.assertThrows(IndexOutOfBoundsException.class,
                () -> {manager.deleteTask(manager.getPrioritizedTasks().get(0).getId());});
        Assertions.assertEquals(IndexOutOfBoundsException.class, ex.getClass());
    }

    @Test // проверяем, что Эпик печатается и добавляется в историю просмотров
    void shouldPrintEpicAndAddItToHistory() {
        manager.addEpicTask("name", "title");
        manager.getEpic(manager.getPrioritizedTasks().get(0).getId());
        Assertions.assertTrue(manager.history().contains(manager.getPrioritizedTasks().get(0)));
    }

    @Test // проверяем, что метод выбрасывает исключение если Айди неверный
    void shouldThrowNPETryingToPrintEpicAndAddItToHistoryWithWrongId() {
        manager.addEpicTask("name", "title");
        NullPointerException ex = Assertions.assertThrows(NullPointerException.class,
                () -> {manager.getEpic("manager.getPrioritizedTasks().get(0).getId()");});
        Assertions.assertEquals(NullPointerException.class, ex.getClass());
    }

    @Test // проверяем, что сабтаска печатается и добавляется в историю просмотров
    void shouldPrintSubTaskAndAddItToHistory() {
        manager.addEpicTask("name", "title");
        Epic ep = (Epic) manager.getPrioritizedTasks().get(0);
        manager.addSubTask(ep, "name", "title");
        manager.getSubtask(manager.getPrioritizedTasks().get(1).getId());
        Assertions.assertTrue(manager.history().contains(manager.getPrioritizedTasks().get(1)));
    }

    @Test // проверяем, что метод не добавляет сабтаску если Айди неверный
    void shouldNotPrintSubTaskAndAddItToHistoryWithWrongId() {
        manager.addEpicTask("name", "title");
        Epic ep = (Epic) manager.getPrioritizedTasks().get(0);
        manager.addSubTask(ep, "name", "title");
        manager.getSubtask("manager.getPrioritizedTasks().get(1).getId()");
        Assertions.assertTrue(manager.history().isEmpty());
    }

    @Test // проверяем, что простая задача печатается и добавляется в историю просмотров
    void shouldPrintTaskAndAddItToHistory() {
        manager.addSimpleTask("name", "title");
        manager.getTask(manager.getPrioritizedTasks().get(0).getId());
        Assertions.assertTrue(manager.history().contains(manager.getPrioritizedTasks().get(0)));
    }

    @Test // проверяем, что метод выбрасывает исключение если Айди неверный
    void shouldThrowNPETryingToPrintTaskAndAddItToHistoryIfIdIsWrong() {
        manager.addSimpleTask("name", "title");
        NullPointerException ex = Assertions.assertThrows(NullPointerException.class,
                () -> {manager.getTask("manager.getPrioritizedTasks().get(0).getId()");});
        Assertions.assertEquals(NullPointerException.class, ex.getClass());
    }

    @Test // проверяем, что метод выбрасывает исключение если не добавить задачи
    void shouldThrowIndexOutOfBoundsExceptionTryingToPrintTaskAndAddItToHistoryIfTasksEmpty() {
        IndexOutOfBoundsException ex = Assertions.assertThrows(IndexOutOfBoundsException.class,
                () -> {manager.getTask(manager.getPrioritizedTasks().get(0).getId());});
        Assertions.assertEquals(IndexOutOfBoundsException.class, ex.getClass());
    }

    @Test // проверяем, что в историю добавляется не больше 10 задач и что в ней нет повторений
    void history() {
        for (int i = 0; i < 6; i++) {
            manager.addSimpleTask("name", "title");
            manager.addEpicTask("name", "title");
        }
        for (Task t : manager.getPrioritizedTasks()) {
            if(t.getType() == TaskTypes.EPIC) {
                manager.addSubTask((Epic) t, "name", "title");
            }
        }

        for (Task t : manager.getPrioritizedTasks()) {
            if (t.getType() == TaskTypes.TASK) manager.getTask(t.getId());
            else if (t.getType() == TaskTypes.EPIC) manager.getEpic(t.getId());
            else manager.getTask(t.getId());
        }

        boolean noRepeatedTasks = true;
        for (Task t : manager.history()) {
            int counter = 0;
            for (Task e : manager.history()) {
                if (t.getId().equals(e.getId())) counter++;
            }
            if (counter > 1) noRepeatedTasks = false;
        }

        Assertions.assertTrue(manager.history().size() == 10 && noRepeatedTasks);
    }

    @Test // проверяем, что в историю не добавляются задачи если их не запросить
    void historyIsEmptyIfNotToGetAnyTask() {
        for (int i = 0; i < 6; i++) {
            manager.addSimpleTask("name", "title");
            manager.addEpicTask("name", "title");
        }
        for (Task t : manager.getPrioritizedTasks()) {
            if(t.getType() == TaskTypes.EPIC) {
                manager.addSubTask((Epic) t, "name", "title");
            }
        }

        Assertions.assertTrue(manager.history().isEmpty());
    }

    @Test // проверяем, что в историю не добавляются задачи если их не запросить
    void shouldThrowNPETryingToGetTaskIfIdIsWrong() {
        for (int i = 0; i < 6; i++) {
            manager.addSimpleTask("name", "title");
            manager.addEpicTask("name", "title");
        }
        for (Task t : manager.getPrioritizedTasks()) {
            if(t.getType() == TaskTypes.EPIC) {
                manager.addSubTask((Epic) t, "name", "title");
            }
        }

        NullPointerException ex = Assertions.assertThrows(NullPointerException.class, () -> {
            for (Task t : manager.getPrioritizedTasks()) {
                if (t.getType() == TaskTypes.TASK) manager.getTask(t.getId() + "qwe");
                else if (t.getType() == TaskTypes.EPIC) manager.getEpic(t.getId() + "rty");
                else manager.getTask(t.getId() + "uio");
            }
        });


        Assertions.assertEquals(ex.getClass(), NullPointerException.class);
    }
}