package io.spring.infrastructure.user;

import io.spring.core.user.FollowRelation;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.repository.MyBatisUserRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@MybatisTest
@Import(MyBatisUserRepository.class)
public class MyBatisUserRepositoryTest {
    @Autowired
    private UserRepository userRepository;
    private User user;

    @Before
    public void setUp() {
        user = new User("aisensiy@163.com", "aisensiy", "123", "", "default");
    }

    @Test
    public void should_save_and_fetch_user_success() {
        userRepository.save(user);
        Optional<User> userOptional = userRepository.findByUsername("aisensiy");
        assertEquals(userOptional.get(), user);
        Optional<User> userOptional2 = userRepository.findByEmail("aisensiy@163.com");
        assertEquals(userOptional2.get(), user);
    }

    @Test
    public void should_update_user_success() {
        String newEmail = "newemail@email.com";
        user.update(newEmail, "", "", "", "");
        userRepository.save(user);
        Optional<User> optional = userRepository.findByUsername(user.getUsername());
        assertTrue(optional.isPresent());
        assertEquals(optional.get().getEmail(), newEmail);

        String newUsername = "newUsername";
        user.update("", newUsername, "", "", "");
        userRepository.save(user);
        optional = userRepository.findByEmail(user.getEmail());
        assertTrue(optional.isPresent());
        assertEquals(optional.get().getUsername(), newUsername);
        assertEquals(optional.get().getImage(), user.getImage());
    }

    @Test
    public void should_create_new_user_follow_success() {
        User other = new User("other@example.com", "other", "123", "", "");
        userRepository.save(other);

        FollowRelation followRelation = new FollowRelation(user.getId(), other.getId());
        userRepository.saveRelation(followRelation);
        assertTrue(userRepository.findRelation(user.getId(), other.getId()).isPresent());
    }

    @Test
    public void should_unfollow_user_success() {
        User other = new User("other@example.com", "other", "123", "", "");
        userRepository.save(other);

        FollowRelation followRelation = new FollowRelation(user.getId(), other.getId());
        userRepository.saveRelation(followRelation);

        userRepository.removeRelation(followRelation);
        assertFalse(userRepository.findRelation(user.getId(), other.getId()).isPresent());
    }
}