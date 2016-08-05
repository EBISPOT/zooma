package uk.ac.ebi.spot.exception;

/**
 * An exception thrown whenever model objects from zooma failed to serialize
 *
 * @author Tony Burdett
 * @date 04/10/12
 */
public class ZoomaSerializationException extends Exception {
    public ZoomaSerializationException() {
        super();
    }

    public ZoomaSerializationException(String message) {
        super(message);
    }

    public ZoomaSerializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ZoomaSerializationException(Throwable cause) {
        super(cause);
    }

    protected ZoomaSerializationException(String message,
                                          Throwable cause,
                                          boolean enableSuppression,
                                          boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
