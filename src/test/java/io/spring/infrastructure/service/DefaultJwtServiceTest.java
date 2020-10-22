package io.spring.infrastructure.service;

import io.spring.core.service.JwtService;
import io.spring.core.user.User;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DefaultJwtServiceTest {

    private JwtService jwtService;

    @Before
    public void setUp() {
        jwtService = new DefaultJwtService("123123", 3600);
    }

    @Test
    public void should_generate_and_parse_token() {
        User user = new User("email@email.com", "username", "123", "", "");
        String token = jwtService.toToken(user);
        assertNotNull(token);
        Optional<String> optional = jwtService.getSubFromToken(token);
        assertTrue(optional.isPresent());
        assertEquals(optional.get(), user.getId());
    }

    @Test
    public void should_get_null_with_wrong_jwt() {
        Optional<String> optional = jwtService.getSubFromToken("123");
        assertFalse(optional.isPresent());
    }

    @Test
    public void should_get_null_with_expired_jwt() {
        String token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhaXNlbnNpeSIsImV4cCI6MTUwMjE2MTIwNH0.SJB-U60WzxLYNomqLo4G3v3LzFxJKuVrIud8D8Lz3-mgpo9pN1i7C8ikU_jQPJGm8HsC1CquGMI-rSuM7j6LDA";
        assertFalse(jwtService.getSubFromToken(token).isPresent());
    }
}