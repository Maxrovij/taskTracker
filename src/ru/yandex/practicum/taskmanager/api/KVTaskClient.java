package ru.yandex.practicum.taskmanager.api;

import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class KVTaskClient {

    private final HttpClient client;
    private String API_KEY = null;
    String serverUrl;

    // при создании нового клиента регистрируемся на KV сервере
    public KVTaskClient(String serverUrl) {
        client = HttpClient.newHttpClient();
        this.serverUrl = serverUrl;
        URI url = URI.create(serverUrl + "/register");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            API_KEY = JsonParser.parseString(response.body()).getAsString();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // метод добавления/обновления состояния менеджера на сервере
    public void put(String key, String json) {
        URI url = URI.create(serverUrl + "/save/" + key + "/?API_KEY=" + API_KEY);
        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(url)
                .header("Content-Type", "text/plain; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        try {
            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // метод загрузки состояния трекера задач с сервера при запуске
    public String load(String key) throws IOException, InterruptedException { // кеу = либо allTasks, либо history
        URI url = URI.create(serverUrl + "/load/" + key + "/?API_KEY=" + API_KEY);
        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
}
