package esz.dev.account.control;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Data transfer object used for transactions which imply presence of a singe account.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonAutoDetect
public class AmountDto {
    private String iban;
    private BigDecimal amount;
}
