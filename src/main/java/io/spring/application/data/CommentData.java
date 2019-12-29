package io.spring.application.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentData {
    private String id;
    private String body;
    @JsonIgnore
    private String articleId;
    private Instant createdAt;
    private Instant updatedAt;
    @JsonProperty("author")
    private ProfileData profileData;
}
