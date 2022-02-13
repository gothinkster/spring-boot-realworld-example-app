package io.spring.application.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.spring.application.DateTimeCursor;
import io.spring.application.Node;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleData implements Node {

    private String id;
    private String slug;
    private String title;
    private String description;
    private String body;
    private boolean favorited;
    private int favoritesCount;
    private DateTime createdAt;
    private DateTime updatedAt;
    private List<String> tagList;

    @JsonProperty("author")
    private ProfileData profileData;

    @Override
    public DateTimeCursor getCursor() {
        return new DateTimeCursor(updatedAt);
    }

}
