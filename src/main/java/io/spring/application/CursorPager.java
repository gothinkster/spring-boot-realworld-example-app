package io.spring.application;

import lombok.Getter;

import java.util.List;

@Getter
public class CursorPager<T extends Node> {

    private final List<T> data;
    private final boolean next;
    private final boolean previous;


    public CursorPager(List<T> data, Direction direction, boolean hasExtra) {
        this.data = data;
        if (direction == Direction.NEXT) {
            this.previous = false;
            this.next = hasExtra;
        } else {
            this.next = false;
            this.previous = hasExtra;
        }
    }


    public boolean hasNext() {
        return next;
    }

    public boolean hasPrevious() {
        return previous;
    }

    public PageCursor<T> getStartCursor() {
        return data.isEmpty() ? null : data.get(0).getCursor();
    }

    public PageCursor<T> getEndCursor() {
        return data.isEmpty() ? null : data.get(data.size() - 1).getCursor();
    }


    public enum Direction {
        PREV,
        NEXT
    }

}
