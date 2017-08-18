package io.spring.infrastructure.service;

import io.spring.core.service.JwtService;
import io.spring.core.user.User;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

public class DefaultJwtServiceTest {

    private JwtService jwtService;

    @Before
    public void setUp() throws Exception {
        jwtService = new DefaultJwtService("123123", 3600);
    }

    @Test
    public void should_generate_and_parse_token() throws Exception {
        User user = new User("email@email.com", "username", "123", "", "");
        String token = jwtService.toToken(user);
        assertThat(token, notNullValue());
        Optional<String> optional = jwtService.getSubFromToken(token);
        assertThat(optional.isPresent(), is(true));
        assertThat(optional.get(), is(user.getId()));
    }

    @Test
    public void should_get_null_with_wrong_jwt() throws Exception {
        Optional<String> optional = jwtService.getSubFromToken("123");
        assertThat(optional.isPresent(), is(false));
    }

    @Test
    public void should_get_null_with_expired_jwt() throws Exception {
        String token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhaXNlbnNpeSIsImV4cCI6MTUwMjE2MTIwNH0.SJB-U60WzxLYNomqLo4G3v3LzFxJKuVrIud8D8Lz3-mgpo9pN1i7C8ikU_jQPJGm8HsC1CquGMI-rSuM7j6LDA";
        assertThat(jwtService.getSubFromToken(token).isPresent(), is(false));
    }
}