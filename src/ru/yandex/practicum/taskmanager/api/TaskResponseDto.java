package ru.yandex.practicum.taskmanager.api;

import ru.yandex.practicum.taskmanager.model.Statuses;
import ru.yandex.practicum.taskmanager.model.TaskTypes;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

// Dto каждой отдельной задачи для формирования представления в формате Json
public class TaskResponseDto implements Serializable {
    private final String id;
    private final TaskTypes type;
    private final Statuses status;
    private final String name;
    private final String title;
    private final Long duration; // may be null
    private final Long startTime; // may be null
    private final Long endTime; // may be null
    private final String motherTaskId; // may be null
    private final List<String> subTasksId; // may be null

    public TaskResponseDto(
            String name,
            String title,
            String id,
            Statuses status,
            TaskTypes type,
            Duration duration,
            Instant startTime,
            Instant endTime,
            String motherTaskId,
            List<String> subTasksId) {
        this.name = name;
        this.title = title;
        this.id = id;
        this.status = status;
        this.type = type;
        if(duration != null)  this.duration = duration.toMillis();
        else this.duration = null;
        if(startTime != null) this.startTime = startTime.toEpochMilli();
        else this.startTime = null;
        if(endTime != null) this.endTime = endTime.toEpochMilli();
        else this.endTime = null;
        this.motherTaskId = motherTaskId;
        this.subTasksId = subTasksId;
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public String getId() {
        return id;
    }

    public Statuses getStatus() {
        return status;
    }

    public TaskTypes getType() {
        return type;
    }

    public Long getDuration() {
        return duration;
    }

    public Long getStartTime() {
        return startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public String getMotherTaskId() {
        return motherTaskId;
    }

    public List<String> getSubTasksId() {
        return subTasksId;
    }
}
