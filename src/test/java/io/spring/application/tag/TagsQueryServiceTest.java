package io.spring.application.tag;

import io.spring.application.TagsQueryService;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.infrastructure.repository.MyBatisArticleRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@MybatisTest
@Import({TagsQueryService.class, MyBatisArticleRepository.class})
public class TagsQueryServiceTest {
    @Autowired
    private TagsQueryService tagsQueryService;

    @Autowired
    private ArticleRepository articleRepository;

    @Test
    public void should_get_all_tags() throws Exception {
        articleRepository.save(new Article("test", "test", "test", new String[]{"java"}, "123"));
        assertThat(tagsQueryService.allTags().contains("java"), is(true));
    }
}