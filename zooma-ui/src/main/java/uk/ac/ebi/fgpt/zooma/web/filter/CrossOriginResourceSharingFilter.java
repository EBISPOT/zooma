package uk.ac.ebi.fgpt.zooma.web.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filters all requests and adds Cross Origin Resource Sharing (CORS) to indicate that GET requests to the ZOOMA API are
 * allowed across all domains.
 *
 * @author Tony Burdett
 * @date 29/04/13
 */
public class CrossOriginResourceSharingFilter implements Filter {
    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;


        // is this a CORS request?
        if (httpRequest.getHeader("Origin") != null) {
            String origin = httpRequest.getHeader("Origin");
            String requestURI = httpRequest.getRequestURI();
            getLog().trace("Possible cross-origin request received from '" + origin + "' to URI: " +
                                   "'" + requestURI + "'.  Enabling CORS.");

            // add CORS "pre-flight" request headers
            httpResponse.addHeader("Access-Control-Allow-Origin", "*");
            httpResponse.addHeader("Access-Control-Allow-Headers", "accept,Content-Type");
            httpResponse.addHeader("Access-Control-Allow-Methods", "GET,POST,PUT");
            httpResponse.addHeader("Access-Control-Allow-Credentials", "true");
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
