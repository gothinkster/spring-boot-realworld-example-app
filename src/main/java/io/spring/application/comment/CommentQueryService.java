package io.spring.application.comment;

import io.spring.application.profile.UserRelationshipQueryService;
import io.spring.core.user.User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CommentQueryService {
    private CommentReadService commentReadService;
    private UserRelationshipQueryService userRelationshipQueryService;

    public CommentQueryService(CommentReadService commentReadService, UserRelationshipQueryService userRelationshipQueryService) {
        this.commentReadService = commentReadService;
        this.userRelationshipQueryService = userRelationshipQueryService;
    }

    public Optional<CommentData> findById(String id, User user) {
        CommentData commentData = commentReadService.findById(id);
        if (commentData == null) {
            return Optional.empty();
        } else {
            commentData.getProfileData().setFollowing(
                userRelationshipQueryService.isUserFollowing(
                    user.getId(),
                    commentData.getProfileData().getId()));
        }
        return Optional.ofNullable(commentData);
    }

    public List<CommentData> findByArticleSlug(String slug, User user) {
        return new ArrayList<>();
    }
}
