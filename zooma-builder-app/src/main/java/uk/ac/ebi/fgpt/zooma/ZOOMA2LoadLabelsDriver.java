package uk.ac.ebi.fgpt.zooma;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import uk.ac.ebi.fgpt.zooma.env.ZoomaEnv;
import uk.ac.ebi.fgpt.zooma.env.ZoomaHome;
import uk.ac.ebi.fgpt.zooma.exception.ZoomaLoadingException;
import uk.ac.ebi.fgpt.zooma.service.LoadOLSLabelsService;

/**
 * Created by olgavrou on 03/10/2016.
 */
public class ZOOMA2LoadLabelsDriver {

    public static void main(String[] args) {
        if (args.length > 0) {
            System.err.println("This application does not take any arguments; configuration can be updated in " +
                    "$ZOOMA_HOME/config/zooma.properties");
        }
        else {
            try {
                ZOOMA2LoadLabelsDriver driver = new ZOOMA2LoadLabelsDriver();
                driver.invoke();
            }
            catch (ZoomaLoadingException e) {
                System.err.println("ZOOMA did not complete successfully: " + e.getMessage());
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    private Logger log = LoggerFactory.getLogger(getClass());

    private Logger getLog() {
        return log;
    }

    private ZOOMA2LoadLabelsDriver() {
        ZoomaEnv.configureZOOMAEnvironment();
        ZoomaHome.checkInstall();
    }

    public void invoke() throws ZoomaLoadingException {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
                "file:${zooma.home}/config/spring/zooma-build.xml",
                "file:${zooma.home}/config/spring/zooma-dao.xml",
                "file:${zooma.home}/config/spring/zooma-load.xml",
                "classpath*:zooma-annotation-dao.xml");
        LoadOLSLabelsService loader = ctx.getBean("loadOLSLabelsService", LoadOLSLabelsService.class);

        try {
            getLog().info("Loading labels to RDF graph for all annotations semantic tags");
            loader.findAndLoad();
        } catch (RuntimeException e) {
            if (e.getCause() instanceof ZoomaLoadingException) {
                throw (ZoomaLoadingException) e.getCause();
            }
            else {
                throw new ZoomaLoadingException(e);
            }
        } finally {
            getLog().info("Loading labels to RDF graph for all annotations semantic tags is complete");
            getLog().info("Shutting down " + getClass().getSimpleName() + "...");
            ctx.destroy();
            getLog().info("Shut down " + getClass().getSimpleName() + " OK.");
        }
    }

}
