package esz.dev;

import esz.dev.common.ResponseDto;
import esz.dev.account.boundary.AccountResource;
import esz.dev.account.boundary.InvalidInputException;
import esz.dev.account.control.AccountNotFoundException;
import esz.dev.account.control.NotEnoughAmountException;
import esz.dev.user.boundary.UserResource;
import esz.dev.user.config.UserMapperConfig;
import esz.dev.user.control.UserNotFoundException;
import io.jooby.Jooby;
import io.jooby.StatusCode;
import io.jooby.di.GuiceModule;
import io.jooby.json.JacksonModule;

/**
 * Main-entry point of the application. Handles global module instantiation and transforming general exceptions into
 * REST responses.
 */
public class App extends Jooby {
    {
        install(new JacksonModule());
        install(new GuiceModule(new UserMapperConfig()));

        mvc(UserResource.class);
        mvc(AccountResource.class);

        error(UserNotFoundException.class, ((context, cause, statusCode) -> {
            context.setResponseCode(StatusCode.NOT_FOUND);
            context.render(new ResponseDto(ResponseDto.Status.FAILURE, cause.getMessage()));
        }));

        error(NotEnoughAmountException.class, ((context, cause, statusCode) -> {
            context.setResponseCode(StatusCode.BAD_REQUEST);
            context.render(new ResponseDto(ResponseDto.Status.FAILURE, cause.getMessage()));
        }));

        error(AccountNotFoundException.class, ((context, cause, statusCode) -> {
            context.setResponseCode(StatusCode.NOT_FOUND);
            context.render(new ResponseDto(ResponseDto.Status.FAILURE, cause.getMessage()));
        }));

        error(InvalidInputException.class, ((context, cause, statusCode) -> {
            context.setResponseCode(StatusCode.BAD_REQUEST);
            context.render(new ResponseDto(ResponseDto.Status.FAILURE, cause.getMessage()));
        }));
    }

    public static void main(String[] args) {
        runApp(args, App::new);
    }
}
