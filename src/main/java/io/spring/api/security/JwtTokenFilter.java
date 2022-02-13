package io.spring.api.security;

import io.spring.core.service.JwtService;
import io.spring.core.user.UserRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

import static java.util.Collections.emptyList;

@SuppressWarnings("SpringJavaAutowiringInspection")
public class JwtTokenFilter extends OncePerRequestFilter {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    private final String header = "Authorization";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            @NotNull HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        getTokenString(request.getHeader(header))
                .flatMap(token -> jwtService.getSubFromToken(token))
                .ifPresent(id -> {
                    var context = SecurityContextHolder.getContext();
                    if (context.getAuthentication() == null) {
                        userRepository
                                .findById(id)
                                .ifPresent(user -> {
                                    var authToken = new UsernamePasswordAuthenticationToken(user, null, emptyList());
                                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                                    context.setAuthentication(authToken);
                                });
                    }
                });
        filterChain.doFilter(request, response);
    }


    private Optional<String> getTokenString(String header) {
        if (header == null) {
            return Optional.empty();
        }
        var split = header.split(" ");
        return split.length < 2 ?
                Optional.empty() :
                Optional.ofNullable(split[1]);
    }

}

