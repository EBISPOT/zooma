package uk.ac.ebi.fgpt.zooma;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import uk.ac.ebi.fgpt.zooma.env.ZoomaEnv;
import uk.ac.ebi.fgpt.zooma.env.ZoomaHome;
import uk.ac.ebi.fgpt.zooma.service.StatusService;
import uk.ac.ebi.fgpt.zooma.util.ProgressLogger;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A command line client for ZOOMA 2 that generates lucene indices that are required by the webapp.  You can use this
 * client to pre-generate indices prior to webapp start up.
 *
 * @author Tony Burdett
 * @date 28/02/13
 */
public class ZOOMA2LuceneIndexDriver extends ZOOMA2BackingUpDriver {
    public static void main(String[] args) {
        if (args.length > 0) {
            System.err.println("This application does not take any arguments; configuration can be updated in " +
                                       "$ZOOMA_HOME/config/zooma.properties");
        }
        else {
            try {
                final ZOOMA2LuceneIndexDriver driver = new ZOOMA2LuceneIndexDriver();
                driver.createOutputDirectory();

                ProgressLogger progress = new ProgressLogger(System.out, "Building ZOOMA indices...", 15) {
                    @Override public boolean test() {
                        return !driver.isComplete();
                    }
                };

                progress.start();
                driver.generateIndices();

                Object lock = new Object();
                while (!driver.isComplete()) {
                    synchronized (lock) {
                        try {
                            lock.wait(15000);
                        }
                        catch (InterruptedException e) {
                            // do nothing
                        }
                    }
                }
                progress.ping();
                System.out.println("ZOOMA indices completed successfully.");
            }
            catch (IOException e) {
                System.err.println("ZOOMA did not complete successfully: " + e.getMessage());
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    private StatusService zoomaStatusService;
    private File luceneHome;

    private boolean started = false;

    public ZOOMA2LuceneIndexDriver() {
        ZoomaEnv.configureZOOMAEnvironment();
        ZoomaHome.checkInstall();
    }

    public void createOutputDirectory() throws IOException {
        luceneHome = FileSystems.getDefault().getPath(System.getProperty("zooma.data.dir"), "index", "lucene").toFile();
        if (luceneHome.exists()) {
            System.out.println("ZOOMA lucene indices already exist in " + luceneHome.getAbsolutePath());
            makeBackup(luceneHome, System.out);
            System.out.println("ZOOMA lucene indices will now be created afresh in " +
                                       luceneHome.getAbsolutePath());
        }
        else {
            System.out.println("ZOOMA lucene indices will be created in a new directory, " +
                                       luceneHome.getAbsolutePath());
        }
    }

    public void generateIndices() throws IOException {
        // load spring config to initialize lucene indexer
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
                "file:${zooma.home}/config/spring/zooma-dao.xml",
                "file:${zooma.home}/config/spring/zooma-build.xml",
                "file:${zooma.home}/config/spring/zooma-lucene.xml",
                "file:${zooma.home}/config/spring/zooma-service.xml");
        zoomaStatusService = ctx.getBean("statusService", StatusService.class);

        if (zoomaStatusService != null) {
            zoomaStatusService.reinitialize();
            started = true;
        }
    }

    public boolean isComplete() {
        return zoomaStatusService != null && started && zoomaStatusService.checkStatus() && luceneHome.exists();
    }
}
