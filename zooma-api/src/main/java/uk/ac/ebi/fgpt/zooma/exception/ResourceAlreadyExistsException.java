package uk.ac.ebi.fgpt.zooma.exception;

/**
 * An exception that is thrown whenever an attempt is made to create a new identifiable resource that already exists.
 *
 * @author Tony Burdett
 * @date 30/03/12
 */
public class ResourceAlreadyExistsException extends RuntimeException {
    public ResourceAlreadyExistsException() {
        super();
    }

    public ResourceAlreadyExistsException(String message) {
        super(message);
    }

    public ResourceAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }

    public ResourceAlreadyExistsException(Throwable cause) {
        super(cause);
    }
}
