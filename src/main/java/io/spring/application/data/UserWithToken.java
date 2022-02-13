package io.spring.application.data;

import lombok.Getter;

@Getter
public class UserWithToken {

    private final String email;
    private final String username;
    private final String bio;
    private final String image;
    private final String token;

    public UserWithToken(UserData userData, String token) {
        this.email = userData.getEmail();
        this.username = userData.getUsername();
        this.bio = userData.getBio();
        this.image = userData.getImage();
        this.token = token;
    }

}
