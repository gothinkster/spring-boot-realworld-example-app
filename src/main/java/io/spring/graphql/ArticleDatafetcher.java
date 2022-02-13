package io.spring.graphql;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import com.netflix.graphql.dgs.InputArgument;
import graphql.execution.DataFetcherResult;
import graphql.relay.DefaultConnectionCursor;
import graphql.relay.DefaultPageInfo;
import graphql.schema.DataFetchingEnvironment;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ArticleQueryService;
import io.spring.application.CursorPageParameter;
import io.spring.application.CursorPager;
import io.spring.application.CursorPager.Direction;
import io.spring.application.DateTimeCursor;
import io.spring.application.data.ArticleData;
import io.spring.application.data.CommentData;
import io.spring.core.user.UserRepository;
import io.spring.graphql.DgsConstants.ARTICLEPAYLOAD;
import io.spring.graphql.DgsConstants.COMMENT;
import io.spring.graphql.DgsConstants.PROFILE;
import io.spring.graphql.DgsConstants.QUERY;
import io.spring.graphql.types.Article;
import io.spring.graphql.types.ArticleEdge;
import io.spring.graphql.types.ArticlesConnection;
import io.spring.graphql.types.Profile;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import java.util.Map;
import java.util.function.Function;

import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toMap;

@DgsComponent
@RequiredArgsConstructor
public class ArticleDatafetcher {

    private final ArticleQueryService articleQueryService;
    private final UserRepository userRepository;


    @DgsData(parentType = DgsConstants.QUERY_TYPE, field = QUERY.Feed)
    public DataFetcherResult<ArticlesConnection> getFeed(
            @InputArgument("first") Integer first,
            @InputArgument("after") String after,
            @InputArgument("last") Integer last,
            @InputArgument("before") String before,
            DgsDataFetchingEnvironment dfe
    ) {
        throwIfBothNull(first, last);
        var current = SecurityUtils.getCurrentUser().orElse(null);
        var cursorPageParameter = getCursorPageParameter(first, before, last, after);
        var articles = articleQueryService.findUserFeedWithCursor(current, cursorPageParameter);
        var pageInfo = buildArticlePageInfo(articles);
        var articlesConnection = getArticlesConnection(pageInfo, articles);
        return getBuild(articlesConnection, articles);
    }


    @DgsData(parentType = PROFILE.TYPE_NAME, field = PROFILE.Feed)
    public DataFetcherResult<ArticlesConnection> userFeed(
            @InputArgument("first") Integer first,
            @InputArgument("after") String after,
            @InputArgument("last") Integer last,
            @InputArgument("before") String before,
            DgsDataFetchingEnvironment dfe
    ) {
        throwIfBothNull(first, last);
        var profile = dfe.<Profile>getSource();
        var target = userRepository
                .findByUsername(profile.getUsername())
                .orElseThrow(ResourceNotFoundException::new);
        var cursorPageParameter = getCursorPageParameter(first, before, last, after);
        var articles = articleQueryService.findUserFeedWithCursor(target, cursorPageParameter);
        var pageInfo = buildArticlePageInfo(articles);
        ArticlesConnection articlesConnection = getArticlesConnection(pageInfo, articles);
        return getBuild(articlesConnection, articles);
    }


    @DgsData(parentType = PROFILE.TYPE_NAME, field = PROFILE.Favorites)
    public DataFetcherResult<ArticlesConnection> userFavorites(
            @InputArgument("first") Integer first,
            @InputArgument("after") String after,
            @InputArgument("last") Integer last,
            @InputArgument("before") String before,
            DgsDataFetchingEnvironment dfe
    ) {
        throwIfBothNull(first, last);
        var current = SecurityUtils.getCurrentUser().orElse(null);
        var profile = dfe.<Profile>getSource();
        var cursorPageParameter = getCursorPageParameter(first, before, last, after);
        var articles = articleQueryService.findRecentArticlesWithCursor(
                null,
                null,
                profile.getUsername(),
                cursorPageParameter,
                current
        );
        var pageInfo = buildArticlePageInfo(articles);
        var articlesConnection = getArticlesConnection(pageInfo, articles);
        return getBuild(articlesConnection, articles);
    }


    @DgsData(parentType = PROFILE.TYPE_NAME, field = PROFILE.Articles)
    public DataFetcherResult<ArticlesConnection> userArticles(
            @InputArgument("first") Integer first,
            @InputArgument("after") String after,
            @InputArgument("last") Integer last,
            @InputArgument("before") String before,
            DgsDataFetchingEnvironment dfe
    ) {
        throwIfBothNull(first, last);
        var current = SecurityUtils.getCurrentUser().orElse(null);
        var profile = dfe.<Profile>getSource();
        var cursorPageParameter = getCursorPageParameter(first, before, last, after);
        var articles = articleQueryService.findRecentArticlesWithCursor(
                null,
                profile.getUsername(),
                null,
                cursorPageParameter,
                current
        );
        var pageInfo = buildArticlePageInfo(articles);
        var articlesConnection = getArticlesConnection(pageInfo, articles);
        return getBuild(articlesConnection, articles);
    }


    @DgsData(parentType = DgsConstants.QUERY_TYPE, field = QUERY.Articles)
    public DataFetcherResult<ArticlesConnection> getArticles(
            @InputArgument("first") Integer first,
            @InputArgument("after") String after,
            @InputArgument("last") Integer last,
            @InputArgument("before") String before,
            @InputArgument("authoredBy") String authoredBy,
            @InputArgument("favoritedBy") String favoritedBy,
            @InputArgument("withTag") String withTag,
            DgsDataFetchingEnvironment dfe
    ) {
        throwIfBothNull(first, last);
        var current = SecurityUtils.getCurrentUser().orElse(null);
        var cursorPageParameter = getCursorPageParameter(first, before, last, after);
        var articles = articleQueryService.findRecentArticlesWithCursor(
                withTag,
                authoredBy,
                favoritedBy,
                cursorPageParameter,
                current
        );
        var pageInfo = buildArticlePageInfo(articles);
        var articlesConnection = getArticlesConnection(pageInfo, articles);
        return getBuild(articlesConnection, articles);
    }


    private DataFetcherResult<ArticlesConnection> getBuild(
            ArticlesConnection articlesConnection,
            CursorPager<ArticleData> articles
    ) {
        return DataFetcherResult.<ArticlesConnection>newResult()
                .data(articlesConnection)
                .localContext(articles.getData()
                        .stream()
                        .collect(toMap(ArticleData::getSlug, a -> a))
                )
                .build();
    }


    private ArticlesConnection getArticlesConnection(DefaultPageInfo pageInfo, CursorPager<ArticleData> articles) {
        return ArticlesConnection.newBuilder()
                .pageInfo(pageInfo)
                .edges(articles.getData()
                        .stream()
                        .map(getArticleEdgeFunction())
                        .toList()
                )
                .build();
    }


    @NotNull
    private CursorPageParameter<DateTime> getCursorPageParameter(Integer first, String before, Integer last, String after) {
        return isNull(first) ?
                new CursorPageParameter<>(DateTimeCursor.parse(before), last, Direction.PREV) :
                new CursorPageParameter<>(DateTimeCursor.parse(after), first, Direction.NEXT);
    }


    @NotNull
    private Function<ArticleData, ArticleEdge> getArticleEdgeFunction() {
        return a -> ArticleEdge.newBuilder()
                .cursor(a.getCursor().toString())
                .node(buildArticleResult(a))
                .build();
    }


    private void throwIfBothNull(Integer first, Integer last) {
        if (first == null && last == null) {
            throw new IllegalArgumentException("first and last are null");
        }
    }


    @DgsData(parentType = ARTICLEPAYLOAD.TYPE_NAME, field = ARTICLEPAYLOAD.Article)
    public DataFetcherResult<Article> getArticle(DataFetchingEnvironment dfe) {
        var article = dfe.<io.spring.core.article.Article>getLocalContext();

        var current = SecurityUtils.getCurrentUser().orElse(null);
        var articleData = articleQueryService
                .findById(article.getId(), current)
                .orElseThrow(ResourceNotFoundException::new);
        var articleResult = buildArticleResult(articleData);
        return DataFetcherResult.<Article>newResult()
                .localContext(Map.of(articleData.getSlug(), articleData))
                .data(articleResult)
                .build();
    }


    @DgsData(parentType = COMMENT.TYPE_NAME, field = COMMENT.Article)
    public DataFetcherResult<Article> getCommentArticle(
            DataFetchingEnvironment dataFetchingEnvironment
    ) {
        var comment = dataFetchingEnvironment.<CommentData>getLocalContext();
        var current = SecurityUtils.getCurrentUser().orElse(null);
        var articleData = articleQueryService
                .findById(comment.getArticleId(), current)
                .orElseThrow(ResourceNotFoundException::new);
        var articleResult = buildArticleResult(articleData);
        return DataFetcherResult.<Article>newResult()
                .localContext(Map.of(articleData.getSlug(), articleData))
                .data(articleResult)
                .build();
    }


    @DgsData(parentType = DgsConstants.QUERY_TYPE, field = QUERY.Article)
    public DataFetcherResult<Article> findArticleBySlug(@InputArgument("slug") String slug) {
        var current = SecurityUtils.getCurrentUser().orElse(null);
        var articleData = articleQueryService.findBySlug(slug, current)
                .orElseThrow(ResourceNotFoundException::new);
        var articleResult = buildArticleResult(articleData);
        return DataFetcherResult.<Article>newResult()
                .localContext(Map.of(articleData.getSlug(), articleData))
                .data(articleResult)
                .build();
    }


    private DefaultPageInfo buildArticlePageInfo(CursorPager<ArticleData> articles) {
        return new DefaultPageInfo(
                articles.getStartCursor() == null ?
                        null :
                        new DefaultConnectionCursor(articles.getStartCursor().toString()),
                articles.getEndCursor() == null ?
                        null :
                        new DefaultConnectionCursor(articles.getEndCursor().toString()),
                articles.hasPrevious(),
                articles.hasNext()
        );
    }


    private Article buildArticleResult(ArticleData articleData) {
        return Article.newBuilder()
                .body(articleData.getBody())
                .createdAt(ISODateTimeFormat.dateTime().withZoneUTC().print(articleData.getCreatedAt()))
                .description(articleData.getDescription())
                .favorited(articleData.isFavorited())
                .favoritesCount(articleData.getFavoritesCount())
                .slug(articleData.getSlug())
                .tagList(articleData.getTagList())
                .title(articleData.getTitle())
                .updatedAt(ISODateTimeFormat.dateTime().withZoneUTC().print(articleData.getUpdatedAt()))
                .build();
    }

}
