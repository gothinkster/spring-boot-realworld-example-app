package io.spring.application;

import io.spring.application.data.CommentData;
import io.spring.core.user.User;
import io.spring.infrastructure.mybatis.readservice.CommentReadService;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import lombok.RequiredArgsConstructor;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentQueryService {

    private final CommentReadService commentReadService;
    private final UserRelationshipQueryService userRelationshipQueryService;


    public Optional<CommentData> findById(String id, User user) {
        var commentData = commentReadService.findById(id);
        if (commentData == null) {
            return Optional.empty();
        }
        var userFollowing = userRelationshipQueryService.isUserFollowing(
                user.getId(),
                commentData.getProfileData().getId()
        );
        commentData.getProfileData().setFollowing(userFollowing);
        return Optional.of(commentData);
    }


    public List<CommentData> findByArticleId(String articleId, User user) {
        var comments = commentReadService.findByArticleId(articleId);
        if (comments.size() > 0 && user != null) {
            var ids = getIds(comments);
            var followingAuthors = userRelationshipQueryService.followingAuthors(user.getId(), ids);
            comments.forEach(commentData -> {
                if (followingAuthors.contains(commentData.getProfileData().getId())) {
                    commentData.getProfileData().setFollowing(true);
                }
            });
        }
        return comments;
    }


    public CursorPager<CommentData> findByArticleIdWithCursor(
            String articleId,
            User user,
            CursorPageParameter<DateTime> page
    ) {
        var comments = commentReadService.findByArticleIdWithCursor(articleId, page);
        if (comments.isEmpty()) {
            return new CursorPager<>(new ArrayList<>(), page.getDirection(), false);
        }
        if (user != null) {
            var ids = getIds(comments);
            var followingAuthors = userRelationshipQueryService.followingAuthors(user.getId(), ids);
            comments.forEach(commentData -> {
                if (followingAuthors.contains(commentData.getProfileData().getId())) {
                    commentData.getProfileData().setFollowing(true);
                }
            });
        }
        var hasExtra = comments.size() > page.getLimit();
        if (hasExtra) {
            comments.remove(page.getLimit());
        }
        if (!page.isNext()) {
            Collections.reverse(comments);
        }
        return new CursorPager<>(comments, page.getDirection(), hasExtra);
    }


    private List<String> getIds(List<CommentData> comments) {
        return comments.stream()
                .map(commentData -> commentData.getProfileData().getId())
                .toList();
    }

}
