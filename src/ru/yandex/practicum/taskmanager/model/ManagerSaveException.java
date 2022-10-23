package ru.yandex.practicum.taskmanager.model;

import java.io.IOException;

// класс собственного исколючения
public class ManagerSaveException extends IOException {
    public ManagerSaveException(String message) {
        super(message);
    }
}
