package esz.dev.user.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

import java.util.Set;

/**
 * Entity class for users.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Wither
@Builder
public class User {
    private Long id;
    private String firstName;
    private String lastName;
    private String address;
    private Set<String> accounts;
}
