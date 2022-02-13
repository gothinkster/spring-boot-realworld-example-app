package io.spring.graphql;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.InputArgument;
import graphql.execution.DataFetcherResult;
import io.spring.api.exception.NoAuthorizationException;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.CommentQueryService;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import io.spring.core.service.AuthorizationService;
import io.spring.core.user.User;
import io.spring.graphql.DgsConstants.MUTATION;
import io.spring.graphql.exception.AuthenticationException;
import io.spring.graphql.types.CommentPayload;
import io.spring.graphql.types.DeletionStatus;
import lombok.RequiredArgsConstructor;

@DgsComponent
@RequiredArgsConstructor
public class CommentMutation {

    private final ArticleRepository articleRepository;
    private final CommentRepository commentRepository;
    private final CommentQueryService commentQueryService;


    @DgsData(parentType = MUTATION.TYPE_NAME, field = MUTATION.AddComment)
    public DataFetcherResult<CommentPayload> createComment(
            @InputArgument("slug") String slug,
            @InputArgument("body") String body
    ) {
        var user = getUser();
        var article = getArticle(slug);
        var comment = new Comment(body, user.getId(), article.getId());
        commentRepository.save(comment);
        var commentData = commentQueryService
                .findById(comment.getId(), user)
                .orElseThrow(ResourceNotFoundException::new);
        return DataFetcherResult.<CommentPayload>newResult()
                .localContext(commentData)
                .data(CommentPayload.newBuilder().build())
                .build();
    }


    @DgsData(parentType = MUTATION.TYPE_NAME, field = MUTATION.DeleteComment)
    public DeletionStatus removeComment(
            @InputArgument("slug") String slug,
            @InputArgument("id") String commentId
    ) {
        var user = getUser();
        var article = getArticle(slug);
        return commentRepository
                .findById(article.getId(), commentId)
                .map(comment -> {
                    if (!AuthorizationService.canWriteComment(user, article, comment)) {
                        throw new NoAuthorizationException();
                    }
                    commentRepository.remove(comment);
                    return DeletionStatus.newBuilder().success(true).build();
                })
                .orElseThrow(ResourceNotFoundException::new);
    }


    private Article getArticle(String slug) {
        return articleRepository.findBySlug(slug)
                .orElseThrow(ResourceNotFoundException::new);
    }

    private User getUser() {
        return SecurityUtils.getCurrentUser().orElseThrow(AuthenticationException::new);
    }

}
