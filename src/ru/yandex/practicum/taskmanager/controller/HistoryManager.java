package ru.yandex.practicum.taskmanager.controller;

import ru.yandex.practicum.taskmanager.model.Task;

import java.util.List;

public interface HistoryManager {
    void add(Task task);
    void remove(String  id);
    List<Task> getHistory();
}
