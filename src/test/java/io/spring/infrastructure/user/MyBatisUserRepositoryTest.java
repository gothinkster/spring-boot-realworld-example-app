package io.spring.infrastructure.user;

import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@MybatisTest
@Import(MyBatisUserRepository.class)
public class MyBatisUserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Test
    public void should_save_and_fetch_user_success() throws Exception {
        User user = new User("aisensiy@163.com", "aisensiy", "123", "", "default");
        userRepository.save(user);
        Optional<User> userOptional = userRepository.findByUsername("aisensiy");
        assertThat(userOptional.get(), is(user));
        Optional<User> userOptional2 = userRepository.findByEmail("aisensiy@163.com");
        assertThat(userOptional2.get(), is(user));
    }
}