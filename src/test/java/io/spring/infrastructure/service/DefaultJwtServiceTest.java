package io.spring.infrastructure.service;

import io.spring.application.JwtService;
import io.spring.application.user.UserData;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.*;

public class DefaultJwtServiceTest {

    private JwtService jwtService;

    @Before
    public void setUp() throws Exception {
        jwtService = new DefaultJwtService("123123", 3600);
    }

    @Test
    public void should_generate_and_parse_token() throws Exception {
        String username = "aisensiy";

        UserData userData = new UserData("123", "aisensiy@163.com", username, "", "");
        String token = jwtService.toToken(userData);
        assertThat(token, notNullValue());
        Optional<String> optional = jwtService.getSubFromToken(token);
        assertThat(optional.isPresent(), is(true));
        assertThat(optional.get(), is(username));
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