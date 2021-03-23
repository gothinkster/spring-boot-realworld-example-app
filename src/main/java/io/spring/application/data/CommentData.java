package io.spring.application.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.spring.application.DateTimeCursor;
import io.spring.application.Node;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentData implements Node {
  private String id;
  private String body;
  @JsonIgnore private String articleId;
  private DateTime createdAt;
  private DateTime updatedAt;

  @JsonProperty("author")
  private ProfileData profileData;

  @Override
  public DateTimeCursor getCursor() {
    return new DateTimeCursor(createdAt);
  }
}
