package io.spring.infrastructure.favorite;

import io.spring.core.favorite.ArticleFavorite;
import io.spring.core.favorite.ArticleFavoriteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class MyBatisArticleFavoriteRepository implements ArticleFavoriteRepository {
    private ArticleFavoriteMapper mapper;

    @Autowired
    public MyBatisArticleFavoriteRepository(ArticleFavoriteMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void save(ArticleFavorite articleFavorite) {
        if (!mapper.find(articleFavorite)) {
            mapper.insert(articleFavorite);
        }
    }
}
