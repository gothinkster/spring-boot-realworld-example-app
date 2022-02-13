package io.spring.graphql;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.InputArgument;
import graphql.schema.DataFetchingEnvironment;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ProfileQueryService;
import io.spring.application.data.ArticleData;
import io.spring.application.data.CommentData;
import io.spring.core.user.User;
import io.spring.graphql.DgsConstants.ARTICLE;
import io.spring.graphql.DgsConstants.COMMENT;
import io.spring.graphql.DgsConstants.QUERY;
import io.spring.graphql.DgsConstants.USER;
import io.spring.graphql.types.Article;
import io.spring.graphql.types.Comment;
import io.spring.graphql.types.Profile;
import io.spring.graphql.types.ProfilePayload;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@DgsComponent
@RequiredArgsConstructor
public class ProfileDatafetcher {

    private final ProfileQueryService profileQueryService;


    @DgsData(parentType = USER.TYPE_NAME, field = USER.Profile)
    public Profile getUserProfile(DataFetchingEnvironment dataFetchingEnvironment) {
        var user = dataFetchingEnvironment.<User>getLocalContext();
        return queryProfile(user.getUsername());
    }


    @DgsData(parentType = ARTICLE.TYPE_NAME, field = ARTICLE.Author)
    public Profile getAuthor(DataFetchingEnvironment dataFetchingEnvironment) {
        var map = dataFetchingEnvironment.<Map<String, ArticleData>>getLocalContext();
        var article = dataFetchingEnvironment.<Article>getSource();
        return queryProfile(map.get(article.getSlug()).getProfileData().getUsername());
    }


    @DgsData(parentType = COMMENT.TYPE_NAME, field = COMMENT.Author)
    public Profile getCommentAuthor(DataFetchingEnvironment dataFetchingEnvironment) {
        var comment = dataFetchingEnvironment.<Comment>getSource();
        var map = dataFetchingEnvironment.<Map<String, CommentData>>getLocalContext();
        return queryProfile(map.get(comment.getId()).getProfileData().getUsername());
    }


    @DgsData(parentType = DgsConstants.QUERY_TYPE, field = QUERY.Profile)
    public ProfilePayload queryProfile(
            @InputArgument("username") String username,
            DataFetchingEnvironment dataFetchingEnvironment
    ) {
        var profile = queryProfile(dataFetchingEnvironment.getArgument("username"));
        return ProfilePayload.newBuilder()
                .profile(profile)
                .build();
    }


    private Profile queryProfile(String username) {
        var current = SecurityUtils.getCurrentUser().orElse(null);
        var profileData = profileQueryService
                .findByUsername(username, current)
                .orElseThrow(ResourceNotFoundException::new);
        return Profile.newBuilder()
                .username(profileData.getUsername())
                .bio(profileData.getBio())
                .image(profileData.getImage())
                .following(profileData.isFollowing())
                .build();
    }

}
