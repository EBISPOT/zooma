package uk.ac.ebi.fgpt.zooma.exception;

/**
 * An exception that is thrown whenever an ambiguously named resource is encountered.  For example, this exception would
 * be thrown if an identifier that should be unique was in conflict.
 *
 * @author Tony Burdett
 * @date 12/06/13
 */
public class AmbiguousResourceException extends RuntimeException {
    public AmbiguousResourceException() {
        super();
    }

    public AmbiguousResourceException(String message) {
        super(message);
    }

    public AmbiguousResourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public AmbiguousResourceException(Throwable cause) {
        super(cause);
    }

    public AmbiguousResourceException(String message,
                                      Throwable cause,
                                      boolean enableSuppression,
                                      boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
