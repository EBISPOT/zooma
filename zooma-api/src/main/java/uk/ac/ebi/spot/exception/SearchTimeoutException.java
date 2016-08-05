package uk.ac.ebi.spot.exception;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 21/01/16
 */
public class SearchTimeoutException extends SearchException {
    public SearchTimeoutException() {
        super();
    }

    public SearchTimeoutException(String message) {
        super(message);
    }

    public SearchTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

    public SearchTimeoutException(Throwable cause) {
        super(cause);
    }
}
