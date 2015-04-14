package uk.ac.ebi.fgpt.zooma.exception;

/**
 * This exception is thrown when a has authenticated and registered with ZOOMA but the type of user is not recognised by
 * the wider system, i.e. is not a type of {@link uk.ac.ebi.fgpt.zooma.model.ZoomaUser}.  This implies that a user has
 * been successfully authenticated by the spring security stack without corresponding ZOOMA user details, and has
 * therefore never been registered.  This is likely suspicious.
 *
 * @author Tony Burdett
 * @date 28/01/14
 */
public class UnrecognisedUserTypeException extends RuntimeException {
    public UnrecognisedUserTypeException() {
        super();
    }

    public UnrecognisedUserTypeException(String message) {
        super(message);
    }

    public UnrecognisedUserTypeException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnrecognisedUserTypeException(Throwable cause) {
        super(cause);
    }

    protected UnrecognisedUserTypeException(String message,
                                            Throwable cause,
                                            boolean enableSuppression,
                                            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
