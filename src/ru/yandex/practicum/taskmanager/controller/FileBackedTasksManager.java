package ru.yandex.practicum.taskmanager.controller;

import ru.yandex.practicum.taskmanager.model.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// класс для работы с бэкап файлом
public class FileBackedTasksManager extends InMemoryTaskManager {

    private final String backUp;

    // конструктор нового менеджера
    public FileBackedTasksManager (String backUp) {
        this.backUp = backUp;
        try {
            createBackup(backUp);
        }catch (IOException e) {
            try {
                load(backUp);
            } catch (IOException ex) {
                System.out.println(e.getMessage());
            }
        }
    }

//  ------------------------------------------- служебные методы -------------------------------------------------------

    // метод создания бэкап файла
    protected void createBackup(String backUp) throws IOException {
            Path backUpFile = Files.createFile(Path.of(backUp));
            Writer fileWriter = new FileWriter(backUpFile.getFileName().toString());
            fileWriter.write("id,type,name,status,description,epic,startTime,duration");
            fileWriter.flush();
            fileWriter.close();
            System.out.println("BackUp File Created!");
    }

    // метод загрузки содержимого из файла
    protected void load(String backUp) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(backUp));

        List<String> csvContents = new ArrayList<>();
        while (br.ready()) {
            csvContents.add(br.readLine());
        }
        br.close();

        if (csvContents.size() == 1) {
            System.out.println("Нет задач для заполения!");
        } else if (csvContents.get(csvContents.size() - 2).isEmpty()) {
            for (int i = 1; i < csvContents.size() - 2; i++) {
                Task task = fromString(csvContents.get(i));
                tasks.put(task.getId(), task);
                sortedByStartTimeTasks.add(task);
            }
            String historyIDs = csvContents.get(csvContents.size() -1);

            List<String> historyIDsList = historyFromString(historyIDs);
            for (String id : historyIDsList) {
                Task task = tasks.get(id);
                historyManager.add(task);
            }
        } else {
            for (int i = 1; i < csvContents.size(); i++) {
                Task task = fromString(csvContents.get(i));
                tasks.put(task.getId(), task);
                sortedByStartTimeTasks.add(task);
            }
            System.out.println("История просмотров отсутствует!");
        }
    }

    // метод конвертирования задачи в строку
    private String toString(Task task) {
        String stringTask;
        String maybeStart = "null";
        String maybeDuration = "null";
        if(task.getStartTime().isPresent()) maybeStart = String.valueOf(task.getStartTime().get());
        if(task.getDuration().isPresent()) maybeDuration = String.valueOf(task.getDuration().get());

        if(task.getType() == TaskTypes.SUBTASK) {
            SubTask subTask = (SubTask) task;
            stringTask = String.join(",",
                    subTask.getId(),
                    subTask.getType().toString(),
                    subTask.getName(),
                    subTask.getStatus().toString(),
                    subTask.getTitle(),
                    subTask.getMotherTaskId(),
                    maybeStart,
                    maybeDuration);
        }else {
            stringTask = String.join(",",
                    task.getId(),
                    task.getType().toString(),
                    task.getName(),
                    task.getStatus().toString(),
                    task.getTitle(),
                    "null",
                    maybeStart,
                    maybeDuration);
        }
        return stringTask;
    }

    // метод получения задачи из строки
    private Task fromString(String value) {
        Task task;
        String[] lineContents = value.split(",");
        switch (lineContents[1]) {
            case ("SUBTASK") :

                SubTask subTask = new SubTask(lineContents[0], lineContents[2], lineContents[4], lineContents[5]);

                if (lineContents[3].equals("IN_PROGRESS")) {
                    subTask.setStatus(Statuses.IN_PROGRESS);
                }else if (lineContents[3].equals("DONE")){
                    subTask.setStatus(Statuses.DONE);
                }
                if (!lineContents[6].equals("null")) {
                    subTask.setStartTime(Instant.parse(lineContents[6]));
                }
                if(!lineContents[7].equals("null")) {
                    subTask.setDuration(Duration.parse(lineContents[7]));
                }

                task = subTask;
                break;

            case ("EPIC") :
                Epic epicTask = new Epic(lineContents[0], lineContents[2], lineContents[4]);
                if (lineContents[3].equals("IN_PROGRESS")) {
                    epicTask.setStatus(Statuses.IN_PROGRESS);
                }else if (lineContents[3].equals("DONE")){
                    epicTask.setStatus(Statuses.DONE);
                }
                if (!lineContents[6].equals("null")) {
                    epicTask.setStartTime(Instant.parse(lineContents[6]));
                }
                if(!lineContents[7].equals("null")) {
                    epicTask.setDuration(Duration.parse(lineContents[7]));
                }
                task = epicTask;
                break;
            default:
                Task simpleTask = new Task(lineContents[0], lineContents[2], lineContents[4]);
                if (lineContents[3].equals("IN_PROGRESS")) {
                    simpleTask.setStatus(Statuses.IN_PROGRESS);
                }else if (lineContents[3].equals("DONE")){
                    simpleTask.setStatus(Statuses.DONE);
                }
                if (!lineContents[6].equals("null")) {
                    simpleTask.setStartTime(Instant.parse(lineContents[6]));
                }
                if(!lineContents[7].equals("null")) {
                    simpleTask.setDuration(Duration.parse(lineContents[7]));
                }

                task = simpleTask;
        }
        return task;
    }

    // етод получения списка айдишников из csv файла
    private List<String> historyFromString(String value) {
        List<String> historyIds = new ArrayList<>();
        String[] lineContents = value.split(",");
        historyIds.addAll(Arrays.asList(lineContents));
        return historyIds;
    }

    // метод получения строки айдишников просмотренных задач
    private String toString(HistoryManager manager) {
        StringBuilder historyString = new StringBuilder();
        List<Task> history = manager.getHistory();
        for (Task task : history) {
            String str = task.getId() + ",";
            historyString.append(str);
        }
        historyString.deleteCharAt(historyString.length() - 1);

        return historyString.toString();
    }

    // метод сохранения текущего состояния менеджера в бэкап файл
    protected void save() {
        try {
            Writer fileWriter = new FileWriter(backUp);
            fileWriter.write("id,type,name,status,description,epic,startTime,duration" + "\n");
            if(tasks == null) {
                throw new ManagerSaveException("Ошибка при сохранении данных");
            }else {
                for (Task task : tasks.values()) {
                    String toBackUpFile = toString(task) + "\n";
                    fileWriter.append(toBackUpFile);
                }
                if (historyManager.getHistory().size() != 0) {
                    fileWriter.append("\n");
                    fileWriter.append(toString(historyManager));
                }
                fileWriter.flush();
                fileWriter.close();
            }
        } catch (ManagerSaveException e) {
            // Собственное исключение
            System.out.println(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//  --------------------------------------- методы для работы с задачами -----------------------------------------------

    // измененный метод установки начала задачи
    @Override
    public boolean setTaskStartTime(String id, Instant startTime) {
        boolean isSetStartTime = super.setTaskStartTime(id,startTime);
        save();
        return isSetStartTime;
    }

    // измененный метод установки продолжительности задачи
    @Override
    public boolean setTaskDuration (String id, Duration dur) {
        boolean isSetDuration = super.setTaskDuration(id, dur);
        save();
        return isSetDuration;
    }

    // измененный метод добавления задачи с сохранением в файл
    @Override
    public Task addSimpleTask(String name, String title) {
        Task t = super.addSimpleTask(name, title);
        save();
        return t;
    }

    // измененный метод добавления задачи с сохранением в файл
    @Override
    public Epic addEpicTask(String name, String title) {
        Epic e = super.addEpicTask(name, title);
        save();
        return e;
    }

    // измененный метод добавления задачи с сохранением в файл
    @Override
    public SubTask addSubTask(Epic epic, String name, String title) {
        SubTask s = super.addSubTask(epic, name, title);
        save();
        return s;
    }

    // измененный метод просмотра задачи с сохранением в файл
    @Override
    public boolean updateTask(String id, String name, String title, Statuses status) {
        boolean isUpdated = super.updateTask(id, name, title, status);
        save();
        return isUpdated;
    }

    // измененный метод просмотра задачи с сохранением в файл
    @Override
    public Epic getEpic(String id) {
        Epic e = super.getEpic(id);
        save();
        return e;
    }

    // измененный метод просмотра задачи с сохранением в файл
    @Override
    public SubTask getSubtask(String id) {
        SubTask s = super.getSubtask(id);
        save();
        return s;
    }

    // измененный метод просмотра задачи с сохранением в файл
    @Override
    public Task getTask(String id) {
        Task t = super.getTask(id);
        save();
        return t;
    }

    // измененный метод удаления задачи с сохранением в файл
    @Override
    public boolean deleteTask(String id) {
        boolean isDeleted = super.deleteTask(id);
        save();
        return isDeleted;
    }
}