package uk.ac.ebi.fgpt.zooma.exception;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 21/01/16
 */
public class SearchResourcesUnavailableException extends SearchException {
    public SearchResourcesUnavailableException() {
        super();
    }

    public SearchResourcesUnavailableException(String message) {
        super(message);
    }

    public SearchResourcesUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public SearchResourcesUnavailableException(Throwable cause) {
        super(cause);
    }
}
