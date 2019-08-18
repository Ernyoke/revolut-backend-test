package esz.dev.user.control;

import esz.dev.user.entity.User;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Set;

/**
 * Implementation of the business logic in order to handle user management.
 */
@Singleton
public class UserService {
    private final UserMapper userMapper;
    private final UserStore userStore;

    @Inject
    public UserService(UserMapper userMapper, UserStore userStore) {
        this.userMapper = userMapper;
        this.userStore = userStore;
    }

    public long addUser(UserDto userDto) {
        User user = userMapper.userDtoToUser(userDto);
        return userStore.addUser(user.withAccounts(new HashSet<>()));
    }

    public UserDto getUser(Long id) throws UserNotFoundException {
        return userStore.getUser(id)
                .map(userMapper::userToUserDto)
                .orElseThrow(() -> new UserNotFoundException("No user found with id of " + id));
    }

    public Set<String> getAccountsForUser(Long id) throws UserNotFoundException {
        return userStore.getUser(id)
                .map(User::getAccounts)
                .orElseThrow(() -> new UserNotFoundException("No user found with id of " + id));
    }
}
