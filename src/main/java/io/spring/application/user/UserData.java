package io.spring.application.user;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@JsonRootName("user")
public class UserData {
    private String email;
    @Id
    private String username;
    private String bio;
    private String image;
}
