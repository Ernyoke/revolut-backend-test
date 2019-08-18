package unit.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import esz.dev.user.control.*;
import esz.dev.user.entity.User;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Unit tests for business logic which handles users.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserStore userStore;

    @Spy
    private UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("Should return an existing user")
    void getExistingUserTest() throws UserNotFoundException {
        long id = 1L;
        User user = User.builder()
                .id(id)
                .firstName("fname")
                .lastName("lname")
                .address("addr")
                .build();
        when(userStore.getUser(id)).thenReturn(Optional.of(user));

        UserDto userDto = userService.getUser(id);

        assertThat(userDto.getFirstName()).isEqualTo(user.getFirstName());
        assertThat(userDto.getLastName()).isEqualTo(user.getLastName());
        assertThat(userDto.getAddress()).isEqualTo(user.getAddress());
        verify(userStore, Mockito.times(1)).getUser(anyLong());
        verifyNoMoreInteractions(userStore);
    }

    @Test
    @DisplayName("Should throw an exception in case of nonexistent user")
    void getNonExistentUserTest() {
        long id = 1L;

        assertThatThrownBy(() -> userService.getUser(id)).isInstanceOf(UserNotFoundException.class);
        verify(userStore, Mockito.times(1)).getUser(anyLong());
        verifyNoMoreInteractions(userStore);
    }
}
