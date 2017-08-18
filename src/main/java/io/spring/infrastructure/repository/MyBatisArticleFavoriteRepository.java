package io.spring.infrastructure.repository;

import io.spring.core.favorite.ArticleFavorite;
import io.spring.core.favorite.ArticleFavoriteRepository;
import io.spring.infrastructure.mybatis.mapper.ArticleFavoriteMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class MyBatisArticleFavoriteRepository implements ArticleFavoriteRepository {
    private ArticleFavoriteMapper mapper;

    @Autowired
    public MyBatisArticleFavoriteRepository(ArticleFavoriteMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void save(ArticleFavorite articleFavorite) {
        if (mapper.find(articleFavorite.getArticleId(), articleFavorite.getUserId()) == null) {
            mapper.insert(articleFavorite);
        }
    }

    @Override
    public Optional<ArticleFavorite> find(String articleId, String userId) {
        return Optional.ofNullable(mapper.find(articleId, userId));
    }

    @Override
    public void remove(ArticleFavorite favorite) {
        mapper.delete(favorite);
    }
}
