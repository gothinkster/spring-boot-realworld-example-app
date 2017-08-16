package io.spring.application.article;

import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.favorite.ArticleFavorite;
import io.spring.core.favorite.ArticleFavoriteRepository;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.article.MyBatisArticleRepository;
import io.spring.infrastructure.favorite.MyBatisArticleFavoriteRepository;
import io.spring.infrastructure.user.MyBatisUserRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@MybatisTest
@Import({ArticleQueryService.class, MyBatisUserRepository.class, MyBatisArticleRepository.class, MyBatisArticleFavoriteRepository.class})
public class ArticleQueryServiceTest {
    @Autowired
    private ArticleQueryService queryService;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ArticleFavoriteRepository articleFavoriteRepository;

    private User user;
    private Article article;

    @Before
    public void setUp() throws Exception {
        user = new User("aisensiy@gmail.com", "aisensiy", "123", "", "");
        userRepository.save(user);
        article = new Article("test", "desc", "body", new String[]{"java", "spring"}, user.getId());
        articleRepository.save(article);
    }

    @Test
    public void should_fetch_article_success() throws Exception {

        Optional<ArticleData> optional = queryService.findById(article.getId(), user);
        assertThat(optional.isPresent(), is(true));
        ArticleData fetched = optional.get();
        assertThat(fetched.getFavoritesCount(), is(0));
        assertThat(fetched.isFavorited(), is(false));
        assertThat(fetched.getCreatedAt(), notNullValue());
        assertThat(fetched.getUpdatedAt(), notNullValue());
        assertThat(fetched.getTagList().contains("java"), is(true));
    }

    @Test
    public void should_get_article_with_right_favorite_and_favorite_count() throws Exception {
        User anotherUser = new User("other@test.com", "other", "123", "", "");
        userRepository.save(anotherUser);
        articleFavoriteRepository.save(new ArticleFavorite(article.getId(), anotherUser.getId()));

        ArticleData articleData = queryService.findById(article.getId(), anotherUser).get();
        assertThat(articleData.getFavoritesCount(), is(1));
        assertThat(articleData.isFavorited(), is(true));
    }
}