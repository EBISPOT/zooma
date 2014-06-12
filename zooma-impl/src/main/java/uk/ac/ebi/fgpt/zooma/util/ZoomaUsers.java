package uk.ac.ebi.fgpt.zooma.util;

import org.springframework.security.core.context.SecurityContextHolder;
import uk.ac.ebi.fgpt.zooma.exception.UnrecognisedUserTypeException;
import uk.ac.ebi.fgpt.zooma.model.ZoomaUser;

/**
 * A simple utility class that wraps some common methods for working with users up in convenience methods
 *
 * @author Tony Burdett
 * @date 28/01/14
 */
public class ZoomaUsers {
    /**
     * Returns the currently authenticated ZOOMA user, or null if the user is anonymous
     *
     * @return the currently authenticated ZOOMA user
     * @throws UnrecognisedUserTypeException if a (non-anonymous) user was found that is NOT of type {@link
     *                                       uk.ac.ebi.fgpt.zooma.model.ZoomaUser}
     */
    public static ZoomaUser getCurrentUser() throws UnrecognisedUserTypeException {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal.equals("anonymousUser")) {
            return null;
        }

        if (principal instanceof ZoomaUser) {
            return (ZoomaUser) principal;
        }
        else {
            throw new UnrecognisedUserTypeException("Authenticated user (details: " + principal.toString() + ") " +
                                                            "is of an unrecognised type " +
                                                            "(" + principal.getClass().getName() + ")");
        }
    }
}
