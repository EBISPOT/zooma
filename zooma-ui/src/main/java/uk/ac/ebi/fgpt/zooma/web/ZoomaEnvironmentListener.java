package uk.ac.ebi.fgpt.zooma.web;

import org.openid4java.util.HttpClientFactory;
import org.openid4java.util.ProxyProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        getLog().debug("Initializing ZOOMA in the following environment:");
        Map<String, String> env = System.getenv();
        for (String envName : env.keySet()) {
            getLog().debug(envName + " = " + env.get(envName));
        }

        // sesame http proxy workaround shenanigans - apparently sesame overwrites http proxy, these are some workaround shenanigans
        String proxyHost = System.getProperty("http.proxyHost");
        String proxyPort = System.getProperty("http.proxyPort");
        if (proxyHost == null && proxyPort == null) {
            String zoomaProxyHost = System.getProperty("zooma.http.proxyHost");
            String zoomaProxyPort = System.getProperty("zooma.http.proxyPort");
            String zoomaNonProxyHosts = System.getProperty("zooma.http.nonProxyHosts");

            if (zoomaProxyHost != null || zoomaProxyPort != null) {
                getLog().warn("Setting proxy properties to echo zooma.http.proxyHost and zooma.http.proxyPort: " +
                                      "these are backups in case of proxy setting wiping by Sesame");
                if (zoomaProxyHost != null) {
                    System.setProperty("http.proxyHost", zoomaProxyHost);
                }
                if (zoomaProxyPort != null) {
                    System.setProperty("http.proxyPort", zoomaProxyPort);
                }
                if (zoomaNonProxyHosts != null) {
                    System.setProperty("http.nonProxyHosts", zoomaNonProxyHosts);
                }
            }
        }

        // update HttpClientFactory with proxy settings - used (at least) in OpenID authentication
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

        // zooma.home already set?
        String zoomaHome = System.getProperty("zooma.home");

        // if not, check $ZOOMA_HOME environment variable
        if (zoomaHome == null || zoomaHome.equals("")) {
            String zooma = System.getenv("ZOOMA_HOME");
            if (zooma == null || zooma.equals("")) {
                zooma = System.getProperty("user.home") + "/.zooma/";
                getLog().info("$ZOOMA_HOME not set - defaulting to: " + zooma);
            }
            else {
                getLog().info("$ZOOMA_HOME: " + zooma);
            }
            System.setProperty("zooma.home", zooma);
        }
    }

    @Override public void contextDestroyed(ServletContextEvent servletContextEvent) {
        getLog().info("Shutting down ZOOMA.");
    }
}
