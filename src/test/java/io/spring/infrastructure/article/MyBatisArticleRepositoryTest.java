package io.spring.infrastructure.article;

import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.article.Tag;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.user.MyBatisUserRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

@MybatisTest
@RunWith(SpringRunner.class)
@Import({MyBatisArticleRepository.class, MyBatisUserRepository.class})
public class MyBatisArticleRepositoryTest {
    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private UserRepository userRepository;
    private User user;


    @Before
    public void setUp() throws Exception {
        user = new User("aisensiy@gmail.com", "aisensiy", "123", "bio", "default");
        userRepository.save(user);
    }

    @Test
    public void should_create_and_fetch_article_success() throws Exception {
        Article article = new Article("test", "test", "desc", "body", new String[]{"java", "spring"}, user.getId());
        articleRepository.save(article);
        Optional<Article> optional = articleRepository.findById(article.getId());
        assertThat(optional.isPresent(), is(true));
        assertThat(optional.get(), is(article));
        assertThat(optional.get().getTags().contains(new Tag("java")), is(true));
        assertThat(optional.get().getTags().contains(new Tag("spring")), is(true));
    }
}