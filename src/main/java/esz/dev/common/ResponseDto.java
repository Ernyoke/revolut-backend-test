package esz.dev.common;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data transfer object which is used a response for generic REST calls.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonAutoDetect
public class ResponseDto {
    public static enum Status {
        SUCCESS, FAILURE
    }

    private Status status;
    private String message;
}
