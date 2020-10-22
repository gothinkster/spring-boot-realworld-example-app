package io.spring.application.profile;

import io.spring.application.ProfileQueryService;
import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.repository.MyBatisUserRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@MybatisTest
@Import({ProfileQueryService.class, MyBatisUserRepository.class})
public class ProfileQueryServiceTest {
    @Autowired
    private ProfileQueryService profileQueryService;
    @Autowired
    private UserRepository userRepository;

    @Test
    public void should_fetch_profile_success() {
        User currentUser = new User("a@test.com", "a", "123", "", "");
        User profileUser = new User("p@test.com", "p", "123", "", "");
        userRepository.save(profileUser);

        Optional<ProfileData> optional = profileQueryService.findByUsername(profileUser.getUsername(), currentUser);
        assertTrue(optional.isPresent());
    }
}