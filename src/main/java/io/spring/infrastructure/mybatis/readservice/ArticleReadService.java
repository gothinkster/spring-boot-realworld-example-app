package io.spring.infrastructure.mybatis.readservice;

import io.spring.application.Page;
import io.spring.application.data.ArticleData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ArticleReadService {
    ArticleData findById(@Param("id") String id);

    ArticleData findBySlug(@Param("slug") String slug);

    List<String> queryArticles(@Param("tag") String tag, @Param("author") String author, @Param("favoritedBy") String favoritedBy, @Param("page") Page page);

    int countArticle(@Param("tag") String tag, @Param("author") String author, @Param("favoritedBy") String favoritedBy);

    List<ArticleData> findArticles(@Param("articleIds") List<String> articleIds);

    List<ArticleData> findArticlesOfAuthors(@Param("authors") List<String> authors, @Param("page") Page page);

    int countFeedSize(@Param("authors") List<String> authors);
}
