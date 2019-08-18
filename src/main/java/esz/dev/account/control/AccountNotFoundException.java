package esz.dev.account.control;

/**
 * Exception which should be thrown in case of a nonexistent account.
 */
public class AccountNotFoundException extends Exception {
    public AccountNotFoundException(String what) {
        super(what);
    }
}
