package io.spring.infrastructure.mybatis.readservice;

import io.spring.application.data.ArticleFavoriteCount;
import io.spring.core.user.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

@Mapper
public interface ArticleFavoritesReadService {
    boolean isUserFavorite(@Param("userId") String userId, @Param("articleId") String articleId);

    int articleFavoriteCount(@Param("articleId") String articleId);

    List<ArticleFavoriteCount> articlesFavoriteCount(@Param("ids") List<String> ids);

    Set<String> userFavorites(@Param("ids") List<String> ids, @Param("currentUser") User currentUser);
}
