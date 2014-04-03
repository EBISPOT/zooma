package uk.ac.ebi.fgpt.zooma.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

/**
 * User: jmcmurry
 * Date: 27/03/2014
 * Time: 15:27
 */
public class ZoomageDriver {

    private int minStringLength;  // todo: move this to the zooma search rest client

    private Float cutoffScoreAutomaticCuration;
    private Float cutoffScoreManualCuration;
    private Float cutoffPctAutomaticCuration;
    private Float cutoffPctManualCuration;

    private String magetabAccession;
    private boolean olsShortIds;
    private String compoundAnnotationDelimiter;
    private String fileDelimiter;

    private String exclusionProfilesResource;
    private String mageTabAccessionsResource;
    private boolean overwriteValues;
    private boolean overwriteAnnotations;
    private boolean stripLegacyAnnotations;

    private final Logger log = LoggerFactory.getLogger(ZoomageDriver.class);
    private boolean addCommentsToSDRF;
    private String magetabBasePath;
    private String zoomaPath;
    private String limpopoPath;
    private String outfileBasePath;

    public static void main(String[] args) {

        // see if user has specified a properties file path
        String propertiesFilePath = getPropertiesFilePath(args, "-1");

        // if not, use the local properties file
        if (propertiesFilePath.equals("")) propertiesFilePath = "zoomage-defaults.properties";

        ZoomageDriver zoomageDriver = new ZoomageDriver(args, propertiesFilePath, "zoomage");
        zoomageDriver.run();
    }

    public ZoomageDriver(String[] args, String propertiesFilePath, String programName) {

        // instantiate a new parser using the program arguments, the properties file and the name of the program
        OptionsParser optionsParser = new OptionsParser(args, propertiesFilePath, programName);

        // if errors were encountered
        if (optionsParser.getStatusCode() != 0) {
            getLog().error("Parser could not be created with the supplied information. " + Arrays.asList(args) + "zoomage-defaults.properties");
        }

        // if these loaded successfully, process each of the options and assign them to variables.
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

        ZoomageMagetabParser zoomageParser = new ZoomageMagetabParser(limpopoPath, magetabBasePath,
                outfileBasePath, overwriteValues, overwriteAnnotations, stripLegacyAnnotations, addCommentsToSDRF);

        ZoomageUtils.initialise(zoomaPath, cutoffScoreAutomaticCuration, cutoffPctAutomaticCuration, minStringLength, exclusionProfilesResource, fileDelimiter, olsShortIds, compoundAnnotationDelimiter);

        HashSet<String> mageTabAccessions = new HashSet<>();

        if (magetabAccession == null || magetabAccession.equals("")) {
            mageTabAccessions = parseMagetabAccessions(mageTabAccessionsResource);
        } else mageTabAccessions.add(magetabAccession);

        System.out.println();

        for (String accession : mageTabAccessions) {
            System.out.println("----------------------------");
            System.out.println("Processing " + accession);
            try {
                boolean success = zoomageParser.runFromFilesystem(accession);
                if (success) {
                    getLog().info("Zoomage completed successfully for " + accession);
                    printLog(accession);
                } else {
                    getLog().info("Zoomage encountered errors for " + accession);
                }
            } catch (Error e) {
                getLog().info("Zoomage encountered errors for " + accession);
                e.printStackTrace();
            }
        }
    }

    public void printLog(String accession) {

        log.info("Printing log for " + accession + "...");

        ZoomageLogger zoomageLogger = new ZoomageLogger(cutoffScoreAutomaticCuration, minStringLength, cutoffPctAutomaticCuration, olsShortIds, fileDelimiter, overwriteValues, overwriteAnnotations, stripLegacyAnnotations, addCommentsToSDRF, zoomaPath, limpopoPath);
        zoomageLogger.printLog(outfileBasePath, accession);
    }


    private void createOptions(OptionsParser optionsParser) {

        minStringLength = optionsParser.processIntOption("minStringLength", false, true, "r", "Zooma minimum string length for input, below which input is ignored from zoomifications");

        cutoffScoreAutomaticCuration = optionsParser.processFloatOption("cutoffScoreAutomaticCuration", false, true, "s", "Zooma cutoff score");
        cutoffPctAutomaticCuration = optionsParser.processFloatOption("cutoffPctAutomaticCuration", false, true, "p", "Zooma minimum percentage, below which input is ignored from zoomifications");
        cutoffScoreManualCuration = optionsParser.processFloatOption("cutoffScoreManualCuration", false, false, "w", "This value is currently ignored");
        cutoffPctManualCuration = optionsParser.processFloatOption("cutoffPctManualCuration", false, false, "b", "This value is currently ignored");

        magetabAccession = optionsParser.processStringOption("magetabAccession", false, false, "a", "MAGE-tab accession number, eg E-MTAB-513. This value is " +
                "required unless a file of magetab accessions is provided instead.");
        olsShortIds = optionsParser.processBooleanOption("olsShortIds", false, true, "u", "Whether to use OLS short IDs in Zoomified Magetab. OLS ShortIDs use a colon delimiter.");

        compoundAnnotationDelimiter = optionsParser.processStringOption("compoundAnnotationDelimiter", false, true, "d", "Delimiter to use between elements of a compound annotations within a single cell. Eg (heart and lung)");
        fileDelimiter = optionsParser.processStringOption("fileDelimiter", false, true, "f", "Delimiter to use in log file output. There currently no way to offer tab output.");
        exclusionProfilesResource = optionsParser.processStringOption("exclusionProfilesResource", false, true, "e", "Filename for exclusion profiles; this file must reside in the folder corresponding to the basepath for inputs.");
        overwriteValues = optionsParser.processBooleanOption("overwriteValues", false, true, "v", "Whether to overwrite values based on automatic zoomifications.");
        overwriteAnnotations = optionsParser.processBooleanOption("overwriteAnnotations", false, true, "t", "Whether to overwrite annotations based on zoomifications. On its own, selecting this option will only strip legacy annotations if a Zooma result is found.");
        stripLegacyAnnotations = optionsParser.processBooleanOption("stripLegacyAnnotations", false, true, "x", "This will strip all legacy annotations, whether or not a Zooma result is found.");
        addCommentsToSDRF = optionsParser.processBooleanOption("addCommentsToSDRF", false, true, "c", "Directly within SDRF output, add to comments in order to indicate what changes have been made. This value is currently ignored");

        magetabBasePath = optionsParser.processStringOption("magetabBasePath", false, true, "i", "Basepath where raw input magetab files can be found.");
        mageTabAccessionsResource = optionsParser.processStringOption("mageTabAccessionsResource", false, true, "m", "Filename where raw input magetab files can be found. This file must reside in the folder corresponding to the basepath for inputs. This is required unless a single magetab accession is specified.");
        zoomaPath = optionsParser.processStringOption("zoomaPath", false, true, "z", "Path for version of Zooma to use. Note that at present, the zooma API differs between prod / dev environments, so you may encounter errors.");
        limpopoPath = optionsParser.processStringOption("limpopoPath", false, true, "l", "Path for version of Limpopo to use.");
        outfileBasePath = optionsParser.processStringOption("outfileBasePath", false, true, "o", "Fully validated base path for output files. You must include the trailing slash.");

        optionsParser.processStringOption("properties file path", false, false, "1", "To completely override the local default properties file, provide Fully validated path of a replacement file.");

        if ((magetabAccession == null || magetabAccession.isEmpty()) && (mageTabAccessionsResource == null || mageTabAccessionsResource.isEmpty())) {
            throw new IllegalArgumentException("Either a magetab accession or file of magetab accessions (magetab accession resource) must be provided.");
        }

    }


    private HashSet<String> parseMagetabAccessions(String mageTabAccessionsResource) {
        HashSet<String> mageTabAccessions = new HashSet<String>();

        // read sources from file
        try {
            InputStream in = ZoomageDriver.class.getClassLoader().getResourceAsStream(mageTabAccessionsResource);
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
        }

        return mageTabAccessions;
    }

    public Logger getLog() {
        return log;
    }

    private static String getPropertiesFilePath(String[] args, String prefix) {

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
