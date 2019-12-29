package io.spring.application.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleData {
    private String id;
    private String slug;
    private String title;
    private String description;
    private String body;
    private boolean favorited;
    private int favoritesCount;
    private Instant createdAt;
    private Instant updatedAt;
    private List<String> tagList;
    @JsonProperty("author")
    private ProfileData profileData;
}

