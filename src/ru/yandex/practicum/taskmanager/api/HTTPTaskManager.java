package ru.yandex.practicum.taskmanager.api;

import com.google.gson.*;
import ru.yandex.practicum.taskmanager.controller.FileBackedTasksManager;
import ru.yandex.practicum.taskmanager.model.Epic;
import ru.yandex.practicum.taskmanager.model.Statuses;
import ru.yandex.practicum.taskmanager.model.SubTask;
import ru.yandex.practicum.taskmanager.model.Task;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class HTTPTaskManager extends FileBackedTasksManager {

    KVTaskClient client;
    Gson gson;

    // инициализируем клиент при создании экземпляра менеджера
    public HTTPTaskManager(String serverUrl) {
        super(serverUrl);
        gson = new Gson();
    }

    @Override // переопределенный метод от FileBackedTaskManager, чтобы состояние хранилось не в файле, а на сервере
    protected void createBackup(String serverUrl) {
        client = new KVTaskClient(serverUrl);
        load(serverUrl);
    }

    @Override // загрузка состояния трекера задач с сервера при запуске
    protected void load(String serverUrl) {
        try {
            String tasks = client.load("allTasks");
            JsonArray array = JsonParser
                    .parseString(tasks)
                    .getAsJsonObject()
                    .get("tasks")
                    .getAsJsonArray();

            for(JsonElement t : array) {
                JsonObject o = t.getAsJsonObject();
                if(o.get("type").getAsString().equals("EPIC")) {
                    Epic epic = new Epic(
                            o.get("id").getAsString(),
                            o.get("name").getAsString(),
                            o.get("title").getAsString());
                    epic.setStatus(Statuses.valueOf(o.get("status").getAsString()));

                    if(!o.get("subTasksId").isJsonNull()) {
                        List<String> ids = new ArrayList<>();
                        for(JsonElement s : o.get("subTasksId").getAsJsonArray()) {
                            ids.add(s.getAsString());
                        }
                        epic.subTasksId =ids;
                    }

                    this.tasks.put(epic.getId(), epic);
                    sortedByStartTimeTasks.add(epic);
                }
            }

            for (JsonElement t : array) {
                JsonObject o = t.getAsJsonObject();
                switch (o.get("type").getAsString()) {
                    case "SUBTASK":
                        SubTask subTask = new SubTask(
                                o.get("id").getAsString(),
                                o.get("name").getAsString(),
                                o.get("title").getAsString(),
                                o.get("motherTaskId").getAsString());
                        subTask.setStatus(Statuses.valueOf(o.get("status").getAsString()));
                        if(!o.get("startTime").isJsonNull())
                        setTaskStartTime(subTask.getId(), Instant.ofEpochMilli(o.get("startTime").getAsLong()));
                        if(!o.get("startTime").isJsonNull())
                        setTaskDuration(subTask.getId(), Duration.ofMillis(o.get("duration").getAsLong()));
                        this.tasks.put(subTask.getId(), subTask);
                        sortedByStartTimeTasks.add(subTask);
                        break;
                    default:
                        Task task = new Task(
                                o.get("id").getAsString(),
                                o.get("name").getAsString(),
                                o.get("title").getAsString());
                        task.setStatus(Statuses.valueOf(o.get("status").getAsString()));
                        if(!o.get("startTime").isJsonNull())
                        setTaskStartTime(task.getId(), Instant.ofEpochMilli(o.get("startTime").getAsLong()));
                        if(!o.get("startTime").isJsonNull())
                        setTaskDuration(task.getId(), Duration.ofMillis(o.get("duration").getAsLong()));
                        this.tasks.put(task.getId(), task);
                        sortedByStartTimeTasks.add(task);
                }
            }

            String history = client.load("history");
            JsonArray historyArray = JsonParser
                    .parseString(history)
                    .getAsJsonObject()
                    .getAsJsonArray();
            for(JsonElement id : historyArray) {
                historyManager.add(this.tasks.get(id.getAsString()));
            }

        }catch (IOException | InterruptedException exception) {
            System.out.println(exception.getMessage());
        }
    }

    @Override // сохранение состояния трекера задач после каждой операции
    protected void save() {

        String jsonAllTasksDto = gson.toJson(new TasksResponseDto(new LinkedList<>(sortedByStartTimeTasks)));
        client.put("allTasks", jsonAllTasksDto);

        if(historyManager.getHistory().size() != 0) {
            List<String> historyIds = new LinkedList<>();
            for (Task t : historyManager.getHistory()) {
                historyIds.add(t.getId());
            }
            String jsonHistory = gson.toJson(historyIds);
            client.put("history", jsonHistory);
        }

    }
}