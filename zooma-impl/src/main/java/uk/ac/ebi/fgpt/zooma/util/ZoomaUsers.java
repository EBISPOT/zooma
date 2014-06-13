package uk.ac.ebi.fgpt.zooma.util;

import org.springframework.security.core.context.SecurityContextHolder;
import uk.ac.ebi.fgpt.zooma.exception.UnrecognisedUserOrTypeException;
import uk.ac.ebi.fgpt.zooma.model.ZoomaUser;
import uk.ac.ebi.fgpt.zooma.model.ZoomaUserImpl;

import java.util.Collection;

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
     * @throws uk.ac.ebi.fgpt.zooma.exception.UnrecognisedUserOrTypeException if a (non-anonymous) user was found that is NOT of type {@link
     *                                       uk.ac.ebi.fgpt.zooma.model.ZoomaUser}
     */
    public static ZoomaUser getCurrentUser() throws UnrecognisedUserOrTypeException {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal.equals("anonymousUser")) {
                return null;
            }

            if (principal instanceof ZoomaUser) {
                return (ZoomaUser) principal;
            }
            else {
                throw new UnrecognisedUserOrTypeException("Authenticated user (details: " + principal.toString() + ") " +
                                                                "is of an unrecognised type " +
                                                                "(" + principal.getClass().getName() + ")");
            }
        }
        catch (NullPointerException e) {
            // if we get a NPE, just return null
            return null;
        }
    }
}
