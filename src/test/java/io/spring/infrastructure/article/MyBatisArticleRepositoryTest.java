package io.spring.infrastructure.article;

import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.article.Tag;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.repository.MyBatisArticleRepository;
import io.spring.infrastructure.repository.MyBatisUserRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

@MybatisTest
@RunWith(SpringRunner.class)
@Import({MyBatisArticleRepository.class, MyBatisUserRepository.class})
public class MyBatisArticleRepositoryTest {
    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private UserRepository userRepository;

    private Article article;


    @Before
    public void setUp() {
        User user = new User("aisensiy@gmail.com", "aisensiy", "123", "bio", "default");
        userRepository.save(user);
        article = new Article("test", "desc", "body", new String[]{"java", "spring"}, user.getId());
    }

    @Test
    public void should_create_and_fetch_article_success() {
        articleRepository.save(article);
        Optional<Article> optional = articleRepository.findById(article.getId());
        assertTrue(optional.isPresent());
        assertEquals(optional.get(), article);
        assertTrue(optional.get().getTags().contains(new Tag("java")));
        assertTrue(optional.get().getTags().contains(new Tag("spring")));
    }

    @Test
    public void should_update_and_fetch_article_success() {
        articleRepository.save(article);

        String newTitle = "new test 2";
        article.update(newTitle, "", "");
        articleRepository.save(article);
        System.out.println(article.getSlug());
        Optional<Article> optional = articleRepository.findBySlug(article.getSlug());
        assertTrue(optional.isPresent());
        Article fetched = optional.get();
        assertEquals(fetched.getTitle(), newTitle);
        assertNotEquals(fetched.getBody(), "");
    }

    @Test
    public void should_delete_article() {
        articleRepository.save(article);

        articleRepository.remove(article);
        assertFalse(articleRepository.findById(article.getId()).isPresent());
    }
}