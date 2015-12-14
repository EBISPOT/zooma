package uk.ac.ebi.fgpt.zooma;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import uk.ac.ebi.fgpt.zooma.datasource.ZoomaDAO;
import uk.ac.ebi.fgpt.zooma.env.ZoomaEnv;
import uk.ac.ebi.fgpt.zooma.env.ZoomaHome;
import uk.ac.ebi.fgpt.zooma.exception.ZoomaLoadingException;
import uk.ac.ebi.fgpt.zooma.service.DataLoadingService;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

/**
 * A command line driver class for loading annotations into ZOOMA from configured datasources.
 *
 * @author Tony Burdett
 * @date 16/10/12
 */
public class ZOOMA2LoaderDriver {
    public static void main(String[] args) {

        System.out.println("Youuuuuuuhooooooooooo");
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

    private final Object lock = new Object();
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
            // backup old RDF directory
            String dateStr = new SimpleDateFormat("yyyyMMdd").format(new Date());
            String backupFileName = rdfHome.getName().concat(".backup.").concat(dateStr);
            File backupFile = new File(rdfHome.getAbsoluteFile().getParentFile(), backupFileName);

            Path oldRDFHome = rdfHome.toPath();
            Path newRDFHome = backupFile.toPath();

            if (!Files.exists(newRDFHome)) {
                System.out.print(
                        "Backing up " + oldRDFHome.toString() + " to " + newRDFHome.toString() + "...");
                Files.move(oldRDFHome,
                           newRDFHome,
                           StandardCopyOption.REPLACE_EXISTING,
                           StandardCopyOption.ATOMIC_MOVE);
                System.out.println("ok!");
            }
            else {
                System.out.print(
                        "Backup already exists for today, clearing " + oldRDFHome.toString() + "...");
                Files.walkFileTree(oldRDFHome, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }
                });
                System.out.println("ok!");
            }
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
        getLog().debug("Found and loaded " + loader.getAvailableDatasources().size() + " AnnotationDAOs");
        System.out.println("Found and loaded " + loader.getAvailableDatasources().size() + " AnnotationDAOs");

        Collection<ZoomaDAO> zoomaDAOs =  loader.getAvailableDatasources();
        for(ZoomaDAO zoomaDAO : zoomaDAOs ){
            System.out.println("zoomaDAO.getDatasourceName() = " + zoomaDAO.getDatasourceName());
        }


        // create a thread to print to standard out while invoker is running
        final Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                String preamble = "Loading annotations...";
                System.out.print(preamble);
                int chars = preamble.length();

                while (invokerRunning) {
                    synchronized (lock) {
                        chars++;
                        if (chars % 40 == 0) {
                            System.out.println(".");
                        }
                        else {
                            System.out.print(".");
                        }
                        try {
                            lock.wait(15000);
                        }
                        catch (InterruptedException e) {
                            // do nothing
                        }
                    }
                }
                System.out.println("ok!");
            }
        });

        try {
            invokerRunning = true;
            t.start();
            getLog().info("Loading annotations from available sources and converting to RDF");
            DataLoadingService.Receipt receipt = loader.load();

            getLog().debug("Received receipt '" + receipt.getID() + "' " +
                    "(datasource \"" + receipt.getDatasourceName() + "\")");

            System.out.println("BOnjour");
            System.out.println("Received receipt '" + receipt.getID() + "' " +
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
                throw new ZoomaLoadingException("A load task failed", e);
            }
        }
        finally {
            getLog().info("Annotation loading from all available annotation sources is complete");
            invokerRunning = false;
            synchronized (lock) {
                lock.notifyAll();
            }
            getLog().info("Shutting down " + getClass().getSimpleName() + "...");
            ctx.destroy();
            getLog().info("Shut down " + getClass().getSimpleName() + " OK.");
        }
    }
}
