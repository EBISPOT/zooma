package uk.ac.ebi.spot.exception;

/**
 * An exception that is thrown whenever a search made against ZOOMA fails, usually due to a communication failure of
 * some sort.
 *
 * @author Tony Burdett
 * @date 02/04/12
 */
public class SearchException extends RuntimeException {
    public SearchException() {
        super();
    }

    public SearchException(String message) {
        super(message);
    }

    public SearchException(String message, Throwable cause) {
        super(message, cause);
    }

    public SearchException(Throwable cause) {
        super(cause);
    }
}
