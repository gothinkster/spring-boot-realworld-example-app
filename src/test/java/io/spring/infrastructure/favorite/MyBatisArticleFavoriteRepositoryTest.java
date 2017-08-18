package io.spring.infrastructure.favorite;

import io.spring.core.favorite.ArticleFavorite;
import io.spring.core.favorite.ArticleFavoriteRepository;
import io.spring.infrastructure.repository.MyBatisArticleFavoriteRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@MybatisTest
@Import({MyBatisArticleFavoriteRepository.class})
public class MyBatisArticleFavoriteRepositoryTest {
    @Autowired
    private ArticleFavoriteRepository articleFavoriteRepository;

    @Autowired
    private io.spring.infrastructure.mybatis.mapper.ArticleFavoriteMapper articleFavoriteMapper;

    @Test
    public void should_save_and_fetch_articleFavorite_success() throws Exception {
        ArticleFavorite articleFavorite = new ArticleFavorite("123", "456");
        articleFavoriteRepository.save(articleFavorite);
        assertThat(articleFavoriteMapper.find(articleFavorite.getArticleId(), articleFavorite.getUserId()), notNullValue());
    }

    @Test
    public void should_remove_favorite_success() throws Exception {
        ArticleFavorite articleFavorite = new ArticleFavorite("123", "456");
        articleFavoriteRepository.save(articleFavorite);
        articleFavoriteRepository.remove(articleFavorite);
        assertThat(articleFavoriteRepository.find("123", "456").isPresent(), is(false));
    }
}