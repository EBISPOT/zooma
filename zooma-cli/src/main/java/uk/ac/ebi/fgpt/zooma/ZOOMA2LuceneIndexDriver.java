package uk.ac.ebi.fgpt.zooma;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import uk.ac.ebi.fgpt.zooma.service.StatusService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
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
        try {
            int statusCode = parseArguments(args);
            if (statusCode == 0) {
                ZOOMA2LuceneIndexDriver driver = new ZOOMA2LuceneIndexDriver();
                driver.generateIndices();

                // test for completion
                String preamble = "Building ZOOMA indices...";
                System.out.print(preamble);
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
            else {
                System.exit(statusCode);
            }
        }
        catch (IOException e) {
            System.out.println();
            System.err.println("A read/write problem occurred: " + e.getMessage());
            System.exit(1);
        }
        catch (Exception e) {
            System.err.println("ZOOMA did not complete successfully: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static int parseArguments(String[] args) throws IOException {
        CommandLineParser parser = new GnuParser();
        HelpFormatter help = new HelpFormatter();
        Options options = bindOptions();

        int parseArgs = 0;
        try {
            CommandLine cl = parser.parse(options, args, true);

            // check for mode help option
            if (cl.hasOption("")) {
                // print out mode help
                help.printHelp("zooma", options, true);
                parseArgs += 1;
            }
            else {
                // check -d required option
                if (cl.hasOption("d")) {
                    // get directory argument
                    File d = new File(cl.getOptionValue("d"));
                    // create if the directory doesn't exist
                    if (!d.getAbsoluteFile().exists()) {
                        System.out.print("Creating index directory '" + d.getAbsolutePath() + "'...");
                        if (d.mkdirs()) {
                            System.out.println("ok!");
                        }
                        else {
                            System.out.println("failed.");
                        }
                    }
                    // set java property for zooma.home
                    System.setProperty("zooma.home", d.getAbsolutePath());
                }
            }
        }
        catch (ParseException e) {
            System.err.println("Failed to read supplied arguments (" + e.getMessage() + ")");
            help.printHelp("zooma", options, true);
            parseArgs += 1;
        }
        return parseArgs;
    }

    private static Options bindOptions() {
        Options options = new Options();

        // help
        Option helpOption = new Option("h", "help", false, "Print the help");
        options.addOption(helpOption);

        // add directory options
        Option dirOption = new Option(
                "d",
                "dir",
                true,
                "Index Directory - the directory where you wish to create ZOOMA lucene indices");
        dirOption.setRequired(true);
        options.addOption(dirOption);

        return options;
    }

    private final File zoomaHome;
    private final StatusService zoomaStatusService;

    private boolean started = false;

    public ZOOMA2LuceneIndexDriver() {
        // ref zoomaHome from system property
        zoomaHome = new File(System.getProperty("zooma.home"));

        // load spring config to initialize lucene indexer
        System.out.print("Loading configuration...");
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
                "zooma-dao.xml",
                "zooma-indexer.xml",
                "zooma-lucene.xml",
                "zooma-service.xml");
        zoomaStatusService = ctx.getBean("statusService", StatusService.class);
        System.out.println("ok!");
    }

    public void generateIndices() throws IOException {
        if (zoomaStatusService != null) {
            if (zoomaHome.exists()) {
                if (zoomaStatusService.checkStatus()) {
                    System.out.println("ZOOMA lucene indices already exist in " + zoomaHome.getAbsolutePath());
                    // backup old index
                    String dateStr = new SimpleDateFormat("yyyyMMdd").format(new Date());
                    String backupFileName = zoomaHome.getName().concat(".backup.").concat(dateStr);
                    File backupFile = new File(zoomaHome.getAbsoluteFile().getParentFile(), backupFileName);

                    Path oldZoomaHome = zoomaHome.toPath();
                    Path newZoomaHome = backupFile.toPath();

                    if (Files.exists(newZoomaHome))  {
                        System.out.print(
                                "Backing up " + oldZoomaHome.toString() + " to " + newZoomaHome.toString() + "...");
                        Files.move(oldZoomaHome,
                                newZoomaHome,
                                StandardCopyOption.REPLACE_EXISTING,
                                StandardCopyOption.ATOMIC_MOVE);
                        System.out.println("ok!");
                        System.out.println("ZOOMA lucene indices will now be created afresh in " +
                                zoomaHome.getAbsolutePath());
                    }
                    else {
                        System.out.println("Backup already exists for today, clearing " + oldZoomaHome.toString());
                    }


                }
                else {
                    System.out.println("ZOOMA lucene indices will be created in " + zoomaHome.getAbsolutePath());
                }
            }
            else {
                System.out.println("ZOOMA lucene indices will be created in a new directory, " +
                        zoomaHome.getAbsolutePath());
            }
            zoomaStatusService.reinitialize();
            started = true;
        }
    }

    public boolean isComplete() {
        return started && zoomaHome.exists() && zoomaStatusService != null && zoomaStatusService.checkStatus();
    }
}
