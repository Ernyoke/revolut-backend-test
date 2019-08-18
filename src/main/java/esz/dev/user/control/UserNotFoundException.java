package esz.dev.user.control;

/**
 * Exception which should be thrown in case when a given user is not found in the system.
 */
public class UserNotFoundException extends Exception {
    public UserNotFoundException(String what) {
        super(what);
    }
}
