package uk.ac.ebi.fgpt.zooma.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.util.UrlUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * An alternative to the spring security {@link org.springframework.security.web.DefaultRedirectStrategy} that makes use
 * of the HTTP 308 (Permanent Redirect) response code; this indicates that requests should be repeated with an
 * alternative URI but that the method should not change.  This therefore allows POST requests to be redirected
 * appropriately, without modification.
 * <p/>
 * HTTP status code 308 is experimental, and approved with RFC status - see <a href="http://tools.ietf.org/html/draft-reschke-http-status-308-07">http://tools.ietf.org/html/draft-reschke-http-status-308-07</a>
 * for more.
 *
 * @author Tony Burdett
 * @date 21/02/14
 */
public class PermanentRedirectStrategy implements RedirectStrategy {
    private final Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    /**
     * Redirects the response to the supplied URL with status code 308 (permanent redirect)
     */
    public void sendRedirect(HttpServletRequest request, HttpServletResponse response, String url) throws IOException {
        String redirectUrl = calculateRedirectUrl(request.getContextPath(), url);
        redirectUrl = response.encodeRedirectURL(redirectUrl);

        if (getLog().isDebugEnabled()) {
            getLog().debug("Redirecting to '" + redirectUrl + "'");
        }

        response.setHeader("Location", redirectUrl);
        response.setStatus(307);
    }

    private String calculateRedirectUrl(String contextPath, String url) {
        if (!UrlUtils.isAbsoluteUrl(url)) {
            return contextPath + url;
        }
        else {
            return url;
        }
    }
}
