package io.spring.application.user;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonRootName("user")
public class UserData {
    private String id;
    private String email;
    private String username;
    private String bio;
    private String image;
}
