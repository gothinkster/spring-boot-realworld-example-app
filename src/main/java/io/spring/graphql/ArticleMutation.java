package io.spring.graphql;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.InputArgument;
import graphql.execution.DataFetcherResult;
import io.spring.api.exception.NoAuthorizationException;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.article.ArticleCommandService;
import io.spring.application.article.NewArticleParam;
import io.spring.application.article.UpdateArticleParam;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.favorite.ArticleFavorite;
import io.spring.core.favorite.ArticleFavoriteRepository;
import io.spring.core.service.AuthorizationService;
import io.spring.core.user.User;
import io.spring.graphql.DgsConstants.MUTATION;
import io.spring.graphql.exception.AuthenticationException;
import io.spring.graphql.types.ArticlePayload;
import io.spring.graphql.types.CreateArticleInput;
import io.spring.graphql.types.DeletionStatus;
import io.spring.graphql.types.UpdateArticleInput;
import lombok.RequiredArgsConstructor;

import java.util.List;

@DgsComponent
@RequiredArgsConstructor
public class ArticleMutation {

    private final ArticleCommandService articleCommandService;
    private final ArticleFavoriteRepository articleFavoriteRepository;
    private final ArticleRepository articleRepository;


    @DgsData(parentType = MUTATION.TYPE_NAME, field = MUTATION.CreateArticle)
    public DataFetcherResult<ArticlePayload> createArticle(
            @InputArgument("input") CreateArticleInput input
    ) {
        var user = getUserOrThrow();
        var newArticleParam = NewArticleParam.builder()
                .title(input.getTitle())
                .description(input.getDescription())
                .body(input.getBody())
                .tagList(input.getTagList() == null ? List.of() : input.getTagList())
                .build();
        var article = articleCommandService.createArticle(newArticleParam, user);
        return buildDataFetcher(article);
    }


    @DgsData(parentType = MUTATION.TYPE_NAME, field = MUTATION.UpdateArticle)
    public DataFetcherResult<ArticlePayload> updateArticle(
            @InputArgument("slug") String slug,
            @InputArgument("changes") UpdateArticleInput params
    ) {
        var user = getUserOrThrow();
        var article = getArticleOrThrow(slug);
        checkCanWriteArticle(user, article);
        var updateArticleParam = new UpdateArticleParam(params.getTitle(), params.getBody(), params.getDescription());
        article = articleCommandService.updateArticle(article, updateArticleParam);
        return buildDataFetcher(article);
    }


    @DgsData(parentType = MUTATION.TYPE_NAME, field = MUTATION.FavoriteArticle)
    public DataFetcherResult<ArticlePayload> favoriteArticle(
            @InputArgument("slug") String slug
    ) {
        var user = getUserOrThrow();
        var article = getArticleOrThrow(slug);
        var articleFavorite = new ArticleFavorite(article.getId(), user.getId());
        articleFavoriteRepository.save(articleFavorite);
        return buildDataFetcher(article);
    }


    @DgsData(parentType = MUTATION.TYPE_NAME, field = MUTATION.UnfavoriteArticle)
    public DataFetcherResult<ArticlePayload> unfavoriteArticle(
            @InputArgument("slug") String slug
    ) {
        var user = getUserOrThrow();
        var article = getArticleOrThrow(slug);
        articleFavoriteRepository
                .find(article.getId(), user.getId())
                .ifPresent(articleFavoriteRepository::remove);
        return buildDataFetcher(article);
    }


    @DgsData(parentType = MUTATION.TYPE_NAME, field = MUTATION.DeleteArticle)
    public DeletionStatus deleteArticle(
            @InputArgument("slug") String slug
    ) {
        var user = getUserOrThrow();
        var article = getArticleOrThrow(slug);
        checkCanWriteArticle(user, article);
        articleRepository.remove(article);
        return DeletionStatus.newBuilder().success(true).build();
    }


    private Article getArticleOrThrow(String slug) {
        return articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
    }


    private User getUserOrThrow() {
        return SecurityUtils.getCurrentUser().orElseThrow(AuthenticationException::new);
    }


    private void checkCanWriteArticle(User user, Article article) {
        if (!AuthorizationService.canWriteArticle(user, article)) {
            throw new NoAuthorizationException();
        }
    }


    private DataFetcherResult<ArticlePayload> buildDataFetcher(Article article) {
        return DataFetcherResult.<ArticlePayload>newResult()
                .data(ArticlePayload.newBuilder().build())
                .localContext(article)
                .build();
    }

}
