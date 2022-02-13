package io.spring.application.tag;

import io.spring.application.TagsQueryService;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.infrastructure.DbTestBase;
import io.spring.infrastructure.repository.MyBatisArticleRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.junit.Assert.assertTrue;

@Import({TagsQueryService.class, MyBatisArticleRepository.class})
public class TagsQueryServiceTest extends DbTestBase {

    @Autowired
    private TagsQueryService tagsQueryService;

    @Autowired
    private ArticleRepository articleRepository;

    @Test
    public void should_get_all_tags() {
        articleRepository.save(new Article("test", "test", "test", List.of("java"), "123"));
        assertTrue(tagsQueryService.allTags().contains("java"));
    }

}
