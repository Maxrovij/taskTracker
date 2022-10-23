package ru.yandex.practicum.taskmanager.controller;

import ru.yandex.practicum.taskmanager.model.Epic;
import ru.yandex.practicum.taskmanager.model.Statuses;
import ru.yandex.practicum.taskmanager.model.SubTask;
import ru.yandex.practicum.taskmanager.model.Task;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public interface TaskManager {

    // метод получения списка задач в порядке приоритета по времени начала
    List<Task> getPrioritizedTasks();

    // метод установки времени начала задачи
    boolean setTaskStartTime(String id, Instant startTime);

    // метод установки продолжительности задачи
    boolean setTaskDuration(String id, Duration dur);

    // метод для добавления простой задачи
    Task addSimpleTask(String name, String title);

    // метод для добавления Эпика
    Epic addEpicTask(String name, String title);

    // метод для добавления подзадачи к эпику
    SubTask addSubTask(Epic epicTask, String name, String title);

    // метод для обновления задач по ID
    boolean updateTask(String id, String name, String title, Statuses status);

    // метод для удаления одной задач по id
    boolean deleteTask(String id);

    // метод для вывода информации об эпике и записи в историю просмотра
    Epic getEpic(String id);

    // метод для вывода информации о сабтаске и записи в историю просмотра
    SubTask getSubtask(String id);

    // метод для вывода информации об обычной задаче и записи в историю просмотра
    Task getTask(String id);

    // метод вывода истории просмотров(последние 10 просмотренных задач)
    List<Task> history();
}
