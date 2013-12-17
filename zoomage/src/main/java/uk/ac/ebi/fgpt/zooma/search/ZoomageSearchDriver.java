package uk.ac.ebi.fgpt.zooma.search;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.zooma.model.AnnotationSummary;
//import org.springframework.context.support.ClassPathXmlApplicationContext;
//import uk.ac.ebi.fgpt.zooma.atlas.ZOOMAPropertySampler;
//import uk.ac.ebi.fgpt.zooma.atlas.ZoomaAtlasDAO;
//import uk.ac.ebi.fgpt.zooma.io.ZOOMAInputParser;
//import uk.ac.ebi.fgpt.zooma.io.ZOOMAReportRenderer;
//import uk.ac.ebi.fgpt.zooma.util.OntologyLabelMapper;

import java.io.*;
import java.net.URI;
import java.util.*;

/**
 * A command line client for ZOOMA 2 that takes a list of properties (values, constrained by an optional type) and
 * produces a list of ontology mappings for those properties.
 *
 * @author Julie McMurry adapted from Tony Burdett
 * @date 7 October 2013
 */
public class ZoomageSearchDriver {

    private static float _cutoffScore;
    private static int _minStringLength;  // todo: move this to the zooma search rest client
    private static float _cutoffPercentage;
    private static String _magetabAccession;
    private static boolean _olsShortIds;
    private static final String _excludedTypesResource = "zoomage-propertytype-exclusions.properties";
    private static boolean _overwriteValues;
    private static boolean _overwriteAnnotations;
    private static final Logger log = LoggerFactory.getLogger(ZoomageSearchDriver.class);

    private static boolean _addCommentsToSDRF;


    public static void main(String[] args) {
        try {
            int statusCode = parseArguments(args);
            if (statusCode == 0) {

                ZoomaRESTClient zoomaRESTClient = new ZoomaRESTClient(_minStringLength, _cutoffPercentage, _cutoffScore, _olsShortIds, _overwriteAnnotations, _excludedTypesResource);
                ZoomageMagetabParser zoomageParser = new ZoomageMagetabParser();
                zoomageParser.runFromFilesystem(_magetabAccession, zoomaRESTClient, _overwriteValues, _overwriteAnnotations, _addCommentsToSDRF);
                zoomageParser.logZoomifiedAnnotations(_magetabAccession, zoomaRESTClient, _olsShortIds);

                getLog().info("Zoomage completed successfully.");

            } else {
                System.exit(statusCode);
            }
        } catch (IOException e) {
            System.err.println("A read/write problem occurred: " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Zoomage did not complete successfully: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    //parse commandline arguments
    private static int parseArguments(String[] args) throws IOException {

        CommandLineParser parser = new GnuParser();
        HelpFormatter help = new HelpFormatter();
        Options options = bindOptions();

        int statusCode = 0;
        try {
            Properties defaults = new Properties();
            InputStream in = ZoomageSearchDriver.class.getClassLoader().getResourceAsStream("zoomage-defaults.properties");
            if (in == null) {
                throw new IOException("Failed to read default options from config/zoomage-defaults.properties");
            }
            defaults.load(in);

            CommandLine commandlineParser = parser.parse(options, args, true);

            // check for mode help option
            if (commandlineParser.hasOption("")) {
                // print out mode help
                help.printHelp("zoomage", options, true);
                statusCode += 1;
            } else {

                System.out.println("Your ZOOMA search will be run with the following supplied options...");

                // print the supplied options
                for (Option opt : commandlineParser.getOptions()) {
                    System.out.println("\t" + opt.getLongOpt() +
                            (opt.hasArg() ? ": " + opt.getValue() : "") +
                            " (" + opt.getArgName() + ")");
                }

                // now that we have the user-supplied options, assign them to the class variables
                assignInputsToVariables(defaults, commandlineParser);
            }
        } catch (ParseException e) {
            System.out.println("Failed to read supplied arguments (" + e.getMessage() + ")");
            help.printHelp("zoomage", options, true);
            statusCode += 4;
        } catch (FileNotFoundException e) {
            System.out.println("Failed to read supplied arguments - file not found (" + e.getMessage() + ")");
            help.printHelp("zoomage", options, true);
            statusCode += 5;
        }
        return statusCode;
    }

    private static void assignInputsToVariables(Properties defaults, CommandLine cl) {
        // Magetab accession (required)
        _magetabAccession = cl.getOptionValue("a");

        // optional arguments, if not supplied use defaults

        //minimum string length
        if (cl.hasOption("m")) {
            String minStringLength = cl.getOptionValue("m");
            _minStringLength = Integer.parseInt(minStringLength);
        } else {
            _minStringLength = Integer.parseInt(defaults.getProperty("zoomage.minstringlength"));
            System.out.println("Using default ZOOMA minstringlength, " + _minStringLength);
        }

        // significance score cutoff
        if (cl.hasOption("s")) {
            String scoreOpt = cl.getOptionValue("s");
            _cutoffScore = Float.parseFloat(scoreOpt);
        } else {
            _cutoffScore = Float.parseFloat(defaults.getProperty("zoomage.significance.score"));
            System.out.println("Using default ZOOMA significance score, " + _cutoffScore);
        }

        // percentage cutoff
        if (cl.hasOption("c")) {
            String cutoffOpt = cl.getOptionValue("c");
            _cutoffPercentage = Float.parseFloat(cutoffOpt);
        } else {
            _cutoffPercentage = Float.parseFloat(defaults.getProperty("zoomage.cutoff.percentage"));
            System.out.println("Using default ZOOMA cutoff percentage, " + _cutoffPercentage);
        }

        // whether to output ols short IDs instead of full URIs
        if (cl.hasOption("o")) {
            _olsShortIds = cl.getOptionValue("o").startsWith("t") || cl.getOptionValue("o").startsWith("y");
        } else {
            String defaultOlsShortIDs = defaults.getProperty("zoomage.olsShortIds");
            _olsShortIds = defaultOlsShortIDs.startsWith("t") || defaultOlsShortIDs.startsWith("y") ||
                    defaultOlsShortIDs.startsWith("T") || defaultOlsShortIDs.startsWith("Y");
            System.out.println("Using default ZOOMA property for using OLS short IDs, " + _olsShortIds);
        }

        // whether to overwrite term source values
        if (cl.hasOption("w")) {
            String overwriteValues = cl.getOptionValue("w");
            _overwriteValues = isTrue(overwriteValues);
        } else {
            String defaultOverwriteValues = defaults.getProperty("zoomage.overwriteValues");
            _overwriteValues = isTrue(defaultOverwriteValues);
            System.out.println("Using default ZOOMA property for overwriting term source values, " + _overwriteValues);
        }

        // whether to overwrite term source ref and accessions
        if (cl.hasOption("x")) {
            String overwriteAnnotations = cl.getOptionValue("x");
            _overwriteAnnotations = isTrue(overwriteAnnotations);
        } else {
            String defaultOverwriteValues = defaults.getProperty("zoomage.overwriteAnnotations");
            _overwriteAnnotations = isTrue(defaultOverwriteValues);
            System.out.println("Using default ZOOMA property for overwriting term source refs and accessions, " + _overwriteAnnotations);
        }

        // whether to add comments to SDRF
        if (cl.hasOption("l")) {
            String addCommentsToSDRF = cl.getOptionValue("l");
            _addCommentsToSDRF = isTrue(addCommentsToSDRF);
        } else {
            String defaultAddCommentsToSDRF = defaults.getProperty("zoomage.addCommentsToSDRF");
            _addCommentsToSDRF = isTrue(defaultAddCommentsToSDRF);
            System.out.println("Using default ZOOMA property for adding comments to SDRF, " + _addCommentsToSDRF);
        }

    }

    private static boolean isTrue(String option) {
        return option.startsWith("t") || option.startsWith("y") ||
                option.startsWith("T") || option.startsWith("Y");
    }

    private static Options bindOptions() {
        Options options = new Options();

        // help
        Option helpOption = new Option("h", "help", false, "Print the help");
        options.addOption(helpOption);

        //accession
        Option magetabAccession = new Option(
                "a",
                "magetabAccession",
                true,
                "MAGEtab accession number eg. M-EXP-3678.");
        magetabAccession.setArgName("String");
        magetabAccession.setRequired(true);
        options.addOption(magetabAccession);

        //min string length to parse
        Option minStringLength = new Option(
                "m",
                "minStringLength",
                true,
                "The minimum length of a string in order to execute a Zooma search.");
        minStringLength.setArgName("String");
        minStringLength.setRequired(false);
        options.addOption(minStringLength);

        //cutoff percent
        Option cutoffPercentOption = new Option(
                "c",
                "cutoffPercentage",
                true,
                "Cutoff - a percentage value that will filter out all hits with a score less than this, " +
                        "when compared to the best scoring hit.");
        cutoffPercentOption.setArgName("float");
        cutoffPercentOption.setRequired(false);
        options.addOption(cutoffPercentOption);

        //cutoff score
        Option cutoffScoreOption = new Option(
                "s",
                "cutoffScore",
                true,
                "cutoffScore - a Zooma score that will filter out all hits with a score less than this.");
        cutoffScoreOption.setArgName("float");
        cutoffScoreOption.setRequired(false);
        options.addOption(cutoffScoreOption);

        //use ols short IDs instead of URIs
        Option olsShortIDs = new Option(
                "o",
                "olsShortIds",
                true,
                "true or false: use OLS short IDs instead of full URIs.");
        cutoffScoreOption.setArgName("string");
        cutoffScoreOption.setRequired(false);
        options.addOption(olsShortIDs);

        // overwrite term source values
        Option overwriteValues = new Option(
                "w",
                "overwriteValues",
                true,
                "true or false: overwrite term source values.");
        overwriteValues.setArgName("string");
        overwriteValues.setRequired(false);
        options.addOption(overwriteValues);

        // overwrite term source ref and term source accession
        Option overwriteAnnotations = new Option(
                "x",
                "overwriteAnnotations",
                true,
                "true or false: overwrite term source ref and term source accession.");
        overwriteAnnotations.setArgName("string");
        overwriteAnnotations.setRequired(false);
        options.addOption(overwriteAnnotations);

        // overwrite term source ref and term source accession
        Option addCommentsToSDRF = new Option(
                "l",
                "addCommentsToSDRF",
                true,
                "true or false: add comments to SDRF file.");
        addCommentsToSDRF.setArgName("string");
        addCommentsToSDRF.setRequired(false);
        options.addOption(addCommentsToSDRF);

        return options;
    }

    protected static Logger getLog() {
        return log;
    }

//    -----------------------------------------------------

    private float cutoffScore;
    private float cutoffPercentage;
    private int minStringLength;
    private HashSet excludedProperties;
    private boolean olsShortIds;
    private boolean overwriteValues;
    private boolean overwriteAnnotations;
    private boolean addCommentsToSDRF;
    private String magetabAccession;


    //Alternative constructor for instantiating programmatically instead of through the command line
    public ZoomageSearchDriver(float cutoffScore, float cutoffPercentage, int minStringLength, boolean olsShortIds, boolean addCommentsToSDRF,boolean overwriteAnnotations,HashSet excludedProperties) {
        this.cutoffScore = cutoffScore;
        this.cutoffPercentage = cutoffPercentage;
        this.minStringLength = minStringLength;
        this.excludedProperties = excludedProperties;
        this.olsShortIds = olsShortIds;
        this.overwriteAnnotations = overwriteAnnotations;
        this.addCommentsToSDRF = addCommentsToSDRF;
        System.out.println("Zoomage Driver created, ready to execute search.");
    }

    public void executeSearch() {
        try {

            ZoomaRESTClient zoomaClient = new ZoomaRESTClient(minStringLength, cutoffPercentage, cutoffScore, olsShortIds, overwriteAnnotations, excludedProperties);
            ZoomageMagetabParser zoomageParser = new ZoomageMagetabParser();
            zoomageParser.runFromFilesystem(magetabAccession, zoomaClient, overwriteValues, overwriteAnnotations, addCommentsToSDRF);

            System.out.println("Zoomage completed successfully.");

        } catch (Exception e) {
            System.out.println("Zoomage did not complete successfully: " + e.getMessage());
            System.exit(1);
        }
    }





}
