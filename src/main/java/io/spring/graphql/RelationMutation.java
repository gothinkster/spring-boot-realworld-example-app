package io.spring.graphql;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.InputArgument;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ProfileQueryService;
import io.spring.core.user.FollowRelation;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.graphql.DgsConstants.MUTATION;
import io.spring.graphql.exception.AuthenticationException;
import io.spring.graphql.types.Profile;
import io.spring.graphql.types.ProfilePayload;
import lombok.RequiredArgsConstructor;

@DgsComponent
@RequiredArgsConstructor
public class RelationMutation {

    private final UserRepository userRepository;
    private final ProfileQueryService profileQueryService;


    @DgsData(parentType = MUTATION.TYPE_NAME, field = MUTATION.FollowUser)
    public ProfilePayload follow(@InputArgument("username") String username) {
        User user = SecurityUtils.getCurrentUser().orElseThrow(AuthenticationException::new);
        return userRepository
                .findByUsername(username)
                .map(target -> {
                    FollowRelation followRelation = new FollowRelation(user.getId(), target.getId());
                    userRepository.saveRelation(followRelation);
                    Profile profile = buildProfile(username, user);
                    return ProfilePayload.newBuilder().profile(profile).build();
                })
                .orElseThrow(ResourceNotFoundException::new);
    }

    @DgsData(parentType = MUTATION.TYPE_NAME, field = MUTATION.UnfollowUser)
    public ProfilePayload unfollow(
            @InputArgument("username") String username
    ) {
        var user = SecurityUtils.getCurrentUser().orElseThrow(AuthenticationException::new);
        var target = userRepository.findByUsername(username).orElseThrow(ResourceNotFoundException::new);
        return userRepository
                .findRelation(user.getId(), target.getId())
                .map(relation -> {
                    userRepository.removeRelation(relation);
                    var profile = buildProfile(username, user);
                    return ProfilePayload.newBuilder().profile(profile).build();
                })
                .orElseThrow(ResourceNotFoundException::new);
    }

    private Profile buildProfile(
            @InputArgument("username") String username,
            User current
    ) {
        var profileData = profileQueryService.findByUsername(username, current).get();
        return Profile.newBuilder()
                .username(profileData.getUsername())
                .bio(profileData.getBio())
                .image(profileData.getImage())
                .following(profileData.isFollowing())
                .build();
    }

}
