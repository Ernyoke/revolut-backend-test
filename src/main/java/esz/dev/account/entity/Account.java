package esz.dev.account.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

import java.math.BigDecimal;

/**
 * Entity class for bank accounts.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Wither
public class Account {
    private String iban;
    private BigDecimal amount;
}
