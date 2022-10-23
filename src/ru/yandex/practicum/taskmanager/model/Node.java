package ru.yandex.practicum.taskmanager.model;

public class Node {

    private Task data;
    private Node prev;
    private Node next;

    public Node(Node prev, Task data, Node next) {
        this.data = data;
        this.prev = prev;
        this.next = next;
    }

    public Task getData() {
        return data;
    }

    public Node getPrev() {
        return prev;
    }

    public Node getNext() {
        return next;
    }

    public void setPrev(Node prev) {
        this.prev = prev;
    }

    public void setNext(Node next) {
        this.next = next;
    }

    public void setData(Task data) {
        this.data = data;
    }
}

