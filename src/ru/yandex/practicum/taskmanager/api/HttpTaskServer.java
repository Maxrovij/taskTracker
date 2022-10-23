package ru.yandex.practicum.taskmanager.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import ru.yandex.practicum.taskmanager.controller.TaskManager;
import ru.yandex.practicum.taskmanager.model.Epic;
import ru.yandex.practicum.taskmanager.model.Task;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;

public class HttpTaskServer {
    private final HttpServer server;
    private TaskManager manager;
    Gson gson;

    public HttpTaskServer(TaskManager taskManager) throws IOException {
        manager = taskManager;
        server = HttpServer.create(new InetSocketAddress(8080),0);
        gson = new GsonBuilder().serializeNulls().create();

        // эндпоинт для получения списка всех задач
        server.createContext("/tasks", (h) -> {
            try {
                switch (h.getRequestMethod()) {
                    case "GET":
                        String jSonList = gson.toJson(new TasksResponseDto(manager.getPrioritizedTasks()));
                        h.sendResponseHeaders(200,0);
                        h.getResponseHeaders().add("Content-Type", "application-json");
                        try (OutputStream os = h.getResponseBody()) {
                            os.write(jSonList.getBytes(StandardCharsets.UTF_8));
                        }
                        break;
                    default:
                        System.out.println("/tasks ждёт GET-запрос, а получил " + h.getRequestMethod());
                        h.sendResponseHeaders(405, 0);
                }
            }finally {
                h.close();
            }
        });

        // эндпоинт для получения истории просмотров задач
        server.createContext("/tasks/history", (h) -> {
            try {
                switch (h.getRequestMethod()) {
                    case "GET":
                        String jSonHistory = gson.toJson(new TasksResponseDto(manager.history()));
                        h.sendResponseHeaders(200,0);
                        h.getResponseHeaders().add("Content-Type", "application-json");
                        try(OutputStream os = h.getResponseBody()) {
                            os.write(jSonHistory.getBytes(StandardCharsets.UTF_8));
                        }
                        break;
                    default:
                        System.out.println("/tasks/history ждёт GET-запрос, а получил " + h.getRequestMethod());
                        h.sendResponseHeaders(405, 0);
                }
            }finally {
                h.close();
            }
        });

        // эндпоинт для получения, добавления, изменения и удаления Простой задачи
        server.createContext("/tasks/task", (h) -> {
            String uri = h.getRequestURI().toString();
            String id = null;
            if(uri.contains("id")) id = uri.split("id")[1].substring(1);
            String body = new String(h.getRequestBody().readAllBytes());
            try {
                switch (h.getRequestMethod()) {
                    case "GET":
                        try {
                            String jsonTask = gson.toJson(TasksResponseDto.apply(manager.getTask(id)));
                            h.sendResponseHeaders(200,0);
                            h.getResponseHeaders().add("Content-Type", "application-json");
                            try (OutputStream os = h.getResponseBody()) {
                                os.write(jsonTask.getBytes(StandardCharsets.UTF_8));
                            }
                        }catch (NullPointerException e) {
                            h.sendResponseHeaders(400,0);
                        }
                        break;
                    case "POST":
                        try {
                            TaskResponseDto t = gson.fromJson(body, TaskResponseDto.class);
                            manager.addSimpleTask(t.getName(), t.getTitle());
                            h.sendResponseHeaders(200,0);
                        }catch (NullPointerException e) {
                            h.sendResponseHeaders(400,0);
                        }
                        break;
                    case "PATCH":
                        try {
                            TaskResponseDto t = gson.fromJson(body, TaskResponseDto.class);
                            manager.updateTask(id, t.getName(), t.getTitle(), t.getStatus());
                            if (t.getStartTime() != null)
                            manager.setTaskStartTime(id, Instant.ofEpochMilli(t.getStartTime()));
                            if(t.getDuration() != null)
                            manager.setTaskDuration(id, Duration.ofMillis(t.getDuration()));
                            h.sendResponseHeaders(200,0);
                        }catch (Exception e) {
                            h.sendResponseHeaders(400,0);
                        }
                        break;
                    case "DELETE":
                        try {
                            manager.deleteTask(id);
                            h.sendResponseHeaders(200,0);
                        }catch (NullPointerException e) {
                            h.sendResponseHeaders(400,0);
                        }
                        break;
                    default:
                        System.out.println("/tasks/task " + h.getRequestMethod() + "-запрос не может быть обработан.");
                        h.sendResponseHeaders(405, 0);
                }
            }finally {
                h.close();
            }
        });

        // эндпоинт для получения, добавления, изменения и удаления Эпика
        server.createContext("/tasks/epic", (h) -> {
            String uri = h.getRequestURI().toString();
            String id = null;
            if(uri.contains("id")) id = uri.split("id")[1].substring(1);
            String body = new String(h.getRequestBody().readAllBytes());
            try {
                switch (h.getRequestMethod()) {
                    case "GET":
                        try {
                            String jsonTask = gson.toJson(TasksResponseDto.apply(manager.getEpic(id)));
                            h.sendResponseHeaders(200,0);
                            h.getResponseHeaders().add("Content-Type", "application-json");
                            try (OutputStream os = h.getResponseBody()) {
                                os.write(jsonTask.getBytes(StandardCharsets.UTF_8));
                            }
                        }catch (NullPointerException e) {
                            h.sendResponseHeaders(400,0);
                        }
                    break;
                    case "POST":
                        try {
                            TaskResponseDto t = gson.fromJson(body, TaskResponseDto.class);
                            manager.addEpicTask(t.getName(), t.getTitle());
                            h.sendResponseHeaders(200,0);
                        }catch (NullPointerException e) {
                            h.sendResponseHeaders(400,0);
                        }
                    break;
                    case "PATCH":
                        try {
                            TaskResponseDto t = gson.fromJson(body, TaskResponseDto.class);
                            manager.updateTask(id, t.getName(), t.getTitle(), t.getStatus());
                            h.sendResponseHeaders(200,0);
                        }catch (NullPointerException e) {
                            h.sendResponseHeaders(400,0);
                        }
                    break;
                    case "DELETE":
                        try {
                            manager.deleteTask(id);
                            h.sendResponseHeaders(200,0);
                        }catch (NullPointerException e) {
                            h.sendResponseHeaders(400,0);
                        }
                    break;
                    default:
                        System.out.println("/tasks/epic " + h.getRequestMethod() + "-запрос не может быть обработан.");
                        h.sendResponseHeaders(405, 0);
                }
            }finally {
                h.close();
            }
        });

        // эндпоинт для получения, добавления, изменения и удаления Подзадачи
        server.createContext("/tasks/subtask", (h) -> {
            String uri = h.getRequestURI().toString();
            String id = null;
            if(uri.contains("id")) id = uri.split("id")[1].substring(1);
            String body = new String(h.getRequestBody().readAllBytes());
            try {
                switch (h.getRequestMethod()) {
                    case "GET":
                        try {
                            String jsonTask = gson.toJson(TasksResponseDto.apply(manager.getSubtask(id)));
                            h.sendResponseHeaders(200,0);
                            h.getResponseHeaders().add("Content-Type", "application-json");
                            try (OutputStream os = h.getResponseBody()) {
                                os.write(jsonTask.getBytes(StandardCharsets.UTF_8));
                            }
                        }catch (NullPointerException e) {
                            h.sendResponseHeaders(400,0);
                        }
                    break;
                    case "POST":
                        try {
                            TaskResponseDto t = gson.fromJson(body, TaskResponseDto.class);
                            Epic epic = null;
                            for (Task task : manager.getPrioritizedTasks()) {
                                if(task.getId().equals(id)) {
                                    epic = (Epic) task;
                                }
                            }
                            manager.addSubTask(epic, t.getName(), t.getTitle());
                            h.sendResponseHeaders(200,0);
                        }catch (NullPointerException e) {
                            h.sendResponseHeaders(400,0);
                        }
                    break;
                    case "PATCH":
                        try {
                            TaskResponseDto t = gson.fromJson(body, TaskResponseDto.class);
                            manager.updateTask(id, t.getName(), t.getTitle(), t.getStatus());
                            if (t.getStartTime() != null)
                                manager.setTaskStartTime(id, Instant.ofEpochMilli(t.getStartTime()));
                            if(t.getDuration() != null)
                                manager.setTaskDuration(id, Duration.ofMillis(t.getDuration()));
                            h.sendResponseHeaders(200,0);
                        }catch (Exception e) {
                            h.sendResponseHeaders(400,0);
                        }
                    break;
                    case "DELETE":
                        try {
                            manager.deleteTask(id);
                            h.sendResponseHeaders(200,0);
                        }catch (NullPointerException e) {
                            h.sendResponseHeaders(400,0);
                        }
                    break;
                    default:
                        System.out.println("/tasks/subtask " + h.getRequestMethod() + "-запрос не может быть обработан.");
                        h.sendResponseHeaders(405, 0);
                }
            }finally {
                h.close();
            }
        });
    }

    // запуск сервера
    public void run() {
        System.out.println("HttpTaskServer running");
        server.start();
    }

    // остановка сервера
    public void stop() {
        server.stop(0);
    }
}
