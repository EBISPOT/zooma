package uk.ac.ebi.fgpt.zooma.exception;

/**
 * An exception that is thrown whenever an attempt to resolve data against ZOOMA fails
 *
 * @author Tony Burdett
 * @date 04/10/12
 */
public class ZoomaResolutionException extends Exception {
    public ZoomaResolutionException() {
        super();
    }

    public ZoomaResolutionException(String message) {
        super(message);
    }

    public ZoomaResolutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ZoomaResolutionException(Throwable cause) {
        super(cause);
    }

    protected ZoomaResolutionException(String message,
                                       Throwable cause,
                                       boolean enableSuppression,
                                       boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
