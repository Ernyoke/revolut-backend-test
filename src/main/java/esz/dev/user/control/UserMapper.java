package esz.dev.user.control;

import org.mapstruct.Mapper;
import esz.dev.user.entity.User;

/**
 * Mapper interface used for transforming user entities into data transfer objects.
 */
@Mapper(componentModel = "jsr330")
public interface UserMapper {
    UserDto userToUserDto(User user);
    User userDtoToUser(UserDto userDto);
}
