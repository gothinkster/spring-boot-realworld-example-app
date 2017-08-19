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
import io.spring.infrastructure.repository.MyBatisArticleRepository;
import io.spring.infrastructure.repository.MyBatisArticleFavoriteRepository;
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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

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
    public void setUp() throws Exception {
        user = new User("aisensiy@gmail.com", "aisensiy", "123", "", "");
        userRepository.save(user);
        article = new Article("test", "desc", "body", new String[]{"java", "spring"}, user.getId(), new DateTime());
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

    @Test
    public void should_get_default_article_list() throws Exception {
        Article anotherArticle = new Article("new article", "desc", "body", new String[]{"test"}, user.getId(), new DateTime().minusHours(1));
        articleRepository.save(anotherArticle);

        ArticleDataList recentArticles = queryService.findRecentArticles(null, null, null, new Page(), user);
        assertThat(recentArticles.getCount(), is(2));
        assertThat(recentArticles.getArticleDatas().size(), is(2));
        assertThat(recentArticles.getArticleDatas().get(0).getId(), is(article.getId()));

        ArticleDataList nodata = queryService.findRecentArticles(null, null, null, new Page(2, 10), user);
        assertThat(nodata.getCount(), is(2));
        assertThat(nodata.getArticleDatas().size(), is(0));
    }

    @Test
    public void should_query_article_by_author() throws Exception {
        User anotherUser = new User("other@email.com", "other", "123", "", "");
        userRepository.save(anotherUser);

        Article anotherArticle = new Article("new article", "desc", "body", new String[]{"test"}, anotherUser.getId());
        articleRepository.save(anotherArticle);

        ArticleDataList recentArticles = queryService.findRecentArticles(null, user.getUsername(), null, new Page(), user);
        assertThat(recentArticles.getArticleDatas().size(), is(1));
        assertThat(recentArticles.getCount(), is(1));
    }

    @Test
    public void should_query_article_by_favorite() throws Exception {
        User anotherUser = new User("other@email.com", "other", "123", "", "");
        userRepository.save(anotherUser);

        Article anotherArticle = new Article("new article", "desc", "body", new String[]{"test"}, anotherUser.getId());
        articleRepository.save(anotherArticle);

        ArticleFavorite articleFavorite = new ArticleFavorite(article.getId(), anotherUser.getId());
        articleFavoriteRepository.save(articleFavorite);

        ArticleDataList recentArticles = queryService.findRecentArticles(null, null, anotherUser.getUsername(), new Page(), anotherUser);
        assertThat(recentArticles.getArticleDatas().size(), is(1));
        assertThat(recentArticles.getCount(), is(1));
        ArticleData articleData = recentArticles.getArticleDatas().get(0);
        assertThat(articleData.getId(), is(article.getId()));
        assertThat(articleData.getFavoritesCount(), is(1));
        assertThat(articleData.isFavorited(), is(true));
    }

    @Test
    public void should_query_article_by_tag() throws Exception {
        Article anotherArticle = new Article("new article", "desc", "body", new String[]{"test"}, user.getId());
        articleRepository.save(anotherArticle);

        ArticleDataList recentArticles = queryService.findRecentArticles("spring", null, null, new Page(), user);
        assertThat(recentArticles.getArticleDatas().size(), is(1));
        assertThat(recentArticles.getCount(), is(1));
        assertThat(recentArticles.getArticleDatas().get(0).getId(), is(article.getId()));

        ArticleDataList notag = queryService.findRecentArticles("notag", null, null, new Page(), user);
        assertThat(notag.getCount(), is(0));
    }

    @Test
    public void should_show_following_if_user_followed_author() throws Exception {
        User anotherUser = new User("other@email.com", "other", "123", "", "");
        userRepository.save(anotherUser);

        FollowRelation followRelation = new FollowRelation(anotherUser.getId(), user.getId());
        userRepository.saveRelation(followRelation);

        ArticleDataList recentArticles = queryService.findRecentArticles(null, null, null, new Page(), anotherUser);
        assertThat(recentArticles.getCount(), is(1));
        ArticleData articleData = recentArticles.getArticleDatas().get(0);
        assertThat(articleData.getProfileData().isFollowing(), is(true));
    }

    @Test
    public void should_get_user_feed() throws Exception {
        User anotherUser = new User("other@email.com", "other", "123", "", "");
        userRepository.save(anotherUser);

        FollowRelation followRelation = new FollowRelation(anotherUser.getId(), user.getId());
        userRepository.saveRelation(followRelation);

        ArticleDataList userFeed = queryService.findUserFeed(user, new Page());
        assertThat(userFeed.getCount(), is(0));

        ArticleDataList anotherUserFeed = queryService.findUserFeed(anotherUser, new Page());
        assertThat(anotherUserFeed.getCount(), is(1));
        ArticleData articleData = anotherUserFeed.getArticleDatas().get(0);
        assertThat(articleData.getProfileData().isFollowing(), is(true));
    }
}