package ru.yandex.practicum.taskmanager.controller;

public class Managers {

    // методы возвращающие объект нужного для работы с приложением класса
    public static TaskManager getDefault(String backUpFile) {return new FileBackedTasksManager(backUpFile);}
}
