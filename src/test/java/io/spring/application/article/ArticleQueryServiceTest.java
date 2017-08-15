package io.spring.application.article;

import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.article.MyBatisArticleRepository;
import io.spring.infrastructure.user.MyBatisUserRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@MybatisTest
@Import({ArticleQueryService.class, MyBatisUserRepository.class, MyBatisArticleRepository.class})
public class ArticleQueryServiceTest {
    @Autowired
    private ArticleQueryService queryService;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void should_fetch_article_success() throws Exception {
        User user = new User("aisensiy@gmail.com", "aisensiy", "123", "", "");
        userRepository.save(user);

        Article article = new Article("test", "test", "desc", "body", new String[]{"java", "spring"}, user.getId());
        articleRepository.save(article);

        Optional<ArticleData> optional = queryService.findById(article.getId(), user);
        assertThat(optional.isPresent(), is(true));
        ArticleData fetched = optional.get();
        assertThat(fetched.getFavoritesCount(), is(0));
        assertThat(fetched.isFavorited(), is(false));
        assertThat(fetched.getCreatedAt(), notNullValue());
        assertThat(fetched.getUpdatedAt(), notNullValue());
    }
}