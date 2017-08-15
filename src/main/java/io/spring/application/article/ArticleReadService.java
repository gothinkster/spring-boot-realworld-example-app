package io.spring.application.article;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

@Component
@Mapper
public interface ArticleReadService {
    ArticleData findById(@Param("id") String id);

    ArticleData findBySlug(@Param("slug") String slug);
}
