package io.spring.graphql;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.UserQueryService;
import io.spring.application.data.UserData;
import io.spring.application.data.UserWithToken;
import io.spring.core.service.JwtService;
import io.spring.graphql.DgsConstants.QUERY;
import io.spring.graphql.DgsConstants.USERPAYLOAD;
import io.spring.graphql.types.User;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestHeader;

@DgsComponent
@AllArgsConstructor
public class MeDatafetcher {
  private UserQueryService userQueryService;
  private JwtService jwtService;

  @DgsData(parentType = DgsConstants.QUERY_TYPE, field = QUERY.Me)
  public DataFetcherResult<User> getMe(
      @RequestHeader(value = "Authorization") String authorization,
      DataFetchingEnvironment dataFetchingEnvironment) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication instanceof AnonymousAuthenticationToken
        || authentication.getPrincipal() == null) {
      return null;
    }
    io.spring.core.user.User user = (io.spring.core.user.User) authentication.getPrincipal();
    UserData userData =
        userQueryService.findById(user.getId()).orElseThrow(ResourceNotFoundException::new);
    UserWithToken userWithToken = new UserWithToken(userData, authorization.split(" ")[1]);
    User result =
        User.newBuilder()
            .email(userWithToken.getEmail())
            .username(userWithToken.getUsername())
            .token(userWithToken.getToken())
            .build();
    return DataFetcherResult.<User>newResult().data(result).localContext(user).build();
  }

  @DgsData(parentType = USERPAYLOAD.TYPE_NAME, field = USERPAYLOAD.User)
  public DataFetcherResult<User> getUserPayloadUser(
      DataFetchingEnvironment dataFetchingEnvironment) {
    io.spring.core.user.User user = dataFetchingEnvironment.getLocalContext();
    User result =
        User.newBuilder()
            .email(user.getEmail())
            .username(user.getUsername())
            .token(jwtService.toToken(user))
            .build();
    return DataFetcherResult.<User>newResult().data(result).localContext(user).build();
  }
}
