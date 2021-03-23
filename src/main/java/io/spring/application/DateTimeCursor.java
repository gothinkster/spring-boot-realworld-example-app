package io.spring.application;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class DateTimeCursor extends PageCursor<DateTime> {

  public DateTimeCursor(DateTime data) {
    super(data);
  }

  @Override
  public String toString() {
    return String.valueOf(getData().getMillis());
  }

  public static DateTime parse(String cursor) {
    if (cursor == null) {
      return null;
    }
    return new DateTime().withMillis(Long.parseLong(cursor)).withZone(DateTimeZone.UTC);
  }
}
