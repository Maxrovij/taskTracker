package ru.yandex.practicum.taskmanager.controller;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {
    public InMemoryTaskManagerTest() {
        this.manager = new InMemoryTaskManager();
    }
}