package esz.dev.user.control;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data transfer object for user entities.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonAutoDetect
public class UserDto {
    private String firstName;
    private String lastName;
    private String address;
}
