// ArrayQueue.java
// Copyright (c) 2011 Dmitry V. Korotkov
// All rights reserved.
// This software is released under the BSD license.
// Please see the accompanying LICENSE.txt for details.package net.sourceforge.javaocr.ocrPlugins.imgShearer;
package net.sourceforge.javaocr.ocrPlugins.receiptFinder;

import java.util.*;

/**
 * Array-backer Queue implementation
 *
 * @author Dmitry Korotkov
 */
@SuppressWarnings({"unchecked"})
public class ArrayQueue<E> implements Queue<E> {
    private Object[] data;
    private int head;
    private int tail;
    private int maxCapacity = -1;

    public ArrayQueue() {
        init(100, 0);
    }

    public ArrayQueue(int capacity, int maxCapacity) {
        init(capacity, maxCapacity);
    }

    private void init(int capacity, int maxCapacity) {
        data = new Object[capacity];
        this.maxCapacity = maxCapacity;
    }

    public boolean add(E e) {
        if (!offer(e)) {
            throw new IllegalStateException("Capacity exceeded");
        }
        return true;
    }

    public boolean offer(E e) {
        if (tail == data.length || tail + 1 == head) {
            // Expand
            if (data.length == maxCapacity) {
                return false;
            }
            int newLen = data.length * 2;
            if (maxCapacity > 0 && newLen > maxCapacity) {
                newLen = maxCapacity;
            }
            Object newData[] = new Object[newLen];
            System.arraycopy(data, head, newData, 0, Math.min(tail, data.length) - head);
            if (head > tail) {
                System.arraycopy(data, 0, newData, data.length - head, tail);
            }
            tail = size();
            head = 0;
            data = newData;
        }
        data[tail++] = e;
        return true;
    }

    public E remove() {
        if (head == tail) {
            throw new NoSuchElementException();
        }
        E result = (E) data[head++];
        if (head == data.length && head > tail) {
            head = 0;
        }
        return result;
    }

    public E poll() {
        if (head == tail) {
            return null;
        }
        E result = (E) data[head++];
        if (head == data.length) {
            head = 0;
        }
        return result;
    }

    public E element() {
        if (head == tail) {
            throw new NoSuchElementException();
        }
        return (E) data[head];
    }

    public E peek() {
        if (head == tail) {
            return null;
        }
        return (E) data[head];
    }

    public int size() {
        return head <= tail ? tail - head : data.length - head + tail;
    }

    public boolean isEmpty() {
        return head == tail;
    }

    public boolean contains(Object o) {
        throw new UnsupportedOperationException("Implement me");
    }

    public Iterator<E> iterator() {
        throw new UnsupportedOperationException("Implement me");
    }

    public Object[] toArray() {
        throw new UnsupportedOperationException("Implement me");
    }

    public <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException("Implement me");
    }

    public boolean remove(Object o) {
        throw new UnsupportedOperationException("Implement me");
    }

    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException("Implement me");
    }

    public boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException("Implement me");
    }

    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("Implement me");
    }

    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("Implement me");
    }

    public void clear() {
        head = 0;
        tail = 0;
    }
}
