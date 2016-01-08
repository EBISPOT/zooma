package uk.ac.ebi.fgpt.zooma.env;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.File;
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

    private final Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    private ZoomaEnv() {

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
        getLog().info("Bootstrapping ZOOMA...");
        getLog().debug("ZOOMA has detected the following environmental variables:");
        Map<String, String> env = System.getenv();
        for (String envName : env.keySet()) {
            getLog().debug(" => " + envName + " = " + env.get(envName));
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
                getLog().debug(
                        "ZOOMA is setting proxy properties to echo zooma.http.proxyHost and " +
                                "zooma.http.proxyPort - these are backups in case of proxy setting wiping by Sesame");
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

        // zooma properties already set?
        String zoomaHome = System.getProperty("zooma.home");
        String zoomaDataDir = System.getProperty("zooma.data.dir");

        // if zooma.home not set, check $ZOOMA_HOME environment variable
        if (zoomaHome == null || zoomaHome.equals("")) {
            String home = System.getenv("ZOOMA_HOME");
            if (home == null || home.equals("")) {
                // try JNDI context lookup
                try {
                    home = new InitialContext().lookup("java:comp/env/zooma.home").toString();
                }
                catch (NamingException e) {
                    getLog().warn("No zooma.home or $ZOOMA_HOME, lookup for java:comp/env/zooma.home also failed");
                }

                if (home == null || home.equals("")) {
                    home = System.getProperty("user.home") + File.separator + ".zooma";
                    getLog().info("*** zooma.home defaulting to: " + home + " " +
                                          "(No zooma.home, $ZOOMA_HOME or context object named zooma.home) ***");
                }
            }
            else {
                getLog().info("*** $ZOOMA_HOME: " + home + " ***");
            }
            System.setProperty("zooma.home", home);
        }
        else {
            getLog().info("*** zooma.home: " + zoomaHome + " ***");
        }

        if (zoomaDataDir == null || zoomaDataDir.equals("")) {
            String dataDir = System.getenv("ZOOMA_DATA_DIR");
            if (dataDir == null || dataDir.equals("")) {
                // try JNDI context lookup
                try {
                    dataDir = new InitialContext().lookup("java:comp/env/zooma.data.dir").toString();
                }
                catch (NamingException e) {
                    getLog().warn("No zooma.data.dir or $ZOOMA_DATA_DIR, " +
                                          "lookup for java:comp/env/zooma.data.dir also failed");
                }

                if (dataDir == null || dataDir.equals("")) {
                    dataDir = System.getProperty("zooma.home") + File.separator + "data";
                    getLog().info("*** zooma.data.dir defaulting to: " + dataDir + " " +
                                          "(No zooma.data.dir, $ZOOMA_DATA_DIR or context object " +
                                          "named zooma.data.dir) ***");
                }
            }
            else {
                getLog().info("*** $ZOOMA_DATA_DIR: " + dataDir + " ***");
            }
            System.setProperty("zooma.data.dir", dataDir);
        }
        else {
            getLog().info("*** zooma.data.dir: " + zoomaDataDir + " ***");
        }

        getLog().info("ZOOMA environment successfully bootstrapped!");
    }
}
