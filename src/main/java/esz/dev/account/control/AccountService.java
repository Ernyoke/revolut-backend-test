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

    private static final CountryCode countryCode = CountryCode.RO;
    private static final String bankCode = "XXXX567890123456789";
    private final AtomicLong accountNumber = new AtomicLong();

    @Inject
    public AccountService(AccountStore accountStore, UserStore userStore) {
        this.accountStore = accountStore;
        this.userStore = userStore;
    }

    public void createAccount(long userId) throws UserNotFoundException {
        Iban iban = new Iban.Builder()
                .countryCode(countryCode)
                .bankCode(bankCode)
                .accountNumber(String.valueOf(accountNumber.incrementAndGet()))
                .build();
        synchronized (this) {
            User user = userStore.getUser(userId).orElseThrow(() -> new UserNotFoundException("No esz.dev.user found with id of " + userId));
            accountStore.addAccount(Account.builder()
                    .iban(iban.toString())
                    .amount(BigDecimal.ZERO).build());
            user.getAccounts().add(iban.toString());
        }
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
                .orElseThrow(() -> new AccountNotFoundException("No esz.dev.account found with iban " + iban));
    }
}
