package io.spring.application;

public abstract class PageCursor<T> {
  private T data;

  public PageCursor(T data) {
    this.data = data;
  }

  public T getData() {
    return data;
  }

  @Override
  public String toString() {
    return data.toString();
  }
}
