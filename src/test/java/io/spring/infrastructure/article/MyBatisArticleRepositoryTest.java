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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

@MybatisTest
@RunWith(SpringRunner.class)
@Import({
        MyBatisArticleRepository.class,
        MyBatisUserRepository.class
})
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
        assertThat(optional.isPresent(), is(true));
        assertThat(optional.get(), is(article));
        assertThat(optional.get().getTags().contains(new Tag("java")), is(true));
        assertThat(optional.get().getTags().contains(new Tag("spring")), is(true));
    }

    @Test
    public void should_update_and_fetch_article_success() {
        articleRepository.save(article);

        String newTitle = "new test 2";
        article.update(newTitle, "", "");
        articleRepository.save(article);
        System.out.println(article.getSlug());
        Optional<Article> optional = articleRepository.findBySlug(article.getSlug());
        assertThat(optional.isPresent(), is(true));
        Article fetched = optional.get();
        assertThat(fetched.getTitle(), is(newTitle));
        assertThat(fetched.getBody(), not(""));
    }

    @Test
    public void should_delete_article() throws Exception {
        articleRepository.save(article);

        articleRepository.remove(article);
        assertThat(articleRepository.findById(article.getId()).isPresent(), is(false));
    }
}
