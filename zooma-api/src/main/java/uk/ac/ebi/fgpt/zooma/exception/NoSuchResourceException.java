package uk.ac.ebi.fgpt.zooma.exception;

/**
 * An exception that is thrown whenever an attempt is made to modify an identifiable resource that does not already
 * exist.
 *
 * @author Tony Burdett
 * @date 30/03/12
 */
public class NoSuchResourceException extends RuntimeException {
    public NoSuchResourceException() {
        super();
    }

    public NoSuchResourceException(String message) {
        super(message);
    }

    public NoSuchResourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoSuchResourceException(Throwable cause) {
        super(cause);
    }
}
