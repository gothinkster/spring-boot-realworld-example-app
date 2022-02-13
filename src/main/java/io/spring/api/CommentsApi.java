package io.spring.api;

import com.fasterxml.jackson.annotation.JsonRootName;
import io.spring.api.exception.NoAuthorizationException;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.CommentQueryService;
import io.spring.application.data.CommentData;
import io.spring.core.article.ArticleRepository;
import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import io.spring.core.service.AuthorizationService;
import io.spring.core.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.Map;

@RestController
@RequestMapping(path = "/articles/{slug}/comments")
@RequiredArgsConstructor
public class CommentsApi {

    private final ArticleRepository articleRepository;
    private final CommentRepository commentRepository;
    private final CommentQueryService commentQueryService;


    @PostMapping
    public ResponseEntity<?> createComment(
            @PathVariable("slug") String slug,
            @AuthenticationPrincipal User user,
            @Valid @RequestBody NewCommentRequestDto newCommentRequestDto
    ) {
        var article = articleRepository
                .findBySlug(slug)
                .orElseThrow(ResourceNotFoundException::new);
        var comment = new Comment(newCommentRequestDto.getBody(), user.getId(), article.getId());
        commentRepository.save(comment);
        return ResponseEntity
                .status(201)
                .body(commentResponse(commentQueryService.findById(comment.getId(), user).get()));
    }


    @GetMapping
    public ResponseEntity<?> getComments(
            @PathVariable("slug") String slug,
            @AuthenticationPrincipal User user
    ) {
        var article = articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
        var comments = commentQueryService.findByArticleId(article.getId(), user);
        return ResponseEntity.ok(Map.of("comments", comments));
    }


    @DeleteMapping(path = "{id}")
    public ResponseEntity<?> deleteComment(
            @PathVariable("slug") String slug,
            @PathVariable("id") String commentId,
            @AuthenticationPrincipal User user
    ) {
        var article = articleRepository
                .findBySlug(slug)
                .orElseThrow(ResourceNotFoundException::new);
        return commentRepository
                .findById(article.getId(), commentId)
                .map(comment -> {
                    if (!AuthorizationService.canWriteComment(user, article, comment)) {
                        throw new NoAuthorizationException();
                    }
                    commentRepository.remove(comment);
                    return ResponseEntity.noContent().build();
                })
                .orElseThrow(ResourceNotFoundException::new);
    }


    private Map<String, CommentData> commentResponse(CommentData commentData) {
        return Map.of("comment", commentData);
    }

}

@Getter
@NoArgsConstructor
@JsonRootName("comment")
class NewCommentRequestDto {

    @NotBlank(message = "can't be empty")
    private String body;

}
