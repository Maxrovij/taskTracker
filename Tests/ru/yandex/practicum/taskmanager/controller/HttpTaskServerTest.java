package ru.yandex.practicum.taskmanager.controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import ru.yandex.practicum.taskmanager.api.HttpTaskServer;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpTaskServerTest {
        private TaskManager manager = new InMemoryTaskManager();
        private HttpTaskServer server = new HttpTaskServer(manager);
        private HttpClient client = HttpClient.newHttpClient();
        private String url = "http://localhost:8080/tasks";

    public HttpTaskServerTest() throws IOException {
    }

    @Test
    public void shouldReturnEmptyListIfNoTasksWereAdded() throws IOException, InterruptedException {
        server.run();
        HttpRequest request = HttpRequest
                .newBuilder()
                .GET()
                .uri(URI.create(url))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Assertions.assertTrue(response.statusCode() == 200);
        JsonElement element = JsonParser.parseString(response.body());
        Assertions.assertEquals("{" + "\"tasks\":null" + "}", element.toString());
        server.stop();
    }

    @Test
    public void shouldAddTasksAndReturnJsonWithTasks()
            throws IOException, InterruptedException {
        server.run();

        // добавляем простую задачу, проверяя эндпоинт "/tasks/task" с методом POST
        String jsonSimple = "{\"name\":\"Simple Test Name\",\"title\":\"Simple Test Title\"}";
        HttpRequest requestToAddSimpleTask = HttpRequest
                .newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(jsonSimple))
                .uri(URI.create(url + "/task"))
                .build();
        HttpResponse<String> response1 = client.send(requestToAddSimpleTask, HttpResponse.BodyHandlers.ofString());
        Assertions.assertTrue(response1.statusCode() == 200);

        // добавляем EPIC задачу, проверяя эндпоинт "/tasks/epic" с методом POST
        String jsonEpic = "{\"name\":\"Epic Test Name\",\"title\":\"Epic Test Title\"}";
        HttpRequest requestToAddEpicTask = HttpRequest
                .newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(jsonEpic))
                .uri(URI.create(url + "/epic"))
                .build();
        HttpResponse<String> response2 = client.send(requestToAddEpicTask, HttpResponse.BodyHandlers.ofString());
        Assertions.assertTrue(response2.statusCode() == 200);

        // получаем список добавленных задач и получаем id Эпика, проверяя эндпоинт /tasks с методом GET
        HttpRequest requestToGetAddedTasks = HttpRequest
                .newBuilder()
                .GET()
                .uri(URI.create(url))
                .build();
        HttpResponse<String> responseToGetAddedTAsks = client.send(
                requestToGetAddedTasks,
                HttpResponse.BodyHandlers.ofString());
        JsonElement elementToGetAddedTasks = JsonParser.parseString(responseToGetAddedTAsks.body());
        JsonArray arrayToGetEpicId = elementToGetAddedTasks.getAsJsonObject().getAsJsonArray("tasks");
        JsonObject obj = arrayToGetEpicId.get(0).getAsJsonObject();
        String epicId = obj.get("id").getAsString();

        // добавляем SUBTASK задачу, проверяя эндпоинт "/tasks/subtask" с методом POST
        String jsonSub = "{\"name\":\"Sub Test Name\",\"title\":\"Sub Test Title\"}";
        HttpRequest requestToAddSubTask = HttpRequest
                .newBuilder()
                .uri(URI.create(url + "/subtask/?id=" + epicId))
                .POST(HttpRequest.BodyPublishers.ofString(jsonSub))
                .build();
        HttpResponse<String> response3 = client.send(requestToAddSubTask, HttpResponse.BodyHandlers.ofString());
        Assertions.assertTrue(response3.statusCode() == 200);

        server.stop();
    }

    @Test
    public void shouldGetTasksByIDAndAddToHistoryReturnHistory()
            throws IOException, InterruptedException {
        server.run();

        // добавляем простую задачу, проверяя эндпоинт "/tasks/task" с методом POST
        String jsonSimple = "{\"name\":\"Simple Test Name\",\"title\":\"Simple Test Title\"}";
        HttpRequest requestToAddSimpleTask = HttpRequest
                .newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(jsonSimple))
                .uri(URI.create(url + "/task"))
                .build();
        HttpResponse<String> response1 = client.send(requestToAddSimpleTask, HttpResponse.BodyHandlers.ofString());
        Assertions.assertTrue(response1.statusCode() == 200);

        // добавляем EPIC задачу, проверяя эндпоинт "/tasks/epic" с методом POST
        String jsonEpic = "{\"name\":\"Epic Test Name\",\"title\":\"Epic Test Title\"}";
        HttpRequest requestToAddEpicTask = HttpRequest
                .newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(jsonEpic))
                .uri(URI.create(url + "/epic"))
                .build();
        HttpResponse<String> response2 = client.send(requestToAddEpicTask, HttpResponse.BodyHandlers.ofString());
        Assertions.assertTrue(response2.statusCode() == 200);

        // получаем список добавленных задач и получаем id Эпика, проверяя эндпоинт /tasks с методом GET
        HttpRequest requestToGetAddedTasks = HttpRequest
                .newBuilder()
                .GET()
                .uri(URI.create(url))
                .build();
        HttpResponse<String> responseToGetAddedTAsks = client.send(
                requestToGetAddedTasks,
                HttpResponse.BodyHandlers.ofString());
        JsonElement elementToGetAddedTasks = JsonParser.parseString(responseToGetAddedTAsks.body());
        JsonArray arrayToGetEpicId = elementToGetAddedTasks.getAsJsonObject().getAsJsonArray("tasks");
        JsonObject obj = arrayToGetEpicId.get(0).getAsJsonObject();
        String epicId = obj.get("id").getAsString();

        // добавляем SUBTASK задачу, проверяя эндпоинт "/tasks/subtask" с методом POST
        String jsonSub = "{\"name\":\"Sub Test Name\",\"title\":\"Sub Test Title\"}";
        HttpRequest requestToAddSubTask = HttpRequest
                .newBuilder()
                .uri(URI.create(url + "/subtask/?id=" + epicId))
                .POST(HttpRequest.BodyPublishers.ofString(jsonSub))
                .build();
        HttpResponse<String> response3 = client.send(requestToAddSubTask, HttpResponse.BodyHandlers.ofString());
        Assertions.assertTrue(response3.statusCode() == 200);

        // получаем ID всех добавленных задач
        HttpResponse<String> responseToGetAllThreeAddedTasks = client.send(
                requestToGetAddedTasks,
                HttpResponse.BodyHandlers.ofString());
        JsonElement elementToGetAllAddedTasks = JsonParser.parseString(responseToGetAllThreeAddedTasks.body());
        JsonArray arrayToGetAllIds = elementToGetAllAddedTasks.getAsJsonObject().getAsJsonArray("tasks");

        String epicIdToGetEpic = arrayToGetAllIds.get(0).getAsJsonObject().get("id").getAsString();
        String simpleId = null;
        String subId = null;
        for (int i = 1; i < arrayToGetAllIds.size(); i++) {
            JsonElement o = arrayToGetAllIds.get(i);
            if (o.getAsJsonObject().get("type").getAsString().equals("TASK")) {
                simpleId = o.getAsJsonObject().get("id").getAsString();
            }else {
                subId = o.getAsJsonObject().get("id").getAsString();
            }
        }

        // запрашиваем задачи, чтобы добавить их в историю
        HttpRequest simpleRequest = HttpRequest
                .newBuilder()
                .uri(URI.create(url + "/task/?id=" + simpleId))
                .GET()
                .build();
        HttpResponse<String> simpleResp = client.send(simpleRequest, HttpResponse.BodyHandlers.ofString());
        Assertions.assertTrue(simpleResp.statusCode() == 200);

        HttpRequest epicRequest = HttpRequest
                .newBuilder()
                .uri(URI.create(url + "/epic/?id=" + epicIdToGetEpic))
                .GET()
                .build();
        HttpResponse<String> epicResp = client.send(epicRequest, HttpResponse.BodyHandlers.ofString());
        Assertions.assertTrue(epicResp.statusCode() == 200);

        HttpRequest subRequest = HttpRequest
                .newBuilder()
                .GET()
                .uri(URI.create(url + "/subtask/?id=" + subId))
                .build();

        HttpResponse<String> subResp = client.send(subRequest, HttpResponse.BodyHandlers.ofString());
        Assertions.assertTrue(subResp.statusCode() == 200);

        // запрашиваем историю и смотрим, что там 3 задачи, проверяя эндпоинт "/tasks/history"
        HttpRequest historyRequest = HttpRequest.newBuilder(URI.create(url + "/history")).GET().build();
        HttpResponse<String> historyResp = client.send(historyRequest, HttpResponse.BodyHandlers.ofString());
        Assertions.assertTrue(historyResp.statusCode() == 200);
        JsonElement el = JsonParser.parseString(historyResp.body());
        Assertions.assertTrue(el.getAsJsonObject().getAsJsonArray("tasks").size() == 3);
        server.stop();
    }

    @Test
    public void shouldPatchTasksAndDeleteThem() throws IOException, InterruptedException {
        server.run();

        // добавляем простую задачу, проверяя эндпоинт "/tasks/task" с методом POST
        String jsonSimple = "{\"name\":\"Simple Test Name\",\"title\":\"Simple Test Title\"}";
        HttpRequest requestToAddSimpleTask = HttpRequest
                .newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(jsonSimple))
                .uri(URI.create(url + "/task"))
                .build();
        HttpResponse<String> response1 = client.send(requestToAddSimpleTask, HttpResponse.BodyHandlers.ofString());
        Assertions.assertTrue(response1.statusCode() == 200);

        // добавляем EPIC задачу, проверяя эндпоинт "/tasks/epic" с методом POST
        String jsonEpic = "{\"name\":\"Epic Test Name\",\"title\":\"Epic Test Title\"}";
        HttpRequest requestToAddEpicTask = HttpRequest
                .newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(jsonEpic))
                .uri(URI.create(url + "/epic"))
                .build();
        HttpResponse<String> response2 = client.send(requestToAddEpicTask, HttpResponse.BodyHandlers.ofString());
        Assertions.assertTrue(response2.statusCode() == 200);

        // получаем список добавленных задач и получаем id Эпика, проверяя эндпоинт /tasks с методом GET
        HttpRequest requestToGetAddedTasks = HttpRequest
                .newBuilder()
                .GET()
                .uri(URI.create(url))
                .build();
        HttpResponse<String> responseToGetAddedTAsks = client.send(
                requestToGetAddedTasks,
                HttpResponse.BodyHandlers.ofString());
        JsonElement elementToGetAddedTasks = JsonParser.parseString(responseToGetAddedTAsks.body());
        JsonArray arrayToGetEpicId = elementToGetAddedTasks.getAsJsonObject().getAsJsonArray("tasks");
        JsonObject obj = arrayToGetEpicId.get(0).getAsJsonObject();
        String epicId = obj.get("id").getAsString();

        // добавляем SUBTASK задачу, проверяя эндпоинт "/tasks/subtask" с методом POST
        String jsonSub = "{\"name\":\"Sub Test Name\",\"title\":\"Sub Test Title\"}";
        HttpRequest requestToAddSubTask = HttpRequest
                .newBuilder()
                .uri(URI.create(url + "/subtask/?id=" + epicId))
                .POST(HttpRequest.BodyPublishers.ofString(jsonSub))
                .build();
        HttpResponse<String> response3 = client.send(requestToAddSubTask, HttpResponse.BodyHandlers.ofString());
        Assertions.assertTrue(response3.statusCode() == 200);

        // получаем ID всех добавленных задач
        HttpResponse<String> responseToGetAllThreeAddedTasks = client.send(
                requestToGetAddedTasks,
                HttpResponse.BodyHandlers.ofString());
        JsonElement elementToGetAllAddedTasks = JsonParser.parseString(responseToGetAllThreeAddedTasks.body());
        JsonArray arrayToGetAllIds = elementToGetAllAddedTasks.getAsJsonObject().getAsJsonArray("tasks");

        String epicIdToGetEpic = arrayToGetAllIds.get(0).getAsJsonObject().get("id").getAsString();
        String simpleId = null;
        String subId = null;
        for (int i = 1; i < arrayToGetAllIds.size(); i++) {
            JsonElement o = arrayToGetAllIds.get(i);
            if (o.getAsJsonObject().get("type").getAsString().equals("TASK")) {
                simpleId = o.getAsJsonObject().get("id").getAsString();
            }else {
                subId = o.getAsJsonObject().get("id").getAsString();
            }
        }

        // редактируем задачи, проверяя эндпоинт /PATCH
        String jsonPATCHSimple = "{\"name\":\"Simple PATCH Name\",\"title\":\"Simple PATCH Title\"}";
        HttpRequest requestToPATCHSimpleTask = HttpRequest
                .newBuilder()
                .uri(URI.create(url + "/task/?id=" + simpleId))
                .method("PATCH", HttpRequest.BodyPublishers.ofString(jsonPATCHSimple))
                .build();
        HttpResponse<String> responsePatchSimple = client.send(requestToPATCHSimpleTask, HttpResponse.BodyHandlers.ofString());
        Assertions.assertTrue(responsePatchSimple.statusCode() == 200);

        String jsonPATCHSub = "{\"name\":\"Sub PATCH Name\",\"title\":\"Sub PATCH Title\"}";
        HttpRequest requestToPATCHSub = HttpRequest
                .newBuilder()
                .uri(URI.create(url + "/subtask/?id=" + subId))
                .method("PATCH", HttpRequest.BodyPublishers.ofString(jsonPATCHSub))
                .build();
        HttpResponse<String> responsePatchSub = client.send(requestToPATCHSub, HttpResponse.BodyHandlers.ofString());
        Assertions.assertTrue(responsePatchSub.statusCode() == 200);

        String jsonPATCHEpic = "{\"name\":\"Epic PATCH Name\",\"title\":\"Epic PATCH Title\"}";
        HttpRequest requestToPATCHEpic = HttpRequest
                .newBuilder()
                .uri(URI.create(url + "/epic/?id=" + epicIdToGetEpic))
                .method("PATCH", HttpRequest.BodyPublishers.ofString(jsonPATCHEpic))
                .build();
        HttpResponse<String> responsePatchEpic = client.send(requestToPATCHEpic, HttpResponse.BodyHandlers.ofString());
        Assertions.assertTrue(responsePatchEpic.statusCode() == 200);

        // удаляем задачи
        HttpRequest requestToDeleteSimple = HttpRequest
                .newBuilder()
                .DELETE()
                .uri(URI.create(url + "/task/?id=" + simpleId)).build();
        HttpResponse<String> respToDeleteSimple = client.send(
                requestToDeleteSimple,
                HttpResponse.BodyHandlers.ofString()
        );
        Assertions.assertTrue(respToDeleteSimple.statusCode() == 200);

        HttpRequest requestToDeleteSub = HttpRequest
                .newBuilder()
                .DELETE()
                .uri(URI.create(url + "/subtask/?id=" + subId)).build();
        HttpResponse<String> respToDeleteSub = client.send(
                requestToDeleteSub,
                HttpResponse.BodyHandlers.ofString()
        );
        Assertions.assertTrue(respToDeleteSub.statusCode() == 200);

        HttpRequest requestToDeleteEpic = HttpRequest
                .newBuilder()
                .DELETE()
                .uri(URI.create(url + "/epic/?id=" + epicIdToGetEpic)).build();
        HttpResponse<String> respToDeleteEpic = client.send(
                requestToDeleteEpic,
                HttpResponse.BodyHandlers.ofString()
        );
        Assertions.assertTrue(respToDeleteEpic.statusCode() == 200);

        // проверяем, что список задач пуст
        HttpResponse<String> deletedResp = client.send(requestToGetAddedTasks, HttpResponse.BodyHandlers.ofString());
        JsonElement deleted = JsonParser.parseString(deletedResp.body());
        Assertions.assertTrue(deleted.getAsJsonObject().get("tasks").isJsonNull());
        server.stop();
    }
}