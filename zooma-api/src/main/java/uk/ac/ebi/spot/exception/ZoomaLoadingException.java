package uk.ac.ebi.spot.exception;

/**
 * An exception that is thrown whenever an attempt to load data into ZOOMA fails
 *
 * @author Tony Burdett
 * @date 04/10/12
 */
public class ZoomaLoadingException extends Exception {
    public ZoomaLoadingException() {
    }

    public ZoomaLoadingException(String message) {
        super(message);
    }

    public ZoomaLoadingException(String message, Throwable cause) {
        super(message, cause);
    }

    public ZoomaLoadingException(Throwable cause) {
        super(cause);
    }

    public ZoomaLoadingException(String message,
                                 Throwable cause,
                                 boolean enableSuppression,
                                 boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
