package esz.dev.account.control;

import esz.dev.account.entity.Account;

import javax.inject.Singleton;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Data persistence for bank account objects. This implementation provides an in-memory solution for persistence, and
 * should not be used in real life scenarios.
 */
@Singleton
public class AccountStore {
    private final Map<String, Account> accounts = new ConcurrentHashMap<>();

    public void addAccount(Account account) {
        accounts.put(account.getIban(), account);
    }

    public Optional<Account> getAccount(String iban) {
        return Optional.ofNullable(accounts.get(iban));
    }
}
