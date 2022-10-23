package ru.yandex.practicum.taskmanager.api;

import ru.yandex.practicum.taskmanager.model.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

// Dto для отправления списка задач в формате Json
public class TasksResponseDto {
    public List<TaskResponseDto> tasks;

    public TasksResponseDto(List<Task> tasks) {
        if(tasks.size() != 0) this.tasks = tasks.stream().map(TasksResponseDto::apply).toList();
        else this.tasks = null;
    }

    public static TaskResponseDto apply(Task task) {

        String name = task.getName();
        String title = task.getTitle();
        String id = task.getId();
        Statuses status = task.getStatus();
        TaskTypes type = task.getType();
        Duration duration = null;
        Instant startTime = null;
        Instant endTime = null;
        String motherTaskId = null;
        List<String> subTasksId = null;

        if (task.getDuration().isPresent()) duration = task.getDuration().get();
        if (task.getStartTime().isPresent()) startTime = task.getStartTime().get();
        if (task.getEndTime().isPresent()) endTime = task.getEndTime().get();
        if (task.getType() == TaskTypes.EPIC) {
            Epic e = (Epic) task;
            if (e.subTasksId.size() != 0) subTasksId = e.subTasksId;
        }
        if (task.getType() == TaskTypes.SUBTASK) {
            SubTask s = (SubTask) task;
            motherTaskId = s.getMotherTaskId();
        }

        return new TaskResponseDto(
                name,
                title,
                id,
                status,
                type,
                duration,
                startTime,
                endTime,
                motherTaskId,
                subTasksId);
    }
}
