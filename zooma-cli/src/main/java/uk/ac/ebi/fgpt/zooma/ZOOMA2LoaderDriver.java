package uk.ac.ebi.fgpt.zooma;

import uk.ac.ebi.fgpt.zooma.util.ZoomaLoaderInvoker;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A command line driver class for loading annotations into ZOOMA from configured datasources.
 *
 * @author Tony Burdett
 * @date 16/10/12
 */
public class ZOOMA2LoaderDriver {
    private static final Object lock = new Object();
    private static boolean invokerRunning;

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Wrong number of arguments - this application requires a single argument, " +
                                       "the location of the output directory to write RDF files to");
        }
        else {
            // first, try to backup old RDF directory
            File rdfHome = new File(args[0]);
            try {
                if (rdfHome.exists()) {
                    System.out.println("RDF directory " + rdfHome.getAbsolutePath() + " already exists");
                    // backup old RDF directory
                    String dateStr = new SimpleDateFormat("yyyyMMdd").format(new Date());
                    String backupFileName = rdfHome.getName().concat(".backup.").concat(dateStr);
                    File backupFile = new File(rdfHome.getAbsoluteFile().getParentFile(), backupFileName);

                    Path oldRDFHome = rdfHome.toPath();
                    Path newRDFHome = backupFile.toPath();

                    System.out.print(
                            "Backing up " + oldRDFHome.toString() + " to " + newRDFHome.toString() + "...");
                    if (Files.deleteIfExists(newRDFHome)) {
                        System.out.println("Backup from today exists, deleting " + newRDFHome.toString());
                    }
                    Files.move(oldRDFHome,
                               newRDFHome,
                               StandardCopyOption.REPLACE_EXISTING,
                               StandardCopyOption.ATOMIC_MOVE);
                    System.out.println("ok!");
                }
            }
            catch (IOException e) {
                System.err.println("Failed to backup Load failed: " + e.getMessage());
                e.printStackTrace();
                System.exit(1);
            }

            // set rdfHome as system property (zooma.rdf.outputPath)
            System.setProperty("zooma.rdf.outputPath", rdfHome.getAbsolutePath());

            // if backup passed, invoke load
            ZoomaLoaderInvoker invoker = ZoomaLoaderInvoker.getInvoker();
            int exitCode = -1;

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
                System.out.println("RDF files will be created in " + rdfHome.getAbsolutePath());
                invokerRunning = true;
                t.start();
                invoker.invoke();
                exitCode = 0;
            }
            catch (Exception e) {
                System.err.println("Load failed: " + e.getMessage());
                e.printStackTrace();
                exitCode = 1;
            }
            finally {
                invokerRunning = false;
                synchronized (lock) {
                    lock.notifyAll();
                }
                invoker.shutdown();
            }
            System.exit(exitCode);
        }
    }
}
