package io.spring.application;

import java.util.List;
import lombok.Getter;

@Getter
public class CursorPager<T extends Node> {
  private List<T> data;
  private boolean next;
  private boolean previous;

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

  public PageCursor getStartCursor() {
    return data.isEmpty() ? null : data.get(0).getCursor();
  }

  public PageCursor getEndCursor() {
    return data.isEmpty() ? null : data.get(data.size() - 1).getCursor();
  }

  public enum Direction {
    PREV,
    NEXT
  }
}
