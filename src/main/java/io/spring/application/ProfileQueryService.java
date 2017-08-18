package io.spring.application;

import io.spring.application.data.ProfileData;
import io.spring.application.data.UserData;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import io.spring.core.user.User;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ProfileQueryService {
    private UserReadService userReadService;
    private UserRelationshipQueryService userRelationshipQueryService;

    @Autowired
    public ProfileQueryService(UserReadService userReadService, UserRelationshipQueryService userRelationshipQueryService) {
        this.userReadService = userReadService;
        this.userRelationshipQueryService = userRelationshipQueryService;
    }

    public Optional<ProfileData> findByUsername(String username, User currentUser) {
        UserData userData = userReadService.findByUsername(username);
        if (userData == null) {
            return Optional.empty();
        } else {
            ProfileData profileData = new ProfileData(
                userData.getId(),
                userData.getUsername(),
                userData.getBio(),
                userData.getImage(),
                userRelationshipQueryService.isUserFollowing(currentUser.getId(), userData.getId()));
            return Optional.of(profileData);
        }
    }
}
