package io.spring.application;

import io.spring.application.data.ArticleData;
import io.spring.application.data.ArticleDataList;
import io.spring.core.user.User;
import io.spring.infrastructure.mybatis.readservice.ArticleFavoritesReadService;
import io.spring.infrastructure.mybatis.readservice.ArticleReadService;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import lombok.RequiredArgsConstructor;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ArticleQueryService {

    private final ArticleReadService articleReadService;
    private final UserRelationshipQueryService userRelationshipQueryService;
    private final ArticleFavoritesReadService articleFavoritesReadService;


    public Optional<ArticleData> findById(String id, User user) {
        var articleData = articleReadService.findById(id);
        if (articleData == null) {
            return Optional.empty();
        }
        if (user != null) {
            fillExtraInfo(id, user, articleData);
        }
        return Optional.of(articleData);
    }


    public Optional<ArticleData> findBySlug(String slug, User user) {
        var articleData = articleReadService.findBySlug(slug);
        if (articleData == null) {
            return Optional.empty();
        }
        if (user != null) {
            fillExtraInfo(articleData.getId(), user, articleData);
        }
        return Optional.of(articleData);
    }


    public CursorPager<ArticleData> findRecentArticlesWithCursor(
            String tag,
            String author,
            String favoritedBy,
            CursorPageParameter<DateTime> page,
            User currentUser
    ) {
        var articleIds = articleReadService.findArticlesWithCursor(tag, author, favoritedBy, page);
        if (articleIds.size() == 0) {
            return new CursorPager<>(new ArrayList<>(), page.getDirection(), false);
        }

        var hasExtra = articleIds.size() > page.getLimit();
        if (hasExtra) {
            articleIds.remove(page.getLimit());
        }
        if (!page.isNext()) {
            Collections.reverse(articleIds);
        }

        var articles = articleReadService.findArticles(articleIds);
        fillExtraInfo(articles, currentUser);
        return new CursorPager<>(articles, page.getDirection(), hasExtra);
    }


    public CursorPager<ArticleData> findUserFeedWithCursor(
            User user,
            CursorPageParameter<DateTime> page
    ) {
        var followdUsers = userRelationshipQueryService.followedUsers(user.getId());
        if (followdUsers.size() == 0) {
            return new CursorPager<>(new ArrayList<>(), page.getDirection(), false);
        }
        var articles = articleReadService.findArticlesOfAuthorsWithCursor(followdUsers, page);
        var hasExtra = articles.size() > page.getLimit();
        if (hasExtra) {
            articles.remove(page.getLimit());
        }
        if (!page.isNext()) {
            Collections.reverse(articles);
        }
        fillExtraInfo(articles, user);
        return new CursorPager<>(articles, page.getDirection(), hasExtra);
    }


    public ArticleDataList findRecentArticles(
            String tag,
            String author,
            String favoritedBy,
            Page page,
            User currentUser
    ) {
        var articleIds = articleReadService.queryArticles(tag, author, favoritedBy, page);
        var articleCount = articleReadService.countArticle(tag, author, favoritedBy);
        if (articleIds.size() == 0) {
            return new ArticleDataList(new ArrayList<>(), articleCount);
        }
        var articles = articleReadService.findArticles(articleIds);
        fillExtraInfo(articles, currentUser);
        return new ArticleDataList(articles, articleCount);
    }


    public ArticleDataList findUserFeed(User user, Page page) {
        var followdUsers = userRelationshipQueryService.followedUsers(user.getId());
        if (followdUsers.size() == 0) {
            return new ArticleDataList(new ArrayList<>(), 0);
        }
        var articles = articleReadService.findArticlesOfAuthors(followdUsers, page);
        fillExtraInfo(articles, user);
        var count = articleReadService.countFeedSize(followdUsers);
        return new ArticleDataList(articles, count);
    }


    private void fillExtraInfo(List<ArticleData> articles, User currentUser) {
        setFavoriteCount(articles);
        if (currentUser != null) {
            setIsFavorite(articles, currentUser);
            setIsFollowingAuthor(articles, currentUser);
        }
    }


    private void setIsFollowingAuthor(List<ArticleData> articles, User currentUser) {
        var ids = articles.stream()
                .map(articleData1 -> articleData1.getProfileData().getId())
                .toList();
        var followingAuthors = userRelationshipQueryService.followingAuthors(
                currentUser.getId(),
                ids
        );
        articles.forEach(articleData -> {
            if (followingAuthors.contains(articleData.getProfileData().getId())) {
                articleData.getProfileData().setFollowing(true);
            }
        });
    }


    private void setFavoriteCount(List<ArticleData> articles) {
        var ids = getIds(articles);
        var favoritesCounts = articleFavoritesReadService.articlesFavoriteCount(ids);
        var countMap = new HashMap<String, Integer>();
        favoritesCounts.forEach(item -> countMap.put(item.getId(), item.getCount()));
        articles.forEach(articleData -> articleData.setFavoritesCount(countMap.get(articleData.getId())));
    }


    private void setIsFavorite(List<ArticleData> articles, User currentUser) {
        var ids = getIds(articles);
        var favoritedArticles = articleFavoritesReadService.userFavorites(ids, currentUser);
        articles.forEach(articleData -> {
            if (favoritedArticles.contains(articleData.getId())) {
                articleData.setFavorited(true);
            }
        });
    }


    private List<String> getIds(List<ArticleData> articles) {
        return articles.stream()
                .map(ArticleData::getId)
                .toList();
    }


    private void fillExtraInfo(String id, User user, ArticleData articleData) {
        articleData.setFavorited(articleFavoritesReadService.isUserFavorite(user.getId(), id));
        articleData.setFavoritesCount(articleFavoritesReadService.articleFavoriteCount(id));
        var userFollowing = userRelationshipQueryService.isUserFollowing(
                user.getId(),
                articleData.getProfileData().getId()
        );
        articleData
                .getProfileData()
                .setFollowing(userFollowing);
    }

}
