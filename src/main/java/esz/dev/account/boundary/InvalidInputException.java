package esz.dev.account.boundary;

/**
 * Exception which should be thrown in case when request body is not a valid.
 */
public class InvalidInputException extends Exception {
    public InvalidInputException(String what) {
        super(what);
    }
}
