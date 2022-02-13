package io.spring.graphql;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.InputArgument;
import graphql.execution.DataFetcherResult;
import io.spring.api.exception.InvalidAuthenticationException;
import io.spring.application.user.RegisterParam;
import io.spring.application.user.UpdateUserCommand;
import io.spring.application.user.UpdateUserParam;
import io.spring.application.user.UserService;
import io.spring.core.user.EncryptService;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.graphql.DgsConstants.MUTATION;
import io.spring.graphql.exception.GraphQLCustomizeExceptionHandler;
import io.spring.graphql.types.CreateUserInput;
import io.spring.graphql.types.UpdateUserInput;
import io.spring.graphql.types.UserPayload;
import io.spring.graphql.types.UserResult;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.validation.ConstraintViolationException;

@DgsComponent
@RequiredArgsConstructor
public class UserMutation {

    private final UserRepository userRepository;
    private final EncryptService encryptService;
    private final UserService userService;


    @DgsData(parentType = MUTATION.TYPE_NAME, field = MUTATION.CreateUser)
    public DataFetcherResult<UserResult> createUser(
            @InputArgument("input") CreateUserInput input
    ) {
        var registerParam = new RegisterParam(input.getEmail(), input.getUsername(), input.getPassword());
        try {
            var user = userService.createUser(registerParam);
            return DataFetcherResult
                    .<UserResult>newResult()
                    .data(UserPayload.newBuilder().build())
                    .localContext(user)
                    .build();
        } catch (ConstraintViolationException cve) {
            return DataFetcherResult
                    .<UserResult>newResult()
                    .data(GraphQLCustomizeExceptionHandler.getErrorsAsData(cve))
                    .build();
        }
    }


    @DgsData(parentType = MUTATION.TYPE_NAME, field = MUTATION.Login)
    public DataFetcherResult<UserPayload> login(
            @InputArgument("password") String password,
            @InputArgument("email") String email
    ) {
        var optional = userRepository.findByEmail(email);
        if (optional.isPresent() && encryptService.check(password, optional.get().getPassword())) {
            return DataFetcherResult.<UserPayload>newResult()
                    .data(UserPayload.newBuilder().build())
                    .localContext(optional.get())
                    .build();
        } else {
            throw new InvalidAuthenticationException();
        }
    }


    @DgsData(parentType = MUTATION.TYPE_NAME, field = MUTATION.UpdateUser)
    public DataFetcherResult<UserPayload> updateUser(
            @InputArgument("changes") UpdateUserInput updateUserInput
    ) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof AnonymousAuthenticationToken
                || authentication.getPrincipal() == null) {
            return null;
        }
        var currentUser = (User) authentication.getPrincipal();
        var param = UpdateUserParam.builder()
                .username(updateUserInput.getUsername())
                .email(updateUserInput.getEmail())
                .bio(updateUserInput.getBio())
                .password(updateUserInput.getPassword())
                .image(updateUserInput.getImage())
                .build();

        userService.updateUser(new UpdateUserCommand(currentUser, param));
        return DataFetcherResult.<UserPayload>newResult()
                .data(UserPayload.newBuilder().build())
                .localContext(currentUser)
                .build();
    }

}
