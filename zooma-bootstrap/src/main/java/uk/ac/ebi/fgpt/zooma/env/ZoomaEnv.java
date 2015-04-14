package uk.ac.ebi.fgpt.zooma.env;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Facilitates setup of the required ZOOMA environment by checking system and environment properties or using sensible
 * defaults
 *
 * @author Tony Burdett
 * @date 26/11/14
 */
public class ZoomaEnv {
    private static final ZoomaEnv instance = new ZoomaEnv();

    public static void configureZOOMAEnvironment() {
        instance.setupEnvironment();
    }

    private ZoomaEnv() {

    }

    private final Logger log = LoggerFactory.getLogger(getClass());

    private Logger getLog() {
        return log;
    }

    /**
     * ZOOMA works on the assumption that a 'ZOOMA home' directory exists, storing configuration files and loaders.
     * Without this, ZOOMA will not function.  This method sets up the ZOOMA environment correctly by first inspecting
     * for the presence of the system variable 'zooma.home' (can be supplied with -Dzooma.home=* when the application is
     * run).  If this exists, the value that the user has set is used throughout.  If this is not supplied, then we
     * check for the presence of the $ZOOMA_HOME environmental variable.  If THIS is set, then this method will set the
     * value of this environment variable as the system property.  If neither option is supplied, zooma.home defaults to
     * {user.home}/.zooma
     */
    private synchronized void setupEnvironment() {
        getLog().debug("Initializing ZOOMA in the following environment:");
        Map<String, String> env = System.getenv();
        for (String envName : env.keySet()) {
            getLog().debug(envName + " = " + env.get(envName));
        }

        // sesame http proxy workaround shenanigans - apparently sesame overwrites http proxy;
        // this is a workaround to allow custom proxy property settings (zooma.http.proxy*) and then update
        // default settings (http.proxy*) after sesame has wiped them
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
}
