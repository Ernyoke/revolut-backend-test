package esz.dev.account.control;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Data transfer object used for transactions which imply presence of two accounts (for example: money transfer from an
 * account to another).
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonAutoDetect
public class TransferAmountDto {
    private String senderIban;
    private String receiverIban;
    private BigDecimal amount;
}
