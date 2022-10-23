package ru.yandex.practicum.taskmanager.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FileBackedTasksManagerTest extends TaskManagerTest<FileBackedTasksManager>{
    private final String backUpFile = "backUpFile.csv";

    public FileBackedTasksManagerTest() {

    }

    @BeforeEach
    public void createNewManager() {
        this.manager = new FileBackedTasksManager(backUpFile);
    }

    @AfterEach
    public void deleteFile() {
        try {
            Files.delete(Path.of(backUpFile));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    @Test // проверяем, что метод сохраняет и загружет в файл Эпик без под задач, обычную задачу и исторю просмотров
    void shouldSaveTasksToBacUpFileAndLoadFromFile() throws IOException {
        manager.addSimpleTask("name", "title");
        manager.addEpicTask("name", "title");
        manager.getEpic(manager.getPrioritizedTasks().get(0).getId());
        manager.getTask(manager.getPrioritizedTasks().get(1).getId());

        BufferedReader br = new BufferedReader(new FileReader(backUpFile));
        List<String> csvContents = new ArrayList<>();
        while (br.ready()) {
            csvContents.add(br.readLine());
        }
        br.close();
        String[] line1Content = csvContents.get(1).split(",");
        String[] line2Content = csvContents.get(2).split(",");

        Assertions.assertEquals(
                manager.getPrioritizedTasks().get(0).getId() + ","
                        + manager.getPrioritizedTasks().get(1).getId(),
                csvContents.get(4));

        Assertions.assertTrue(line1Content[1].equals("TASK") && line2Content[1].equals("EPIC")
                || line1Content[1].equals("EPIC") && line2Content[1].equals("TASK"));

        FileBackedTasksManager newManager = new FileBackedTasksManager(backUpFile);
        Assertions.assertEquals(manager.getPrioritizedTasks().get(0), newManager.getPrioritizedTasks().get(0));
        Assertions.assertEquals(manager.getPrioritizedTasks().get(1), newManager.getPrioritizedTasks().get(1));
    }

    @Test // проверка сохранения и загрузки из файла при пустом списке задач
    void  shouldSaveAndLoadIfTasksListIsEmpty() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(backUpFile));
        List<String> csvContents = new ArrayList<>();
        while (br.ready()) {
            csvContents.add(br.readLine());
        }
        br.close();
        Assertions.assertEquals(1, csvContents.size());
        Assertions.assertEquals("id,type,name,status,description,epic,startTime,duration", csvContents.get(0));

        FileBackedTasksManager newManager = new FileBackedTasksManager(backUpFile);
        Assertions.assertEquals(0, newManager.tasks.size());
    }

    @Test // проверка сохранения и загрузки из файла при пустой истории
    void shouldSaveAndLoadIfHistoryListIsEmpty() throws IOException {
        manager.addSimpleTask("name", "title");
        manager.addEpicTask("name", "title");
        BufferedReader br = new BufferedReader(new FileReader(backUpFile));
        List<String> csvContents = new ArrayList<>();
        while (br.ready()) {
            csvContents.add(br.readLine());
        }
        br.close();

        String[] line1Content = csvContents.get(1).split(",");
        String[] line2Content = csvContents.get(2).split(",");

        Assertions.assertTrue(line1Content[1].equals("TASK") && line2Content[1].equals("EPIC")
                || line1Content[1].equals("EPIC") && line2Content[1].equals("TASK"));
    }
}