package esz.dev.user.boundary;

import esz.dev.common.ResponseDto;
import esz.dev.user.control.UserDto;
import esz.dev.user.control.UserNotFoundException;
import esz.dev.user.control.UserService;
import io.jooby.Context;
import io.jooby.StatusCode;
import io.jooby.annotations.GET;
import io.jooby.annotations.POST;
import io.jooby.annotations.Path;
import io.jooby.annotations.PathParam;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Set;

/**
 * REST end-point implementation for user management.
 */
@Singleton
@Path("/api/user")
public class UserResource {
    private final UserService userService;

    @Inject
    public UserResource(UserService userService) {
        this.userService = userService;
    }

    @GET("/{id}")
    public UserDto getUser(@PathParam Long id) throws UserNotFoundException {
        return userService.getUser(id);
    }

    @GET("/{id}/accounts")
    public Set<String> getAccountsForUser(@PathParam Long id) throws UserNotFoundException {
        return userService.getAccountsForUser(id);
    }

    @POST
    public ResponseDto addUser(UserDto userDto, Context context) {
        long id = userService.addUser(userDto);
        context.setResponseCode(StatusCode.CREATED);
        return ResponseDto.builder().status(ResponseDto.Status.SUCCESS).message("Successfully created user with id " + id).build();
    }
}
