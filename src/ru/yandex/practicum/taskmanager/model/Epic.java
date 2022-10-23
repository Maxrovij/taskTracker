package ru.yandex.practicum.taskmanager.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// класс для Эпиков
public class Epic extends Task {

    // список ID подзадач этого эпика
    public List<String> subTasksId = new ArrayList<>();

    // конструктор задачи
    public Epic(String id,String name, String title) {
        super(id, name,  title);
        type = TaskTypes.EPIC;
    }

    // метод добавления подзадачи к этому эпику
    public SubTask addSubTask(String id, String name, String title){
        String motherTaskId = this.getId();
        SubTask subTask = new SubTask(id, name, title, motherTaskId);
        subTasksId.add(subTask.getId());
        return subTask;
    }

    // переопределенный equals.
    @Override
    public boolean equals(Object obj) {
        boolean isEqual = false;
        if(this == obj) isEqual = true;
        if(obj == null) isEqual = false;
        if(this.getClass() == obj.getClass()) isEqual = true;
        Epic otherEpic = (Epic) obj;
        if(Objects.equals(this.getId(), otherEpic.getId())) isEqual = true;

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




