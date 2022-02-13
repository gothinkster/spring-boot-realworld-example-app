package io.spring.core.user;

import io.spring.Utils;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@EqualsAndHashCode(of = {"id"})
public class User {

    private String id;
    private String email;
    private String username;
    private String password;
    private String bio;
    private String image;

    public User(String email, String username, String password, String bio, String image) {
        this.id = UUID.randomUUID().toString();
        this.email = email;
        this.username = username;
        this.password = password;
        this.bio = bio;
        this.image = image;
    }

    public void update(String email, String username, String password, String bio, String image) {
        if (!Utils.isEmpty(email)) {
            this.email = email;
        }

        if (!Utils.isEmpty(username)) {
            this.username = username;
        }

        if (!Utils.isEmpty(password)) {
            this.password = password;
        }

        if (!Utils.isEmpty(bio)) {
            this.bio = bio;
        }

        if (!Utils.isEmpty(image)) {
            this.image = image;
        }
    }

}
