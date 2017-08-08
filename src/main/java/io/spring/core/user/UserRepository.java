package io.spring.core.user;

import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository {
    void save(User user);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);
}
