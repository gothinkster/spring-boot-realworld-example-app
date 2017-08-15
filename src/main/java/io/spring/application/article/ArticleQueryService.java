package io.spring.application.article;

import io.spring.application.profile.UserRelationshipQueryService;
import io.spring.core.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ArticleQueryService {
    private ArticleReadService articleReadService;
    private UserRelationshipQueryService userRelationshipQueryService;
    private ArticleFavoritesQueryService articleFavoritesQueryService;

    @Autowired
    public ArticleQueryService(ArticleReadService articleReadService,
                               UserRelationshipQueryService userRelationshipQueryService,
                               ArticleFavoritesQueryService articleFavoritesQueryService) {
        this.articleReadService = articleReadService;
        this.userRelationshipQueryService = userRelationshipQueryService;
        this.articleFavoritesQueryService = articleFavoritesQueryService;
    }

    public Optional<ArticleData> findById(String id, User user) {
        ArticleData articleData = articleReadService.ofId(id);
        if (articleData == null) {
            return Optional.empty();
        } else {
            articleData.setFavorited(articleFavoritesQueryService.isUserFavorite(user.getId(), id));
            articleData.setFavoritesCount(articleFavoritesQueryService.articleFavoriteCount(id));
            articleData.getProfileData().setFollowing(
                userRelationshipQueryService.isUserFollowing(
                    user.getId(),
                    articleData.getProfileData().getId()));
            return Optional.of(articleData);
        }
    }
}
