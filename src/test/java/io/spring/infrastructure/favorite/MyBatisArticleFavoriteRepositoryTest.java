package io.spring.infrastructure.favorite;

import io.spring.core.favorite.ArticleFavorite;
import io.spring.core.favorite.ArticleFavoriteRepository;
import io.spring.infrastructure.DbTestBase;
import io.spring.infrastructure.mybatis.mapper.ArticleFavoriteMapper;
import io.spring.infrastructure.repository.MyBatisArticleFavoriteRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

@Import({MyBatisArticleFavoriteRepository.class})
public class MyBatisArticleFavoriteRepositoryTest extends DbTestBase {

    @Autowired
    private ArticleFavoriteRepository articleFavoriteRepository;

    @Autowired
    private ArticleFavoriteMapper articleFavoriteMapper;

    @Test
    public void should_save_and_fetch_articleFavorite_success() {
        var articleFavorite = new ArticleFavorite("123", "456");
        articleFavoriteRepository.save(articleFavorite);
        assertNotNull(articleFavoriteMapper.find(articleFavorite.getArticleId(), articleFavorite.getUserId()));
    }

    @Test
    public void should_remove_favorite_success() {
        var articleFavorite = new ArticleFavorite("123", "456");
        articleFavoriteRepository.save(articleFavorite);
        articleFavoriteRepository.remove(articleFavorite);
        assertFalse(articleFavoriteRepository.find("123", "456").isPresent());
    }

}
