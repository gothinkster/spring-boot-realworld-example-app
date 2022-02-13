package io.spring.application;

import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProfileQueryService {

    private final UserReadService userReadService;
    private final UserRelationshipQueryService userRelationshipQueryService;


    public Optional<ProfileData> findByUsername(String username, User currentUser) {
        var userData = userReadService.findByUsername(username);
        if (userData == null) {
            return Optional.empty();
        }
        boolean following = currentUser != null &&
                userRelationshipQueryService.isUserFollowing(currentUser.getId(), userData.getId());
        var profileData = new ProfileData(
                userData.getId(),
                userData.getUsername(),
                userData.getBio(),
                userData.getImage(),
                following
        );
        return Optional.of(profileData);
    }

}
