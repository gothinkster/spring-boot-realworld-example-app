package io.spring.graphql;

import io.spring.core.user.User;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class SecurityUtils {

    public static Optional<User> getCurrentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof AnonymousAuthenticationToken || authentication.getPrincipal() == null) {
            return Optional.empty();
        }
        User currentUser = (User) authentication.getPrincipal();
        return Optional.of(currentUser);
    }

}
