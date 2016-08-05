package uk.ac.ebi.spot.exception;

/**
 * An exception that is thrown whenever an attempt to update data in ZOOMA fails
 *
 * @author Tony Burdett
 * @date 10/07/13
 */
public class ZoomaUpdateException extends Exception {
    public ZoomaUpdateException() {
    }

    public ZoomaUpdateException(String message) {
        super(message);
    }

    public ZoomaUpdateException(String message, Throwable cause) {
        super(message, cause);
    }

    public ZoomaUpdateException(Throwable cause) {
        super(cause);
    }

    public ZoomaUpdateException(String message,
                                Throwable cause,
                                boolean enableSuppression,
                                boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
