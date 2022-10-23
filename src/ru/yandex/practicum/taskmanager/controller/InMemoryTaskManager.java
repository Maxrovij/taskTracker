package ru.yandex.practicum.taskmanager.controller;

import ru.yandex.practicum.taskmanager.model.*;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {

    // Мапа для хранения задач разного типа
    public HashMap<String, Task> tasks;
    // Объект для работы с историей просмотров
    public HistoryManager historyManager;
    // Объект для сортировки задач по startTime
    public Set<Task> sortedByStartTimeTasks;

    // конструктор нового менеджера
    public InMemoryTaskManager () {
        tasks = new HashMap<>();
        historyManager = new InMemoryHistoryManager();
        sortedByStartTimeTasks = new TreeSet<>((o1, o2) -> {
            if (o1.getId().equals(o2.getId()))
                return 0;
            if (o1.getStartTime().isEmpty() && o2.getStartTime().isEmpty()) {
                if (o1.getClass() == Epic.class && o2.getClass() != Epic.class)
                    return -1;
                else if (o2.getClass() == Epic.class && o1.getClass() != Epic.class)
                    return 1;
                else
                    return o1.getId().compareTo(o2.getId());
            } else if (o1.getStartTime().isPresent() && o2.getStartTime().isEmpty()) {
                return -1;
            } else if (o1.getStartTime().isEmpty() && o2.getStartTime().isPresent()) {
                return 1;
            } else {
                if(o1.getStartTime().equals(o2.getStartTime())) {
                    if (o1.getClass() == Epic.class && o2.getClass() != Epic.class)
                        return -1;
                    else if (o2.getClass() == Epic.class && o1.getClass() != Epic.class)
                        return 1;
                }
                return o1.getStartTime().get().compareTo(o2.getStartTime().get());
            }
        });
    }

//  --------------------------------------------- служебные методы -----------------------------------------------------

    // метод генерации уникального ID
    private String generateID() {
        String id = UUID.randomUUID().toString();
        while (tasks.containsKey(id)){
            id = UUID.randomUUID().toString();
        }
        return id;
    }

    // метод определения доступности периода времени
    private boolean isPeriodFree(Task task, Optional<Instant> end) {
        boolean isFree = true;
        for (Task t : getPrioritizedTasks()) {
            if(!t.getId().equals(task.getId()) && t.getType() != TaskTypes.EPIC) {
                if(t.getStartTime().isPresent()) {
                    if (t.getEndTime().isPresent()) {
                        if (task.getStartTime().get().isAfter(t.getStartTime().get())
                                && task.getStartTime().get().isBefore(t.getEndTime().get())
                                || task.getStartTime().get().equals(t.getStartTime().get())) {
                            isFree = false;
                        }else {
                            if (end.isPresent()) {
                                if (end.get().isAfter(t.getStartTime().get())
                                        && end.get().isBefore(t.getEndTime().get())
                                        || end.get().equals(t.getEndTime().get()))
                                    isFree = false;
                                if (task.getStartTime().get().isBefore(t.getStartTime().get()) && end.get().isAfter(t.getEndTime().get()))
                                    isFree = false;
                            }
                        }
                    }else {
                        if (t.getStartTime().get().equals(task.getStartTime().get())) isFree = false;
                        if (end.isPresent()) {
                            if (t.getStartTime().get().isAfter(task.getStartTime().get()) && t.getStartTime().get().isBefore(end.get()))
                                isFree = false;
                        }
                    }
                }
            }

        }
        return isFree;
    }

//  ---------------------------------------- методы для работы с задачами ----------------------------------------------

    // метод получения задач в порядке приоритета по времени начала за О(n)
    public List<Task> getPrioritizedTasks() {
        List<Task> prioritizedTaskList = new LinkedList<>();
        for (Task task : sortedByStartTimeTasks) prioritizedTaskList.add(task);
        return prioritizedTaskList;
    }

    // метод установки начала задачи
    public boolean setTaskStartTime(String id, Instant startTime) {
        Task task = tasks.get(id);

        if(task.getType() == TaskTypes.EPIC) {
            System.out.println("Дата начала Эпика - это дата начала его первой задачи! \n Не хулигань!");
            return false;
        }else {
            Task tempTask = new Task(id, "name", "title");
            tempTask.setStartTime(startTime);
            if(task.getDuration().isPresent()) tempTask.setDuration(task.getDuration().get());

            if(isPeriodFree(tempTask, tempTask.getEndTime())) {
                if(task.getType() == TaskTypes.TASK) {
                    sortedByStartTimeTasks.remove(task);
                    task.setStartTime(startTime);
                    tasks.put(task.getId(), task);
                    sortedByStartTimeTasks.add(task);

                    System.out.println("Время начала задачи установлено.");
                }else {
                    SubTask subTask = (SubTask) task;
                    String epicId = subTask.getMotherTaskId();
                    sortedByStartTimeTasks.remove(subTask);
                    subTask.setStartTime(startTime);
                    tasks.put(subTask.getId(), subTask);
                    sortedByStartTimeTasks.add(subTask);

                    Epic epic = (Epic) tasks.get(epicId);

                    Instant minSubStartTime = startTime;
                    for (String subId : epic.subTasksId) {
                        Optional<Instant> start = tasks.get(subId).getStartTime();
                        if(start.isPresent()) {
                            if(start.get().isBefore(minSubStartTime)) {
                                minSubStartTime = start.get();
                            }
                        }
                    }

                    sortedByStartTimeTasks.remove(epic);
                    epic.setStartTime(minSubStartTime);
                    tasks.put(epicId, epic);
                    sortedByStartTimeTasks.add(epic);

                    System.out.println("Дата начала сабтаска установлена. Эпик обновлен.");
                }
            }else {
                System.out.println("Это время занято другой задачей!");
                return false;
            }
        }
        return true;
    }

    // метод установки продолжительности задачи
    public boolean setTaskDuration (String id, Duration dur) {
        Task task = tasks.get(id);

        if (task.getType() == TaskTypes.EPIC) {
            System.out.println("Продолжительность Эпика - это сумма продолжительности его сабтасок! \n Не хулигань!");
            return false;
        }else {
            Optional<Instant> mayBeEndTime = Optional.empty();
            if(task.getStartTime().isPresent())
                mayBeEndTime = Optional.of(task.getStartTime().get().plus(dur));
            if(task.getStartTime().isPresent() && !isPeriodFree(task, mayBeEndTime)){
                System.out.println("Это время занято другой задачей!");
                return false;
            } else {
                if (task.getType() == TaskTypes.TASK) {
                    sortedByStartTimeTasks.remove(task);
                    task.setDuration(dur);
                    tasks.put(task.getId(), task);
                    sortedByStartTimeTasks.add(task);
                    return true;
                }else if (task.getType() == TaskTypes.SUBTASK){
                    sortedByStartTimeTasks.remove(task);
                    task.setDuration(dur);
                    tasks.put(task.getId(), task);
                    sortedByStartTimeTasks.add(task);

                    SubTask subTask = (SubTask) task;
                    Epic epicTask = (Epic) tasks.get(subTask.getMotherTaskId());
                    sortedByStartTimeTasks.remove(epicTask);

                    Duration epicDuration = Duration.ofMillis(0);
                    for (String subTaskId : epicTask.subTasksId) {
                        if(tasks.get(subTaskId).getDuration().isPresent())
                        epicDuration = epicDuration.plusMillis(tasks.get(subTaskId).getDuration().get().toMillis());
                    }
                    epicTask.setDuration(epicDuration);
                    tasks.put(epicTask.getId(), epicTask);
                    sortedByStartTimeTasks.add(epicTask);
                    System.out.println("Продолжительность подзадачи установлена. Эпик обновлен.");
                }
            }
            return true;
        }
    }

    // метод для добавления простой задачи
    public Task addSimpleTask(String name, String title){
        Task simpleTask = new Task(generateID(), name, title);
        tasks.put(simpleTask.getId(), simpleTask);
        sortedByStartTimeTasks.add(simpleTask);
        return simpleTask;
    }

    // метод для добавления Эпика
    public Epic addEpicTask(String name, String title) {
        Epic epicTask = new Epic(generateID(), name, title);
        tasks.put(epicTask.getId(), epicTask);
        sortedByStartTimeTasks.add(epicTask);
        return epicTask;
    }

    // метод для добавления подзадачи к эпику
    public SubTask addSubTask(Epic epicTask, String name, String title){
        SubTask subTask = epicTask.addSubTask(generateID(), name, title);
        tasks.put(subTask.getId(), subTask);
        sortedByStartTimeTasks.add(subTask);
        return subTask;
    }

    // метод для обновления задач по ID
    public boolean updateTask(String id, String name, String title, Statuses status) throws UnsupportedOperationException {
        Task taskToUpdate = tasks.get(id);
        sortedByStartTimeTasks.remove(taskToUpdate);

        taskToUpdate.setName(name);
        taskToUpdate.setTitle(title);

        if (taskToUpdate.getType() != TaskTypes.EPIC) taskToUpdate.setStatus(status);

        if (taskToUpdate.getType() == TaskTypes.SUBTASK) {
            SubTask subTaskToUpdate = (SubTask) taskToUpdate;
            Epic obj = (Epic) tasks.get(subTaskToUpdate.getMotherTaskId());
            sortedByStartTimeTasks.remove(obj);
            ArrayList<Enum> subStatuses = new ArrayList<>();
            for (String subId : obj.subTasksId){
                SubTask task = (SubTask) tasks.get(subId);
                subStatuses.add(task.getStatus());
            }
            if(!subStatuses.contains(Statuses.IN_PROGRESS)
                    && !subStatuses.contains(Statuses.NEW)){
                obj.setStatus(Statuses.DONE);
            }else if(!subStatuses.contains(Statuses.IN_PROGRESS)
                    && !subStatuses.contains(Statuses.DONE)){
                obj.setStatus(Statuses.NEW);
            }else {
                obj.setStatus(Statuses.IN_PROGRESS);
            }
            tasks.put(obj.getId(), obj);
            sortedByStartTimeTasks.add(obj);
        }

        sortedByStartTimeTasks.add(taskToUpdate);
        return true;
    }

    // метод для удаления задач по id
    public boolean deleteTask(String id){
        if (tasks.get(id).getType() == TaskTypes.EPIC){
            Epic epicTask = (Epic) tasks.get(id);
            sortedByStartTimeTasks.remove(epicTask);
            for (String subTaskId : epicTask.subTasksId) {
                sortedByStartTimeTasks.remove(tasks.get(subTaskId));
                tasks.remove(subTaskId);
                historyManager.remove(subTaskId);
            }
            tasks.remove(id);
            historyManager.remove(id);
            return true;
        }else if(tasks.get(id).getType() == TaskTypes.SUBTASK){
            SubTask subTask = (SubTask) tasks.get(id);
            Epic epicTask = (Epic) tasks.get(subTask.getMotherTaskId());
            epicTask.subTasksId.remove(id);
            tasks.put(epicTask.getId(), epicTask);
            sortedByStartTimeTasks.remove(subTask);
            tasks.remove(id);
            historyManager.remove(id);
            return true;
        }else {
            sortedByStartTimeTasks.remove(tasks.get(id));
            tasks.remove(id);
            historyManager.remove(id);
            return true;
        }
    }

    // реализация метода получения Эпика с записью в историю
    public Epic getEpic(String id) {
        Epic e = (Epic) tasks.get(id);
        historyManager.add(e);
        return e;
    }

    // реализация метода для получения сабтаска с записью в историю
    public SubTask getSubtask(String id) {
        SubTask s;
        if(tasks.containsKey(id) && tasks.get(id).getType() == TaskTypes.SUBTASK) {
            s = (SubTask) tasks.get(id);
            historyManager.add(s);
        }else {
            s = null;
        }
        return  s;
    }

    // реализация метода для получения простой задачи с записью в историю
    public Task getTask(String id) {
        Task t = tasks.get(id);
        historyManager.add(t);
        return t;
    }

    // реализация метода для вывода истории просмотров
    public List<Task> history() {
        return historyManager.getHistory();
    }
}


