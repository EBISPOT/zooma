package uk.ac.ebi.fgpt.zooma.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * In the pre-authenticated authentication case, where the user has supplied an API key, the user will already have been
 * identified using this API key and their full credentials obtained using LDAP lookup. <p> This means this class isn't
 * actually responsible for the commencement of authentication, as it is in the case of other providers. It will only be
 * called if the user does not supply an API key, but then tries to access a secure resource.
 * <p/>
 * The <code>commence</code> method will always return an <code>HttpServletResponse.SC_FORBIDDEN</code> (403 error).
 *
 * @author Tony Burdett
 * @date 23/10/13
 */
public class Http401UnauthorizedEntryPoint implements AuthenticationEntryPoint {
    private final Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException arg2)
            throws IOException, ServletException {
        getLog().debug("Pre-authenticated entry point called. Rejecting access");
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Access Denied");
    }
}
