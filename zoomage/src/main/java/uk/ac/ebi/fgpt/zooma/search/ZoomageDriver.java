package uk.ac.ebi.fgpt.zooma.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URISyntaxException;
import java.util.*;

/**
 * User: jmcmurry
 * Date: 27/03/2014
 * Time: 15:27
 */
public class ZoomageDriver {

    private String appResourcesPath;
    private int minStringLength;  // todo: move this to the zooma search rest client

    private Float cutoffScoreAutomaticCuration;
    private Float cutoffScoreManualCuration;
    private Float cutoffPctAutomaticCuration;
    private Float cutoffPctManualCuration;

    private String magetabAccession;
    private boolean olsShortIds;
    private String compoundAnnotationDelimiter;
    private String fileDelimiter;

    private boolean overwriteValues;
    private boolean overwriteAnnotations;
    private boolean stripLegacyAnnotations;
    private boolean singleLogFileForBatch;

    private final Logger log = LoggerFactory.getLogger(ZoomageDriver.class);
    private boolean addCommentsToSDRF;
    private String magetabBasePath;
    private String zoomaPath;
    private String limpopoPath;
    private String outfileBasePath;

    private ZoomageMagetabParser zoomageParser;
    private ZoomageLogger zoomageLogger;

    public static void main(String[] args) {


        ZoomageDriver zoomageDriver = new ZoomageDriver(args, "zoomage");
        zoomageDriver.run();
    }

    public ZoomageDriver(String[] args, String programName) {


        // see if user has specified a properties file path
        this.appResourcesPath = getAppResourcesPath(args, "-1");

        if (!appResourcesPath.isEmpty() && !appResourcesPath.endsWith("/")) {
            appResourcesPath += "/";
        }

        String propertiesFilePath = appResourcesPath + "zoomage-defaults.properties";

        // instantiate a new parser using the program arguments, the properties file and the name of the program
        OptionsParser optionsParser = new OptionsParser(args, propertiesFilePath, programName);

        // if errors were encountered
        if (optionsParser.getStatusCode() != 0) {
            getLog().error("Parser could not be created with the supplied information. " + Arrays.asList(args) + "zoomage-defaults.properties");
        }

        // if these loaded successfully, getZoomaResults each of the options and assign them to variables.
        else {

            createOptions(optionsParser);

            // finalise the parser so that the args can be rechecked and the help can be printed if needed
            optionsParser.finalise("", args);

            // if the user just wanted to print the help, discontinue the program.
            if (optionsParser.getStatusCode() == 1) {
                getLog().info("Exiting Zoomage.");
                System.exit(0);
            } else if (optionsParser.getStatusCode() != 0) {
                getLog().error("Errors were encountered when loading args and defaults." + optionsParser.getStatusCode());
                throw new IllegalArgumentException("Errors were encountered when loading args and defaults.");
            }
        }
    }

    // separate method so that Zoomage Driver can also be instantiated
    public void run() {

        zoomageParser = new ZoomageMagetabParser(limpopoPath, magetabBasePath,
                outfileBasePath, overwriteValues, overwriteAnnotations, stripLegacyAnnotations, addCommentsToSDRF);

        ZoomageUtils.initialise(zoomaPath, cutoffScoreAutomaticCuration, cutoffPctAutomaticCuration, minStringLength, appResourcesPath, fileDelimiter, olsShortIds, compoundAnnotationDelimiter);

        HashSet<String> mageTabAccessions = new HashSet<>();

        if (magetabAccession == null || magetabAccession.equals("")) {
            mageTabAccessions = parseMagetabAccessions(appResourcesPath + "zoomage-accessions.properties");
        } else mageTabAccessions.add(magetabAccession);

        System.out.println();

        zoomageLogger = new ZoomageLogger(cutoffScoreAutomaticCuration, minStringLength, cutoffPctAutomaticCuration, olsShortIds, fileDelimiter, overwriteValues, overwriteAnnotations, stripLegacyAnnotations, addCommentsToSDRF, zoomaPath, limpopoPath);


        // for each accession
        for (String accession : mageTabAccessions) {
            processSingleAccession(accession);
            // Whether or not logs are batched into one file,
            // clear cache after processing each accession, otherwise log output will be cumulative each time and also suboptimal since
            // duplicates between accessions are not stored (the key is type:value only)
            ZoomageUtils.clearMasterCache();
        }

        if (singleLogFileForBatch) {
            zoomageLogger.printLogRowsToFile(outfileBasePath, "Batch");
        }
    }

    private void processSingleAccession(String accession) {

        System.out.println("----------------------------");
        System.out.println("Processing " + accession);

        boolean success = zoomageParser.runFromFilesystem(accession);

        try {
            if (success) {
                getLog().info("Zoomage completed successfully for " + accession);

                HashMap<String, TransitionalAttribute> masterCache = ZoomageUtils.getMasterCache();
                ArrayList<String> logFileRowsForSingleAccession = zoomageLogger.formatCacheAsLogFileRows(masterCache);
                logSingleAccession(logFileRowsForSingleAccession, accession);

            } else {
                getLog().error("Zoomage encountered errors for " + accession);
                logSingleAccession(zoomageLogger.formatErrorAsLogFileRow(accession), accession);
            }
        } catch (Error e) {
            getLog().error("Zoomage encountered errors for " + accession);
            e.printStackTrace();
            logSingleAccession(zoomageLogger.formatErrorAsLogFileRow(accession), accession);
        }
    }

    private void logSingleAccession(ArrayList<String> logFileRowsForSingleAccession, String accession) {
        if (!singleLogFileForBatch) {
            zoomageLogger.printLogRowsToFile(outfileBasePath, accession);
        } else zoomageLogger.addLogFileRowsForSingleAccession(logFileRowsForSingleAccession);
    }


    private void createOptions(OptionsParser optionsParser) {

        optionsParser.processStringOption("appResourcesFolderPath", false, false, "1", "To completely override the local default appresources folder, provide Fully validated path of a replacement folder. " +
                "This folder must contain three documents: a properties file called zoomage-defaults.properties, an exclusions list called zoomage-exclusions.tsv " +
                "and a list of magetab accession numbers to parse (zoomage-accessions.properties). Instead of providing a list of accessions, you may optionally pass in the " +
                "accession via commandline argument -a.");

        minStringLength = optionsParser.processIntOption("minStringLength", false, true, "r", "Zooma minimum string length for input, below which input is ignored from zoomifications");

        cutoffScoreAutomaticCuration = optionsParser.processFloatOption("cutoffScoreAutomaticCuration", false, true, "s", "Zooma cutoff score");
        cutoffPctAutomaticCuration = optionsParser.processFloatOption("cutoffPctAutomaticCuration", false, true, "p", "Zooma minimum percentage, below which input is ignored from zoomifications");
        cutoffScoreManualCuration = optionsParser.processFloatOption("cutoffScoreManualCuration", false, false, "w", "This value is currently ignored");
        cutoffPctManualCuration = optionsParser.processFloatOption("cutoffPctManualCuration", false, false, "b", "This value is currently ignored");

        magetabAccession = optionsParser.processStringOption("magetabAccession", false, false, "a", "MAGE-tab accession number, eg E-MTAB-513. This value is " +
                "required unless a file of magetab accessions is provided instead.");
        olsShortIds = optionsParser.processBooleanOption("olsShortIds", false, true, "u", "Whether to use OLS short IDs in Zoomified Magetab. OLS ShortIDs use a colon delimiter.");

        compoundAnnotationDelimiter = optionsParser.processStringOption("compoundAnnotationDelimiter", false, true, "d", "Delimiter to use between elements of a compound annotations within a single cell. Eg (heart and lung)");
        fileDelimiter = optionsParser.processStringOption("fileDelimiter", false, true, "f", "Delimiter to use in log file output. For tab, type 'tab' without quotes.");
        if (fileDelimiter.equals("tab")) fileDelimiter = "\t";
        overwriteValues = optionsParser.processBooleanOption("overwriteValues", false, true, "v", "Whether to overwrite values based on automatic zoomifications.");
        overwriteAnnotations = optionsParser.processBooleanOption("overwriteAnnotations", false, true, "t", "Whether to overwrite annotations based on zoomifications. On its own, selecting this option will only strip legacy annotations if a Zooma result is found.");
        stripLegacyAnnotations = optionsParser.processBooleanOption("stripLegacyAnnotations", false, true, "x", "This will strip all legacy annotations, whether or not a Zooma result is found.");
        addCommentsToSDRF = optionsParser.processBooleanOption("addCommentsToSDRF", false, true, "c", "Directly within SDRF output, add to comments in order to indicate what changes have been made. This value is currently ignored");
        singleLogFileForBatch = optionsParser.processBooleanOption("singleLogFileForBatch", false, true, "e", "Directly within SDRF output, add to comments in order to indicate what changes have been made. This value is currently ignored");

        magetabBasePath = optionsParser.processStringOption("magetabBasePath", false, true, "i", "Basepath where raw input magetab files can be found.");
        zoomaPath = optionsParser.processStringOption("zoomaPath", false, true, "z", "Path for version of Zooma to use. Note that at present, the zooma API differs between prod / dev environments, so you may encounter errors.");
        limpopoPath = optionsParser.processStringOption("limpopoPath", false, true, "l", "Path for version of Limpopo to use.");
        outfileBasePath = optionsParser.processStringOption("outfileBasePath", false, true, "o", "Fully validated base path for output files. You must include the trailing slash.");
        if (!outfileBasePath.endsWith("/")) outfileBasePath += "/";


//        mageTabAccessionsResource = optionsParser.processStringOption("mageTabAccessionsResource", false, true, "m", "Filename where raw input magetab files can be found. This file must reside in the folder corresponding to the basepath for inputs. This is required unless a single magetab accession is specified.");
    }


    private HashSet<String> parseMagetabAccessions(String mageTabAccessionsResource) {
        HashSet<String> mageTabAccessions = new HashSet<String>();

        // read sources from file
        try {
            InputStream in = OptionsParser.getInputStreamFromFilePath(ZoomageDriver.class, mageTabAccessionsResource);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String accession;
            while ((accession = reader.readLine()) != null) {
                if (!accession.startsWith("#") && !accession.isEmpty()) {
                    mageTabAccessions.add(accession);
                }
            }
        } catch (FileNotFoundException e) {
            getLog().error("Failed to load properties: could not locate file '" + mageTabAccessionsResource + "'.  ");
        } catch (IOException e) {
            getLog().error("Failed to load properties: could not read file '" + mageTabAccessionsResource + "'.  ");
        } catch (URISyntaxException e) {
            e.printStackTrace();  //todo:
        }

        return mageTabAccessions;
    }

    public Logger getLog() {
        return log;
    }

    private static String getAppResourcesPath(String[] args, String prefix) {

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals(prefix)) {
                if (args.length > i) {
                    return args[i + 1];
                }
            }
        }

        return "";
    }
}
