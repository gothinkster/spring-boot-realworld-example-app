package io.spring.application;

import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.data.ProfileData;
import io.spring.core.user.FollowRelation;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final ProfileQueryService profileQueryService;

    public ProfileData follow(String username, User currentUser) {

        User target =
                userRepository.findByUsername(username)
                        .orElseThrow(ResourceNotFoundException::new);

        FollowRelation relation =
                new FollowRelation(currentUser.getId(), target.getId());

        userRepository.saveRelation(relation);

        return profileQueryService
                .findByUsername(username, currentUser)
                .orElseThrow(ResourceNotFoundException::new);
    }


    public ProfileData unfollow(String username, User currentUser) {

        User target = userRepository.findByUsername(username)
                .orElseThrow(ResourceNotFoundException::new);

        FollowRelation relation = userRepository
                .findRelation(currentUser.getId(), target.getId())
                .orElseThrow(ResourceNotFoundException::new);

        userRepository.removeRelation(relation);

        return profileQueryService
                .findByUsername(username, currentUser)
                .orElseThrow(ResourceNotFoundException::new);
    }
}