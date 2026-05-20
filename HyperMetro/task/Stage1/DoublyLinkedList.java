package metro;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

public class DoublyLinkedList<T> implements Iterable<T> {

    private Node<T> head;
    private Node<T> tail;
    private int size;

    private static class Node<T> {
        T data;
        Node<T> prev;
        Node<T> next;

        Node(T data) {
            this.data = data;
        }
    }

    private class DLLIterator implements Iterator<T> {

        Node<T> current = head;

        @Override
        public boolean hasNext() {
            return current != null;
        }

        @Override
        public T next() {
            if (current == null) {
                throw new NoSuchElementException("No such element!");
            }
            T data = current.data;
            current = current.next;
            return data;
        }

    }

    @Override
    public Iterator<T> iterator() {
        return new DLLIterator();
    }

    public void addFirst(T data) {

        Node<T> newNode = new Node<>(data);

        if (head == null) {
            head = newNode;
            tail = newNode;
        } else {
            newNode.next = head;
            head.prev = newNode;
            head = newNode;
        }

        size++;

    }

    public void addLast(T data) {

        Node<T> newNode = new Node<>(data);

        if (tail == null) {
            head = newNode;
        } else {
            newNode.prev = tail;
            tail.next = newNode;
        }
        tail = newNode;

        size++;

    }

    public T removeFirst() {

        if (head == null) {
            return null;
        }

        T removedData = head.data;

        if (size == 1) {
            head = null;
            tail = null;
        } else {
            head = head.next;
            head.prev = null;
        }

        size--;

        return removedData;

    }

    public T removeLast() {

        if (tail == null) {
            return null;
        }

        T removedData = tail.data;

        if (size == 1) {
            head = null;
            tail = null;
        } else {
            tail = tail.prev;
            tail.next = null;
        }

        size--;

        return removedData;

    }

    public void addAt(int index, T data) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index out of range!");
        } else if (index == 0) {
            addFirst(data);
        } else if (index == size) {
            addLast(data);
        } else {
            Node<T> current = head;

            for (int i = 0; i < index; i++) {
                current = current.next;
            }

            Node<T> newNode = new Node<>(data);

            newNode.prev = current.prev;
            newNode.next = current;
            current.prev.next = newNode;
            current.prev = newNode;
            size++;
        }
    }

    public T removeAt(int index) {

        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index out of range!");
        } else if (index == 0) {
            return removeFirst();
        } else if (index == size - 1) {
            return removeLast();
        } else {
            Node<T> current = head;

            for (int i = 0; i < index; i++) {
                current = current.next;
            }

            T removedData = current.data;

            current.prev.next = current.next;
            current.next.prev = current.prev;
            current.prev = null;
            current.next = null;

            size--;

            return removedData;

        }
    }

    public boolean remove(T value) {

        if (head == null) {
            return false;
        }

        Node<T> current = head;

        while (current != null) {
            if (Objects.equals(value, current.data)) {
                if (current == head) {
                    removeFirst();
                    return true;

                } else if (current == tail) {
                    removeLast();
                    return true;

                } else {
                    current.prev.next = current.next;
                    current.next.prev = current.prev;
                    current.prev = null;
                    current.next = null;

                    size--;

                    return true;

                }
            }

            current = current.next;

        }
        return false;
    }

    public void reverse() {

        if (size < 2) {
            return;
        }

        Node<T> current = head;
        Node<T> temp;

        while (current != null) {
            temp = current.prev;
            current.prev = current.next;
            current.next = temp;
            current = current.prev;
        }

        temp = head;
        head = tail;
        tail = temp;

    }

    public T get(int index) {

        Node<T> current;

        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index out of range!");
        }

        if (index < size / 2) {
            current = head;

            for (int i = 0; i < index; i++) {
                current = current.next;
            }

        } else {
            current = tail;

            for (int i = size - 1; i > index; i--) {
                current = current.prev;
            }

        }

        return current.data;

    }

    public boolean contains(T data) {

        for (T element : this) {

            if (Objects.equals(data, element)) {
                return true;
            }

        }

        return false;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[");

        boolean first = true;

        for (T e : this) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(e);
            first = false;
        }

        sb.append("]");
        return sb.toString();
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public T peekFirst() {

        if (head == null) {
            return null;
        }
        return head.data;
    }

    public T peekLast() {

        if (tail == null) {
            return null;
        }
        return tail.data;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (!(o instanceof DoublyLinkedList<?> other)) {
            return false;
        }

        if (this.size != other.size) {
            return false;
        }

        Iterator<T> thisIterator = this.iterator();
        Iterator<?> otherIterator = other.iterator();

        while (thisIterator.hasNext()) {
            if (!Objects.equals(thisIterator.next(), otherIterator.next())) {
                return false;
            }
        }
        return true;

    }

    @Override
    public int hashCode() {

        int hash = 1;

        for (T element : this) {
            int hashElement = (element == null) ? 0 : element.hashCode();
            hash = 31 * hash + hashElement;
        }

        return hash;

    }

}
