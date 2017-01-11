package uk.ac.ebi.spot.zooma.exception;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 17/08/15
 */
public class TemplateBuildingException extends RuntimeException {
    public TemplateBuildingException() {
        super();
    }

    public TemplateBuildingException(String message) {
        super(message);
    }

    public TemplateBuildingException(String message, Throwable cause) {
        super(message, cause);
    }
}
