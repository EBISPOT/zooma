package uk.ac.ebi.spot.exception;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 13/06/14
 */
public class AnonymousUserNotAllowedException extends RuntimeException {
    public AnonymousUserNotAllowedException() {
        super();
    }

    public AnonymousUserNotAllowedException(String message) {
        super(message);
    }

    public AnonymousUserNotAllowedException(String message, Throwable cause) {
        super(message, cause);
    }

    public AnonymousUserNotAllowedException(Throwable cause) {
        super(cause);
    }

    protected AnonymousUserNotAllowedException(String message,
                                               Throwable cause,
                                               boolean enableSuppression,
                                               boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
