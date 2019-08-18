package esz.dev.account.control;

import esz.dev.account.entity.Account;
import org.iban4j.CountryCode;
import org.iban4j.Iban;
import esz.dev.user.control.UserNotFoundException;
import esz.dev.user.control.UserStore;
import esz.dev.user.entity.User;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Core implementation for banking transactions.
 */
@Singleton
public class AccountService {
    private final AccountStore accountStore;
    private final UserStore userStore;

    private static final CountryCode COUNTRY_CODE = CountryCode.RO;
    private static final String BANK_CODE_PREFIX = "XXXX";
    private int accountCounter = 0;

    @Inject
    public AccountService(AccountStore accountStore, UserStore userStore) {
        this.accountStore = accountStore;
        this.userStore = userStore;
    }

    public synchronized String createAccount(long userId) throws UserNotFoundException {
        String accountNumber = String.valueOf(accountCounter++);
        Iban iban = new Iban.Builder()
                .countryCode(COUNTRY_CODE)
                .bankCode(generateBankCode(accountNumber))
                .accountNumber(accountNumber)
                .build();
        User user = userStore.getUser(userId).orElseThrow(() -> new UserNotFoundException("No user found with id of " + userId));
        accountStore.addAccount(Account.builder()
                .iban(iban.toString())
                .amount(BigDecimal.ZERO).build());
        user.getAccounts().add(iban.toString());
        return iban.toString();
    }

    public AmountDto checkFunds(String iban) throws AccountNotFoundException {
        return accountStore.getAccount(iban).map(account -> new AmountDto(iban, account.getAmount()))
                .orElseThrow(() -> new AccountNotFoundException("No account found with iban " + iban));
    }

    public synchronized void withdraw(AmountDto amountDto) throws AccountNotFoundException, NotEnoughAmountException {
        Account account = getAccountOrElseThrow(amountDto.getIban());
        BigDecimal amount = account.getAmount().subtract(amountDto.getAmount());
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new NotEnoughAmountException("Not enough amount on account with iban " + account.getIban());
        } else {
            account.setAmount(amount);
        }
    }

    public synchronized void deposit(AmountDto amountDto) throws AccountNotFoundException {
        Account account = getAccountOrElseThrow(amountDto.getIban());
        BigDecimal amount = account.getAmount().add(amountDto.getAmount());
        account.setAmount(amount);
    }

    public synchronized void transfer(TransferAmountDto transferAmountDto) throws AccountNotFoundException, NotEnoughAmountException {
        withdraw(new AmountDto(transferAmountDto.getSenderIban(), transferAmountDto.getAmount()));
        deposit(new AmountDto(transferAmountDto.getReceiverIban(), transferAmountDto.getAmount()));
    }

    private Account getAccountOrElseThrow(String iban) throws AccountNotFoundException {
        return accountStore.getAccount(iban)
                .orElseThrow(() -> new AccountNotFoundException("No account found with iban " + iban));
    }

    private String generateBankCode(String accountNumber) {
        StringBuilder stringBuilder = new StringBuilder().append(BANK_CODE_PREFIX);
        for (int i = 0; i < 16 - accountNumber.length(); i++) {
            stringBuilder.append(0);
        }
        return stringBuilder.toString();
    }
}
