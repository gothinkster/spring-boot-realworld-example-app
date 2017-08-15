package io.spring.infrastructure.article;

import io.spring.core.article.Article;
import io.spring.core.article.Tag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

@Component
@Mapper
public interface ArticleMapper {
    void insert(@Param("article") Article article);

    Article findById(@Param("id") String id);

    boolean findTag(@Param("tagName") String tagName);

    void insertTag(@Param("tag") Tag tag);

    void insertArticleTagRelation(@Param("articleId") String articleId, @Param("tagId") String tagId);
}
