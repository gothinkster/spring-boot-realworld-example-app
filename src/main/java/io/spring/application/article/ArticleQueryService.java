package io.spring.application.article;

import io.spring.application.Page;
import io.spring.application.profile.UserRelationshipQueryService;
import io.spring.core.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
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
        ArticleData articleData = articleReadService.findById(id);
        if (articleData == null) {
            return Optional.empty();
        } else {
            if (user != null) {
                fillExtraInfo(id, user, articleData);
            }
            return Optional.of(articleData);
        }
    }

    public Optional<ArticleData> findBySlug(String slug, User user) {
        ArticleData articleData = articleReadService.findBySlug(slug);
        if (articleData == null) {
            return Optional.empty();
        } else {
            if (user != null) {
                fillExtraInfo(articleData.getId(), user, articleData);
            }
            return Optional.of(articleData);
        }
    }

    private void fillExtraInfo(String id, User user, ArticleData articleData) {
        articleData.setFavorited(articleFavoritesQueryService.isUserFavorite(user.getId(), id));
        articleData.setFavoritesCount(articleFavoritesQueryService.articleFavoriteCount(id));
        articleData.getProfileData().setFollowing(
            userRelationshipQueryService.isUserFollowing(
                user.getId(),
                articleData.getProfileData().getId()));
    }

    public ArticleDataList findRecentArticles(String tag, String author, String favoritedBy, Page page) {
        List<String> articleIds = articleReadService.queryArticles(tag, author, favoritedBy, page);
        int articleCount = articleReadService.countArticle(tag, author, favoritedBy);
        return new ArticleDataList(
            articleIds.size() == 0 ? new ArrayList<>() : articleReadService.findArticles(articleIds),
            articleCount);
    }
}
