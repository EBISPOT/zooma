package uk.ac.ebi.fgpt.zooma.web;

import org.openid4java.util.HttpClientFactory;
import org.openid4java.util.ProxyProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.zooma.util.ZoomaUtils;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.Map;

/**
 * Checks the environment at startup and sets some sensible defaults if expected values are missing.
 *
 * @author Tony Burdett
 * @date 12/06/12
 */
public class ZoomaEnvironmentListener implements ServletContextListener {
    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    @Override public void contextInitialized(ServletContextEvent servletContextEvent) {
        ZoomaUtils.configureZOOMAEnvironment();

        // update HttpClientFactory with proxy settings - used (at least) in OpenID authentication
        String proxyHost = System.getProperty("http.proxyHost");
        String proxyPort = System.getProperty("http.proxyPort");
        if (proxyHost != null || proxyPort != null) {
            ProxyProperties proxyProps = new ProxyProperties();
            if (proxyHost != null) {
                getLog().debug("Updating HttpClientFactory proxy settings - host: " + proxyHost);
                proxyProps.setProxyHostName(proxyHost);
            }
            if (proxyPort != null) {
                getLog().debug("Updating HttpClientFactory proxy settings - port: " + proxyPort);
                proxyProps.setProxyPort(Integer.parseInt(proxyPort));
            }
            HttpClientFactory.setProxyProperties(proxyProps);
            getLog().debug("HttpClientFactory proxy settings updated successfully");
        }
    }

    @Override public void contextDestroyed(ServletContextEvent servletContextEvent) {
        getLog().info("Shutting down ZOOMA.");
    }
}
