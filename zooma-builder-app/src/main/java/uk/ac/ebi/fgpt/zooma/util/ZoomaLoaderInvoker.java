package uk.ac.ebi.fgpt.zooma.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import uk.ac.ebi.fgpt.zooma.exception.ZoomaLoadingException;
import uk.ac.ebi.fgpt.zooma.service.DataLoadingService;

/**
 * A general utility class that offers convenience calls for invoking ZOOMA loads given convention-based patterns.  This
 * invoker loads the spring configuration "zooma-load.xml" from the classpath, and then looks for DAO implementations to
 * use.
 * <p/>
 * If using this mechanism to load into zooma, you should create spring configuration files to set up your {@link
 * uk.ac.ebi.fgpt.zooma.datasource.AnnotationDAO} implementations and name these files, by convention,
 * zooma-annotation-dao.xml.
 * <p/>
 * This invoker will then pick them up and autowire any AnnotationDAO implementations to the load framework, before
 * using them to extract and convert annotations into ZOOMA.
 * <p/>
 * Note that if you wish to suppress DAOs being used in this way (for example, when compositing multiple DAOs) then you
 * should add "autowire-candidate=false" to those DAOs that should not be automatically discovered.
 *
 * @author Tony Burdett
 * @date 16/10/12
 */
public class ZoomaLoaderInvoker {
    private static ZoomaLoaderInvoker invoker;

    public static ZoomaLoaderInvoker getInvoker() {
        if (invoker == null) {
            invoker = new ZoomaLoaderInvoker();
        }
        return invoker;
    }

    private ClassPathXmlApplicationContext ctx;
    private DataLoadingService loader;

    private Logger log = LoggerFactory.getLogger(getClass());

    private Logger getLog() {
        return log;
    }

    private ZoomaLoaderInvoker() {
        checkZoomaProperties();
        this.ctx = new ClassPathXmlApplicationContext(
                "zooma-cli.xml",
                "zooma-dao.xml",
                "zooma-load.xml",
                "classpath*:zooma-annotation-dao.xml");
        this.loader = ctx.getBean("dataLoadingService", DataLoadingService.class);
        getLog().debug("Found and loaded " + loader.getAvailableDatasources().size() + " AnnotationDAOs");
    }

    public void invoke() throws ZoomaLoadingException {
        getLog().info("Loading annotations from available sources and converting to RDF");
        DataLoadingService.Receipt receipt = loader.load();
        getLog().debug("Received receipt '" + receipt.getID() + "' " +
                               "(datasource \"" + receipt.getDatasourceName() + "\")");
        try {
            while (true) {
                try {
                    receipt.waitUntilCompletion();
                    break;
                }
                catch (InterruptedException e) {
                    getLog().debug("Interrupted whilst waiting for annotations retrieval to finish, continuing");
                }
            }
        }
        catch (RuntimeException e) {
            if (e.getCause() instanceof ZoomaLoadingException) {
                throw (ZoomaLoadingException) e.getCause();
            }
            else {
                throw new ZoomaLoadingException("A load task failed", e);
            }
        }
        finally {
            getLog().info("Annotation loading from all available annotation sources is complete");
        }
    }

    public void shutdown() {
        getLog().info("Shutting down " + getClass().getSimpleName() + "...");
        ctx.destroy();
        getLog().info("Shut down " + getClass().getSimpleName() + " OK.");
    }

    private void checkZoomaProperties() {
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
