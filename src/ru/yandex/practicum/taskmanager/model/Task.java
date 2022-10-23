package ru.yandex.practicum.taskmanager.model;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

// класс для простой задачи
public class Task {
    private String name;
    private String title;
    private final String id;
    private Statuses status;
    protected TaskTypes type;
    private Duration duration;
    private Instant startTime;
    private Instant endTime;

    // конструктор задачи
    public Task(String id, String name, String title){
        this.id = id;
        setName(name);
        setTitle(title);
        setStatus(Statuses.NEW);
        type = TaskTypes.TASK;
    }

//  ------------------------------------------------- сеттеры ----------------------------------------------------------

    // продолжительность задачи
    public void setDuration(Duration duration) {
        this.duration = duration;
        calculateEndTime();
    }

    // время начала задачи
    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
        calculateEndTime();
    }

    // вычислитель времени окончания задачи
    private void calculateEndTime() {
        if(duration != null && startTime != null) {
            endTime = startTime.plus(duration);
        }
    }

    // статус задачи
    public void setStatus(Statuses status) {
        this.status = status;
    }

    // описание задачи
    public void setTitle(String title) {
        this.title = title;
    }

    // название задачи
    public void setName(String name) {
        this.name = name;
    }

//  ------------------------------------------------- геттеры ----------------------------------------------------------

    // продолжительность задачи
    public Optional<Duration> getDuration() {
        return Optional.ofNullable(duration);
    }

    // время начала задачи
    public Optional<Instant> getStartTime() {
        return Optional.ofNullable(startTime);
    }

    // время окончания задачи
    public Optional<Instant> getEndTime() {
        return Optional.ofNullable(endTime);
    }

    // тип задачи
    public TaskTypes getType() {
        return type;
    }

    // статус задачи
    public Statuses getStatus() {
        return status;
    }

    // описание задачи
    public String getTitle() {
        return title;
    }

    // ID задачи
    public String getId() {
        return id;
    }

    // название задачи
    public String getName() {
        return name;
    }

    // переопределенный equals
    @Override
    public boolean equals(Object obj) {
        boolean isEqual = false;
        if(this == obj) isEqual = true;
        if(obj == null) isEqual = false;
        if(this.getClass() == obj.getClass()) isEqual = true;
        Task otherSubTask = (Task) obj;
        if(Objects.equals(this.getId(), otherSubTask.getId())) isEqual = true;

        return isEqual;
    }

    // переопределенный HashCode
    @Override
    public int hashCode() {
        int result = Objects.hash(getId(), getName());
        result = 31 * result + Objects.hash(getTitle());
        return result;
    }
}
