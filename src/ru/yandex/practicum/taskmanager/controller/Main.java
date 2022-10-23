package ru.yandex.practicum.taskmanager.controller;

import ru.yandex.practicum.taskmanager.api.HTTPTaskManager;
import ru.yandex.practicum.taskmanager.api.HttpTaskServer;
import ru.yandex.practicum.taskmanager.api.KVServer;

import java.io.IOException;

public class Main {

    public static void main (String[] args) throws IOException, InterruptedException {
        new KVServer().start();
        HttpTaskServer server = new HttpTaskServer(new HTTPTaskManager("http://localhost:8078"));
        server.run();
    }
}
