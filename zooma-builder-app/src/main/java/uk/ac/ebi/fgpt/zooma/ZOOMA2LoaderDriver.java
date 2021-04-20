package uk.ac.ebi.fgpt.zooma;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import uk.ac.ebi.fgpt.zooma.datasource.CSVAnnotationDAO;
import uk.ac.ebi.fgpt.zooma.env.ZoomaEnv;
import uk.ac.ebi.fgpt.zooma.env.ZoomaHome;
import uk.ac.ebi.fgpt.zooma.exception.ZoomaLoadingException;
import uk.ac.ebi.fgpt.zooma.service.DataLoadingService;
import uk.ac.ebi.fgpt.zooma.util.ProgressLogger;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * A command line driver class for loading annotations into ZOOMA from configured datasources.
 *
 * @author Tony Burdett
 * @date 16/10/12
 */
public class ZOOMA2LoaderDriver extends ZOOMA2BackingUpDriver {
    public static void main(String[] args) {
        if (args.length > 0) {
            System.err.println("This application does not take any arguments; configuration can be updated in " +
                                       "$ZOOMA_HOME/config/zooma.properties");
        }
        else {
            try {
                ZOOMA2LoaderDriver driver = new ZOOMA2LoaderDriver();
                driver.createOutputDirectory();
                driver.invoke();
            }
            catch (ZoomaLoadingException | IOException e) {
                System.err.println("ZOOMA did not complete successfully: " + e.getMessage());
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    private boolean invokerRunning;

    private Logger log = LoggerFactory.getLogger(getClass());

    private Logger getLog() {
        return log;
    }

    private ZOOMA2LoaderDriver() {
        ZoomaEnv.configureZOOMAEnvironment();
        ZoomaHome.checkInstall();
    }

    public void createOutputDirectory() throws IOException {
        // first, try to backup old RDF directory
        File rdfHome = new File(System.getProperty("zooma.data.dir"), "rdf");
        if (rdfHome.exists()) {
            System.out.println("RDF directory already exists at " + rdfHome.getAbsolutePath());
            makeBackup(rdfHome, System.out);
            System.out.println("RDF files will now be created afresh in " + rdfHome.getAbsolutePath());
        }
        else {
            System.out.println("RDF files will be created in a new directory, " + rdfHome.getAbsolutePath());
        }
    }

    public void invoke() throws ZoomaLoadingException {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
                "file:${zooma.home}/config/spring/zooma-build.xml",
                "file:${zooma.home}/config/spring/zooma-dao.xml",
                "file:${zooma.home}/config/spring/zooma-load.xml",
                "classpath*:zooma-annotation-dao.xml");
        DataLoadingService loader = ctx.getBean("dataLoadingService", DataLoadingService.class);

        //get the CSVAnnotationDAOs and load the data for each one
        Map<String, CSVAnnotationDAO> annotationDAOs = ctx.getBeansOfType(CSVAnnotationDAO.class);
        for (String datasource : annotationDAOs.keySet()){
            try {
                CSVAnnotationDAO csvAnnotationDAO = annotationDAOs.get(datasource);
                csvAnnotationDAO.loadDataFromCSV();
            } catch (Exception e) {
                getLog().error("Could not load datasource: " + datasource, e);
            }
        }
        getLog().debug("Found and loaded " + loader.getAvailableDatasources().size() + " AnnotationDAOs");
        ProgressLogger progress = new ProgressLogger(System.out, "Loading annotations...", 15) {
            @Override public boolean test() {
                return invokerRunning;
            }
        };

        try {
            invokerRunning = true;
            progress.start();
            getLog().info("Loading annotations from available sources and converting to RDF");
            DataLoadingService.Receipt receipt = loader.load();
            getLog().debug("Received receipt '" + receipt.getID() + "' " +
                                   "(datasource \"" + receipt.getDatasourceName() + "\")");
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
                throw new ZoomaLoadingException(e);
            }
        }
        finally {
            getLog().info("Annotation loading from all available annotation sources is complete");
            invokerRunning = false;
            progress.ping();
            getLog().info("Shutting down " + getClass().getSimpleName() + "...");
            ctx.destroy();
            getLog().info("Shut down " + getClass().getSimpleName() + " OK.");
        }
    }
}
