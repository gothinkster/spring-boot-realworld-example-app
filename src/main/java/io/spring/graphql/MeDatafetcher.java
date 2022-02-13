package io.spring.graphql;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.UserQueryService;
import io.spring.application.data.UserWithToken;
import io.spring.core.service.JwtService;
import io.spring.graphql.DgsConstants.QUERY;
import io.spring.graphql.DgsConstants.USERPAYLOAD;
import io.spring.graphql.types.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestHeader;

@DgsComponent
@RequiredArgsConstructor
public class MeDatafetcher {

    private final UserQueryService userQueryService;
    private final JwtService jwtService;


    @DgsData(parentType = DgsConstants.QUERY_TYPE, field = QUERY.Me)
    public DataFetcherResult<User> getMe(
            @RequestHeader(value = "Authorization") String authorization,
            DataFetchingEnvironment dataFetchingEnvironment
    ) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof AnonymousAuthenticationToken
                || authentication.getPrincipal() == null) {
            return null;
        }
        var user = (io.spring.core.user.User) authentication.getPrincipal();
        var userData = userQueryService.findById(user.getId()).orElseThrow(ResourceNotFoundException::new);
        var userWithToken = new UserWithToken(userData, authorization.split(" ")[1]);
        var result = User.newBuilder()
                .email(userWithToken.getEmail())
                .username(userWithToken.getUsername())
                .token(userWithToken.getToken())
                .build();
        return getDataFetcherResult(result, user);
    }


    @DgsData(parentType = USERPAYLOAD.TYPE_NAME, field = USERPAYLOAD.User)
    public DataFetcherResult<User> getUserPayloadUser(
            DataFetchingEnvironment dataFetchingEnvironment
    ) {
        var user = dataFetchingEnvironment.<io.spring.core.user.User>getLocalContext();
        var result = User.newBuilder()
                .email(user.getEmail())
                .username(user.getUsername())
                .token(jwtService.toToken(user))
                .build();
        return getDataFetcherResult(result, user);
    }


    private DataFetcherResult<User> getDataFetcherResult(User result, io.spring.core.user.User user) {
        return DataFetcherResult.<User>newResult().data(result).localContext(user).build();
    }

}
