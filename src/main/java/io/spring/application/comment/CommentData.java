package io.spring.application.comment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.spring.application.profile.ProfileData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentData {
    private String id;
    private String body;
    @JsonIgnore
    private String articleId;
    private DateTime createdAt;
    private DateTime updatedAt;
    @JsonProperty("author")
    private ProfileData profileData;
}
