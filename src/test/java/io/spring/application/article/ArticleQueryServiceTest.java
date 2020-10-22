package io.spring.application.article;

import io.spring.application.ArticleQueryService;
import io.spring.application.Page;
import io.spring.application.data.ArticleData;
import io.spring.application.data.ArticleDataList;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.favorite.ArticleFavorite;
import io.spring.core.favorite.ArticleFavoriteRepository;
import io.spring.core.user.FollowRelation;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.repository.MyBatisArticleFavoriteRepository;
import io.spring.infrastructure.repository.MyBatisArticleRepository;
import io.spring.infrastructure.repository.MyBatisUserRepository;
import org.joda.time.DateTime;
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@MybatisTest
@Import({
    ArticleQueryService.class,
    MyBatisUserRepository.class,
    MyBatisArticleRepository.class,
    MyBatisArticleFavoriteRepository.class})
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
    public void setUp() {
        user = new User("aisensiy@gmail.com", "aisensiy", "123", "", "");
        userRepository.save(user);
        article = new Article("test", "desc", "body", new String[]{"java", "spring"}, user.getId(), new DateTime());
        articleRepository.save(article);
    }

    @Test
    public void should_fetch_article_success() {
        Optional<ArticleData> optional = queryService.findById(article.getId(), user);
        assertTrue(optional.isPresent());

        ArticleData fetched = optional.get();
        assertEquals(fetched.getFavoritesCount(),0);
        assertFalse(fetched.isFavorited());
        assertNotNull(fetched.getCreatedAt());
        assertNotNull(fetched.getUpdatedAt());
        assertTrue(fetched.getTagList().contains("java"));
    }

    @Test
    public void should_get_article_with_right_favorite_and_favorite_count() {
        User anotherUser = new User("other@test.com", "other", "123", "", "");
        userRepository.save(anotherUser);
        articleFavoriteRepository.save(new ArticleFavorite(article.getId(), anotherUser.getId()));

        Optional<ArticleData> optional = queryService.findById(article.getId(), anotherUser);
        assertTrue(optional.isPresent());

        ArticleData articleData = optional.get();
        assertEquals(articleData.getFavoritesCount(), 1);
        assertTrue(articleData.isFavorited());
    }

    @Test
    public void should_get_default_article_list() {
        Article anotherArticle = new Article("new article", "desc", "body", new String[]{"test"}, user.getId(), new DateTime().minusHours(1));
        articleRepository.save(anotherArticle);

        ArticleDataList recentArticles = queryService.findRecentArticles(null, null, null, new Page(), user);
        assertEquals(recentArticles.getCount(), 2);
        assertEquals(recentArticles.getArticleDatas().size(), 2);
        assertEquals(recentArticles.getArticleDatas().get(0).getId(), article.getId());

        ArticleDataList nodata = queryService.findRecentArticles(null, null, null, new Page(2, 10), user);
        assertEquals(nodata.getCount(),2);
        assertEquals(nodata.getArticleDatas().size(), 0);
    }

    @Test
    public void should_query_article_by_author() {
        User anotherUser = new User("other@email.com", "other", "123", "", "");
        userRepository.save(anotherUser);

        Article anotherArticle = new Article("new article", "desc", "body", new String[]{"test"}, anotherUser.getId());
        articleRepository.save(anotherArticle);

        ArticleDataList recentArticles = queryService.findRecentArticles(null, user.getUsername(), null, new Page(), user);
        assertEquals(recentArticles.getArticleDatas().size(), 1);
        assertEquals(recentArticles.getCount(), 1);
    }

    @Test
    public void should_query_article_by_favorite() {
        User anotherUser = new User("other@email.com", "other", "123", "", "");
        userRepository.save(anotherUser);

        Article anotherArticle = new Article("new article", "desc", "body", new String[]{"test"}, anotherUser.getId());
        articleRepository.save(anotherArticle);

        ArticleFavorite articleFavorite = new ArticleFavorite(article.getId(), anotherUser.getId());
        articleFavoriteRepository.save(articleFavorite);

        ArticleDataList recentArticles = queryService.findRecentArticles(null, null, anotherUser.getUsername(), new Page(), anotherUser);
        assertEquals(recentArticles.getArticleDatas().size(), 1);
        assertEquals(recentArticles.getCount(), 1);
        ArticleData articleData = recentArticles.getArticleDatas().get(0);
        assertEquals(articleData.getId(), article.getId());
        assertEquals(articleData.getFavoritesCount(),1);
        assertTrue(articleData.isFavorited());
    }

    @Test
    public void should_query_article_by_tag() {
        Article anotherArticle = new Article("new article", "desc", "body", new String[]{"test"}, user.getId());
        articleRepository.save(anotherArticle);

        ArticleDataList recentArticles = queryService.findRecentArticles("spring", null, null, new Page(), user);
        assertEquals(recentArticles.getArticleDatas().size(), 1);
        assertEquals(recentArticles.getCount(), 1);
        assertEquals(recentArticles.getArticleDatas().get(0).getId(), article.getId());

        ArticleDataList notag = queryService.findRecentArticles("notag", null, null, new Page(), user);
        assertEquals(notag.getCount(), 0);
    }

    @Test
    public void should_show_following_if_user_followed_author() {
        User anotherUser = new User("other@email.com", "other", "123", "", "");
        userRepository.save(anotherUser);

        FollowRelation followRelation = new FollowRelation(anotherUser.getId(), user.getId());
        userRepository.saveRelation(followRelation);

        ArticleDataList recentArticles = queryService.findRecentArticles(null, null, null, new Page(), anotherUser);
        assertEquals(recentArticles.getCount(), 1);
        ArticleData articleData = recentArticles.getArticleDatas().get(0);
        assertTrue(articleData.getProfileData().isFollowing());
    }

    @Test
    public void should_get_user_feed() {
        User anotherUser = new User("other@email.com", "other", "123", "", "");
        userRepository.save(anotherUser);

        FollowRelation followRelation = new FollowRelation(anotherUser.getId(), user.getId());
        userRepository.saveRelation(followRelation);

        ArticleDataList userFeed = queryService.findUserFeed(user, new Page());
        assertEquals(userFeed.getCount(), 0);

        ArticleDataList anotherUserFeed = queryService.findUserFeed(anotherUser, new Page());
        assertEquals(anotherUserFeed.getCount(), 1);
        ArticleData articleData = anotherUserFeed.getArticleDatas().get(0);
        assertTrue(articleData.getProfileData().isFollowing());
    }
}