package uk.ac.ebi.fgpt.zooma.exception;

/**
 * This exception is thrown when either a user is not recognised, has managed to access a restricted endpoint without
 * authenticating, or has authenticated and registered with ZOOMA but the type of user is not recognised by the wider
 * system, i.e. is not a type of {@link uk.ac.ebi.fgpt.zooma.model.ZoomaUser}.  In this latter case, a user has been
 * successfully authenticated by the spring security stack without corresponding ZOOMA user details, which implies this
 * user has never been registered.  This is likely suspicious.
 *
 * @author Tony Burdett
 * @date 28/01/14
 */
public class UnrecognisedUserOrTypeException extends RuntimeException {
    public UnrecognisedUserOrTypeException() {
        super();
    }

    public UnrecognisedUserOrTypeException(String message) {
        super(message);
    }

    public UnrecognisedUserOrTypeException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnrecognisedUserOrTypeException(Throwable cause) {
        super(cause);
    }

    protected UnrecognisedUserOrTypeException(String message,
                                              Throwable cause,
                                              boolean enableSuppression,
                                              boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
