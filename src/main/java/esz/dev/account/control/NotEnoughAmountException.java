package esz.dev.account.control;

/**
 * Exception which should be thrown in case when a given account goes bellow 0 funds after a specific transaction.
 */
public class NotEnoughAmountException extends Exception {
    public NotEnoughAmountException(String what) {
        super(what);
    }
}
