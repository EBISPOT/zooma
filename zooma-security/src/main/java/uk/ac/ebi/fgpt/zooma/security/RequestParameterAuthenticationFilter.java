package uk.ac.ebi.fgpt.zooma.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedCredentialsNotFoundException;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;

/**
 * A simple pre-authenticated filter which obtains the username from a request parameter; this is useful for stateless
 * requests that require some measure of security, but in these cases it is highly important that the parameter value
 * (the 'api key') is not shared and cannot be intercepted - so requests should be made over https.
 * <p/>
 * As with most pre-authenticated scenarios, it is essential that the external authentication system is set up correctly
 * as this filter does no authentication whatsoever. All the protection is assumed to be provided externally and if this
 * filter is included inappropriately in a configuration, it would be possible  to assume the identity of a user merely
 * by setting the correct parameter. This also means it should not generally be used in combination with other Spring
 * Security authentication mechanisms such as form login, as this would imply there was a means of bypassing the
 * external system which would be risky.
 * <p/>
 * The property {@code principalRequestParameter} is the name of the request parameter that contains the api key. It
 * defaults to "apiKey".
 * <p/>
 * If the parameter is missing from the request, {@code getPreAuthenticatedPrincipal} will, by default, return null -
 * which in effect means an attempt at anonymous access.  You can override this behaviour by setting the {@code
 * exceptionIfParameterMissing} property to true.
 *
 * @author Tony Burdett
 * @date 25/10/13
 */
public class RequestParameterAuthenticationFilter extends AbstractPreAuthenticatedProcessingFilter {
    private String principalRequestParameter = "apiKey";
    private boolean exceptionIfParameterMissing = false;

    private final Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    /**
     * Read and returns the parameter named by {@code principalRequestParameter} from the request.
     *
     * @throws PreAuthenticatedCredentialsNotFoundException if the header is missing and {@code exceptionIfParameterMissing}
     *                                                      is set to {@code true}.
     */
    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
        String principal = request.getParameter(principalRequestParameter);
        if (getLog().isTraceEnabled()) {
            getLog().trace("Attempted to capture api key from request - got " + principal);
        }
        if (principal == null && exceptionIfParameterMissing) {
            throw new PreAuthenticatedCredentialsNotFoundException(principalRequestParameter
                                                                           + " parameter not found in request.");
        }

        return principal;
    }

    /**
     * Credentials aren't applicable in this scenario.
     */
    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
        return "N/A";
    }

    public void setPrincipalRequestParameter(String principalRequestParameter) {
        Assert.hasText(principalRequestParameter, "principalRequestParameter must not be empty or null");
        this.principalRequestParameter = principalRequestParameter;
    }

    /**
     * Defines whether an exception should be raised if the principal parameter is missing. Defaults to {@code false}.
     *
     * @param exceptionIfParameterMissing set to {@code true} to override the default behaviour and allow the request to
     *                                    proceed if no header is found.
     */
    public void setExceptionIfParameterMissing(boolean exceptionIfParameterMissing) {
        this.exceptionIfParameterMissing = exceptionIfParameterMissing;
    }
}
