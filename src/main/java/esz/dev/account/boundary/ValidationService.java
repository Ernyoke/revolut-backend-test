package esz.dev.account.boundary;

import esz.dev.account.control.AmountDto;
import esz.dev.account.control.TransferAmountDto;
import org.iban4j.IbanFormatException;
import org.iban4j.IbanUtil;

import javax.inject.Singleton;
import java.math.BigDecimal;

/**
 * Handles basic validation in the input data.
 */
@Singleton
public class ValidationService {
    public void validate(AmountDto amountDto) throws InvalidInputException {
        validateAmount(amountDto.getAmount());
        validateIban(amountDto.getIban());
    }

    public void validate(TransferAmountDto amountDto) throws InvalidInputException {
        validateAmount(amountDto.getAmount());
        validateIban(amountDto.getSenderIban());
        validateIban(amountDto.getReceiverIban());
    }

    public void validateIban(String iban) throws InvalidInputException {
        try {
            IbanUtil.validate(iban);
        } catch (IbanFormatException ex) {
            throw new InvalidInputException("Invalid IBAN value " + iban);
        }
    }

    private void validateAmount(BigDecimal amount) throws InvalidInputException {
        if (amount == null) {
            throw new InvalidInputException("Missing amount value!");
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidInputException("Amount can not be a negative value!");
        }
    }
}
