package uk.ac.ebi.fgpt.zooma.atlas;

/**
 * An exception thrown whenever communication with the Gene Expression Atlas fails.
 *
 * @author Tony Burdett
 * @date 24/11/11
 */
public class AtlasCommunicationException extends Exception {
    public AtlasCommunicationException() {
        super();
    }

    public AtlasCommunicationException(String message) {
        super(message);
    }

    public AtlasCommunicationException(String message, Throwable cause) {
        super(message, cause);
    }

    public AtlasCommunicationException(Throwable cause) {
        super(cause);
    }
}
