package io.spring.application.article;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

@Component
@Mapper
public interface ArticleReadService {
    ArticleData ofId(@Param("id") String id);
}
