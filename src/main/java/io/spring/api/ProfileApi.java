package io.spring.api;

import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ProfileQueryService;
import io.spring.application.data.ProfileData;
import io.spring.core.user.FollowRelation;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping(path = "profiles/{username}")
@RequiredArgsConstructor
public class ProfileApi {

    private final ProfileQueryService profileQueryService;
    private final UserRepository userRepository;


    @GetMapping
    public ResponseEntity<?> getProfile(
            @PathVariable("username") String username,
            @AuthenticationPrincipal User user
    ) {
        return profileQueryService.findByUsername(username, user)
                .map(this::profileResponse)
                .orElseThrow(ResourceNotFoundException::new);
    }

    @PostMapping(path = "follow")
    public ResponseEntity<?> follow(
            @PathVariable("username") String username,
            @AuthenticationPrincipal User user
    ) {
        return userRepository.findByUsername(username)
                .map(target -> {
                    FollowRelation followRelation = new FollowRelation(user.getId(), target.getId());
                    userRepository.saveRelation(followRelation);
                    return profileResponse(profileQueryService.findByUsername(username, user).get());
                })
                .orElseThrow(ResourceNotFoundException::new);
    }


    @DeleteMapping(path = "follow")
    public ResponseEntity<?> unfollow(
            @PathVariable("username") String username,
            @AuthenticationPrincipal User user
    ) {
        var userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            var target = userOptional.get();
            return userRepository.findRelation(user.getId(), target.getId())
                    .map(relation -> {
                        userRepository.removeRelation(relation);
                        return profileResponse(profileQueryService.findByUsername(username, user).get());
                    })
                    .orElseThrow(ResourceNotFoundException::new);
        } else {
            throw new ResourceNotFoundException();
        }
    }


    private ResponseEntity<?> profileResponse(ProfileData profile) {
        return ResponseEntity.ok(Map.of("profile", profile));
    }

}
