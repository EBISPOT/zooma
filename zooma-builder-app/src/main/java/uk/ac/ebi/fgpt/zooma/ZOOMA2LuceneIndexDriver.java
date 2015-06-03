package uk.ac.ebi.fgpt.zooma;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import uk.ac.ebi.fgpt.zooma.env.ZoomaEnv;
import uk.ac.ebi.fgpt.zooma.env.ZoomaHome;
import uk.ac.ebi.fgpt.zooma.service.StatusService;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A command line client for ZOOMA 2 that generates lucene indices that are required by the webapp.  You can use this
 * client to pre-generate indices prior to webapp start up.
 *
 * @author Tony Burdett
 * @date 28/02/13
 */
public class ZOOMA2LuceneIndexDriver {
    public static void main(String[] args) {
        if (args.length > 0) {
            System.err.println("This application does not take any arguments; configuration can be updated in " +
                                       "$ZOOMA_HOME/config/zooma.properties");
        }
        else {
            try {
                ZOOMA2LuceneIndexDriver driver = new ZOOMA2LuceneIndexDriver();
                driver.createOutputDirectory();

                // trigger index building
                String preamble = "Building ZOOMA indices...";
                System.out.print(preamble);
                driver.generateIndices();

                // test for completion
                int chars = preamble.length();
                final Object lock = new Object();
                while (!driver.isComplete()) {
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
            if (zoomaStatusService.checkStatus()) {
                System.out.println("ZOOMA lucene indices already exist in " + luceneHome.getAbsolutePath());
                // backup old index
                String dateStr = new SimpleDateFormat("yyyyMMdd").format(new Date());
                String backupFileName = luceneHome.getName().concat(".backup.").concat(dateStr);
                File backupFile = new File(luceneHome.getAbsoluteFile().getParentFile(), backupFileName);

                Path oldZoomaHome = luceneHome.toPath();
                Path newZoomaHome = backupFile.toPath();

                if (!Files.exists(newZoomaHome)) {
                    System.out.print(
                            "Backing up " + oldZoomaHome.toString() + " to " + newZoomaHome.toString() + "...");
                    Files.move(oldZoomaHome,
                               newZoomaHome,
                               StandardCopyOption.REPLACE_EXISTING,
                               StandardCopyOption.ATOMIC_MOVE);
                    System.out.println("ok!");
                }
                else {
                    System.out.print(
                            "Backup already exists for today, clearing " + oldZoomaHome.toString() + "...");
                    Files.walkFileTree(oldZoomaHome, new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                                throws IOException {
                            Files.delete(file);
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                                throws IOException {
                            Files.delete(dir);
                            return FileVisitResult.CONTINUE;
                        }
                    });
                    System.out.println("ok!");
                }
                System.out.println("ZOOMA lucene indices will now be created afresh in " +
                                           luceneHome.getAbsolutePath());
            }
            else {
                System.out.println("ZOOMA lucene indices will be created in " + luceneHome.getAbsolutePath());
            }
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
