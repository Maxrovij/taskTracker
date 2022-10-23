package ru.yandex.practicum.taskmanager.model;

import java.util.Objects;

// класс для подзадач
public class SubTask extends Task{

    // ID эпика, к которому привязана задача
    private final String motherTaskId;

    // конструктор задачи
    public SubTask(String id, String name, String title, String motherTaskId) {
        super(id, name, title);
        this.motherTaskId = motherTaskId;
        type = TaskTypes.SUBTASK;
    }

    // геттер эпика, к которому привязана задача
    public String getMotherTaskId() {
        return motherTaskId;
    }

    // переопределенный equals
    @Override
    public boolean equals(Object obj) {
        boolean isEqual = false;
        if(this == obj) isEqual = true;
        if(obj == null) isEqual = false;
        if(this.getClass() == obj.getClass()) isEqual = true;
        SubTask otherSubTask = (SubTask) obj;
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
