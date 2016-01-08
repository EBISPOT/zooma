package uk.ac.ebi.fgpt.zooma.util;

import org.springframework.security.core.context.SecurityContextHolder;
import uk.ac.ebi.fgpt.zooma.exception.AnonymousUserNotAllowedException;
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
     * Returns the currently authenticated ZOOMA user, by default requiring authentication and NOT allowing anonymous
     * access.  This is a convenience wrapper around <code>getCurrentUser(false)</code>
     *
     * @return the non-anonymous, fully authenticated ZOOMA user
     * @throws uk.ac.ebi.fgpt.zooma.exception.UnrecognisedUserTypeException    if a (non-anonymous) user was found that
     *                                                                         is NOT of type {@link uk.ac.ebi.fgpt.zooma.model.ZoomaUser}
     * @throws uk.ac.ebi.fgpt.zooma.exception.AnonymousUserNotAllowedException if the current user is anonymous
     */
    public static ZoomaUser getCurrentUser() throws AnonymousUserNotAllowedException, UnrecognisedUserTypeException {
        return getCurrentUser(false);
    }

    /**
     * Returns the currently authenticated ZOOMA user, with an option to allow the case where the user is not
     * authenticated.  If this flag is set to true, this method behaves identically to {@link #getUserIfAuthenticated()}
     * and if false this method behaves identically to {@link #getAuthenticatedUser()}
     *
     * @param allowAnonymous whether to allow anonymous access (in which case the result of this method may be null) or
     *                       not
     * @return the user (may be null if allowAnonymous is set to be 'true')
     * @throws uk.ac.ebi.fgpt.zooma.exception.UnrecognisedUserTypeException    if a (non-anonymous) user was found that
     *                                                                         is NOT of type {@link uk.ac.ebi.fgpt.zooma.model.ZoomaUser}
     * @throws uk.ac.ebi.fgpt.zooma.exception.AnonymousUserNotAllowedException if the current user is anonymous, and
     *                                                                         allowAnonymous was set to 'false'
     */
    public static ZoomaUser getCurrentUser(boolean allowAnonymous) throws
            AnonymousUserNotAllowedException, UnrecognisedUserTypeException {
        return allowAnonymous ? getUserIfAuthenticated() : getAuthenticatedUser();
    }

    /**
     * Returns the currently authenticated ZOOMA user if and only if the user has successfully authenticated and is not
     * anonymous.  In all other cases, and exception will be thrown.
     *
     * @return the non-anonymous, fully authenticated ZOOMA user
     * @throws uk.ac.ebi.fgpt.zooma.exception.UnrecognisedUserTypeException    if a (non-anonymous) user was found that
     *                                                                         is NOT of type {@link uk.ac.ebi.fgpt.zooma.model.ZoomaUser}
     * @throws uk.ac.ebi.fgpt.zooma.exception.AnonymousUserNotAllowedException if the current user is anonymous
     */
    public static ZoomaUser getAuthenticatedUser()
            throws AnonymousUserNotAllowedException, UnrecognisedUserTypeException {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal.equals("anonymousUser")) {
                throw new AnonymousUserNotAllowedException("Anonymous access to this method is not allowed");
            }

            if (principal instanceof ZoomaUser) {
                return (ZoomaUser) principal;
            }
            else {
                throw new UnrecognisedUserTypeException(
                        "Authenticated user (details: " + principal.toString() + ") " +
                                "is of an unrecognised type " +
                                "(" + principal.getClass().getName() + ")");
            }
        }
        catch (NullPointerException e) {
            // if we get a NPE, just return null
            throw new AnonymousUserNotAllowedException(
                    "Access to this application normally requires authentication, but authentication is disabled");
        }
    }

    /**
     * Returns the currently authenticated ZOOMA user, if known, or null if the user is anonymous or if there is no
     * active authentication stack.  This is a permissive form of user checking; only use this in code that you are
     * happy for unauthenticated users to access, and remember to handle the case where this returns null to avoid null
     * pointer exceptions
     *
     * @return the currently authenticated ZOOMA user, or null if unknown or anonymous
     * @throws uk.ac.ebi.fgpt.zooma.exception.UnrecognisedUserTypeException if a (non-anonymous) user was found that is
     *                                                                      NOT of type {@link uk.ac.ebi.fgpt.zooma.model.ZoomaUser}
     */
    public static ZoomaUser getUserIfAuthenticated() throws UnrecognisedUserTypeException {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal.equals("anonymousUser")) {
                return null;
            }

            if (principal instanceof ZoomaUser) {
                return (ZoomaUser) principal;
            }
            else {
                throw new UnrecognisedUserTypeException(
                        "Authenticated user (details: " + principal.toString() + ") " +
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
