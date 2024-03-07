package io.spring.infrastructure.user;

import io.spring.core.user.FollowRelation;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.DbTestBase;
import io.spring.infrastructure.repository.MyBatisUserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import(MyBatisUserRepository.class)
public class MyBatisUserRepositoryTest extends DbTestBase {
  @Autowired private UserRepository userRepository;
  private User user;

  @BeforeEach
  public void setUp() {
    user = new User("aisensiy@163.com", "aisensiy", "123", "", "default");
  }

  @Test
  public void should_return_users() {
    List<User> usersList = new ArrayList<>(List.of(
            new User("john@163.com", "john", "123", "", "default"),
            new User("adam@163.com", "adam", "123", "", "default")));

    userRepository.save(new User("john@163.com", "john", "123", "", "default"));
    userRepository.save(new User("adam@163.com", "adam", "123", "", "default"));

    Assertions.assertEquals(userRepository.findAll().get(0).getEmail(), (usersList.get(0).getEmail()));
    Assertions.assertEquals(userRepository.findAll().get(0).getUsername(), (usersList.get(0).getUsername()));
    Assertions.assertEquals(userRepository.findAll().get(1).getEmail(), (usersList.get(1).getEmail()));
    Assertions.assertEquals(userRepository.findAll().get(1).getUsername(), (usersList.get(1).getUsername()));
  }@Test
  public void should_save_and_fetch_user_success() {
    userRepository.save(user);
    Optional<User> userOptional = userRepository.findByUsername("aisensiy");
    Assertions.assertEquals(userOptional.get(), user);
    Optional<User> userOptional2 = userRepository.findByEmail("aisensiy@163.com");
    Assertions.assertEquals(userOptional2.get(), user);
  }

  @Test
  public void should_update_user_success() {
    String newEmail = "newemail@email.com";
    user.update(newEmail, "", "", "", "");
    userRepository.save(user);
    Optional<User> optional = userRepository.findByUsername(user.getUsername());
    Assertions.assertTrue(optional.isPresent());
    Assertions.assertEquals(optional.get().getEmail(), newEmail);

    String newUsername = "newUsername";
    user.update("", newUsername, "", "", "");
    userRepository.save(user);
    optional = userRepository.findByEmail(user.getEmail());
    Assertions.assertTrue(optional.isPresent());
    Assertions.assertEquals(optional.get().getUsername(), newUsername);
    Assertions.assertEquals(optional.get().getImage(), user.getImage());
  }

  @Test
  public void should_create_new_user_follow_success() {
    User other = new User("other@example.com", "other", "123", "", "");
    userRepository.save(other);

    FollowRelation followRelation = new FollowRelation(user.getId(), other.getId());
    userRepository.saveRelation(followRelation);
    Assertions.assertTrue(userRepository.findRelation(user.getId(), other.getId()).isPresent());
  }

  @Test
  public void should_unfollow_user_success() {
    User other = new User("other@example.com", "other", "123", "", "");
    userRepository.save(other);

    FollowRelation followRelation = new FollowRelation(user.getId(), other.getId());
    userRepository.saveRelation(followRelation);

    userRepository.removeRelation(followRelation);
    Assertions.assertFalse(userRepository.findRelation(user.getId(), other.getId()).isPresent());
  }
}
