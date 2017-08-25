package io.spring.application;

import io.spring.application.data.ArticleData;
import io.spring.application.data.ArticleDataList;
import io.spring.application.data.ArticleFavoriteCount;
import io.spring.core.user.User;
import io.spring.infrastructure.mybatis.readservice.ArticleFavoritesReadService;
import io.spring.infrastructure.mybatis.readservice.ArticleReadService;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toList;

@Service
public class ArticleQueryService {
    private ArticleReadService articleReadService;
    private UserRelationshipQueryService userRelationshipQueryService;
    private ArticleFavoritesReadService articleFavoritesReadService;

    @Autowired
    public ArticleQueryService(ArticleReadService articleReadService,
                               UserRelationshipQueryService userRelationshipQueryService,
                               ArticleFavoritesReadService articleFavoritesReadService) {
        this.articleReadService = articleReadService;
        this.userRelationshipQueryService = userRelationshipQueryService;
        this.articleFavoritesReadService = articleFavoritesReadService;
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

    public ArticleDataList findRecentArticles(String tag, String author, String favoritedBy, Page page, User currentUser) {
        List<String> articleIds = articleReadService.queryArticles(tag, author, favoritedBy, page);
        int articleCount = articleReadService.countArticle(tag, author, favoritedBy);
        if (articleIds.size() == 0) {
            return new ArticleDataList(new ArrayList<>(), articleCount);
        } else {
            List<ArticleData> articles = articleReadService.findArticles(articleIds);
            fillExtraInfo(articles, currentUser);
            return new ArticleDataList(articles, articleCount);
        }
    }

    private void fillExtraInfo(List<ArticleData> articles, User currentUser) {
        setFavoriteCount(articles);
        if (currentUser != null) {
            setIsFavorite(articles, currentUser);
            setIsFollowingAuthor(articles, currentUser);
        }
    }

    private void setIsFollowingAuthor(List<ArticleData> articles, User currentUser) {
        Set<String> followingAuthors = userRelationshipQueryService.followingAuthors(
            currentUser.getId(),
            articles.stream().map(articleData1 -> articleData1.getProfileData().getId()).collect(toList()));
        articles.forEach(articleData -> {
            if (followingAuthors.contains(articleData.getProfileData().getId())) {
                articleData.getProfileData().setFollowing(true);
            }
        });
    }

    private void setFavoriteCount(List<ArticleData> articles) {
        List<ArticleFavoriteCount> favoritesCounts = articleFavoritesReadService.articlesFavoriteCount(articles.stream().map(ArticleData::getId).collect(toList()));
        Map<String, Integer> countMap = new HashMap<>();
        favoritesCounts.forEach(item -> {
            countMap.put(item.getId(), item.getCount());
        });
        articles.forEach(articleData -> articleData.setFavoritesCount(countMap.get(articleData.getId())));
    }

    private void setIsFavorite(List<ArticleData> articles, User currentUser) {
        Set<String> favoritedArticles = articleFavoritesReadService.userFavorites(articles.stream().map(articleData -> articleData.getId()).collect(toList()), currentUser);

        articles.forEach(articleData -> {
            if (favoritedArticles.contains(articleData.getId())) {
                articleData.setFavorited(true);
            }
        });
    }

    private void fillExtraInfo(String id, User user, ArticleData articleData) {
        articleData.setFavorited(articleFavoritesReadService.isUserFavorite(user.getId(), id));
        articleData.setFavoritesCount(articleFavoritesReadService.articleFavoriteCount(id));
        articleData.getProfileData().setFollowing(
            userRelationshipQueryService.isUserFollowing(
                user.getId(),
                articleData.getProfileData().getId()));
    }

    public ArticleDataList findUserFeed(User user, Page page) {
        List<String> followdUsers = userRelationshipQueryService.followedUsers(user.getId());
        if (followdUsers.size() == 0) {
            return new ArticleDataList(new ArrayList<>(), 0);
        } else {
            List<ArticleData> articles = articleReadService.findArticlesOfAuthors(followdUsers, page);
            fillExtraInfo(articles, user);
            int count = articleReadService.countFeedSize(followdUsers);
            return new ArticleDataList(articles, count);
        }
    }
}

