package uk.ac.ebi.fgpt.zooma.exception;

/**
 * An exception that is thrown whenever a query against the ZOOMA index failed to be created
 *
 * @author Tony Burdett
 * @date 31/08/12
 */
public class QueryCreationException extends RuntimeException {
    public QueryCreationException() {
        super();
    }

    public QueryCreationException(String message) {
        super(message);
    }

    public QueryCreationException(String message, Throwable cause) {
        super(message, cause);
    }

    public QueryCreationException(Throwable cause) {
        super(cause);
    }
}
