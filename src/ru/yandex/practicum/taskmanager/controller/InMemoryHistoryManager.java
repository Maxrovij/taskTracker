package ru.yandex.practicum.taskmanager.controller;

import ru.yandex.practicum.taskmanager.model.Node;
import ru.yandex.practicum.taskmanager.model.Task;

import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {

    private LinkedList<Task> lastSeen = new LinkedList<>();

    @Override
    public void add(Task task) {
        lastSeen.linkLast(task);
        if (!lastSeen.idMap.containsKey(task.getId())) {
            lastSeen.idMap.put(task.getId(), lastSeen.getLast());
        }else {
            lastSeen.removeNode(lastSeen.idMap.get(task.getId()));
            lastSeen.idMap.put(task.getId(), lastSeen.getLast());
        }
        if (lastSeen.size == 11) {
            lastSeen.removeFirst();
        }
    }

    @Override
    public void remove(String id) {
        if(lastSeen.idMap.containsKey(id)) {
            lastSeen.removeNode(lastSeen.idMap.get(id));
            lastSeen.idMap.remove(id);
        }
    }

    @Override
    public List<Task> getHistory() {
        return lastSeen.getTasks();
    }

    private static class LinkedList<E> {

        private Node head;
        private Node tail;
        private int size = 0;
        private Map<String, Node> idMap = new HashMap<>();

        // метод добавления задачи в конец списка
        private void linkLast (Task task) {
            final Node oldTail = tail;
            final Node newNode = new Node(oldTail, task, null);
            this.tail = newNode;

            if(oldTail == null){
                this.head = newNode;
            }else {
                oldTail.setNext(newNode);
            }
            size++;
        }

        private void removeNode(Node node) {
            if (node.getPrev() == null) {
                if (node.getNext() == null) {
                    this.head = null;
                    this.tail = null;
                    node.setData(null);
                }else {
                    node.getNext().setPrev(null);
                    this.head = node.getNext();
                    node.setNext(null);
                    node.setData(null);
                }
            }else {
                if (node.getNext() == null) {
                    node.getPrev().setNext(null);
                    node.setPrev(null);
                    node.setData(null);
                }else {
                    node.getPrev().setNext(node.getNext());
                    node.getNext().setPrev(node.getPrev());
                    node.setPrev(null);
                    node.setNext(null);
                    node.setData(null);
                }
            }
            size--;
        }

        private Node getLast() {
            if(this.tail == null) throw new NoSuchElementException();
            return this.tail;
        }

        private void removeFirst() {
            removeNode(this.head);
        }

        private List<Task> getTasks() {
            List<Task> recentTasks = new ArrayList<>();
            Node current = this.head;
            while (current != null) {
                recentTasks.add(current.getData());
                current = current.getNext();
            }
            return recentTasks;
        }
    }
}

