package io.spring.core.favorite;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@EqualsAndHashCode
public class ArticleFavorite {
    private String articleId;
    private String userId;

    public ArticleFavorite(String articleId, String userId) {
        this.articleId = articleId;
        this.userId = userId;
    }
}
