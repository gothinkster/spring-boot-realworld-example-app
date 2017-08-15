package io.spring.core.article;

import java.util.Optional;

public interface ArticleRepository {
    String toSlug(String title);

    void save(Article article);

    Optional<Article> findById(String id);
}
