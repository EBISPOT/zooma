package uk.ac.ebi.fgpt.zooma.search;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.springframework.context.support.ClassPathXmlApplicationContext;
//import uk.ac.ebi.fgpt.zooma.atlas.ZOOMAPropertySampler;
//import uk.ac.ebi.fgpt.zooma.atlas.ZoomaAtlasDAO;
//import uk.ac.ebi.fgpt.zooma.io.ZOOMAInputParser;
//import uk.ac.ebi.fgpt.zooma.io.ZOOMAReportRenderer;
//import uk.ac.ebi.fgpt.zooma.util.OntologyLabelMapper;

import java.io.*;
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
    private static String _compoundAnnotationDelimiter;
    private static String _logFileDelimiter;

    //    private static final String _excludedTypesResource = "zoomage-propertytype-exclusions.properties";
    private static final String exclusionProfilesResource = "zoomage-exclusions.csv";
    private static final String mageTabAccessionsResource = "zoomage-accessions.txt";
    private static boolean _overwriteValues;
    private static boolean _overwriteAnnotations;

    private static final Logger log = LoggerFactory.getLogger(ZoomageSearchDriver.class);
    private static boolean _addCommentsToSDRF;
    private static String _magetabBasePath;
    private static String _zoomaPath;
    private static String _limpopoPath;


    public static void main(String[] args) {
        try {
            int statusCode = parseArguments(args);
            if (statusCode == 0) {

                ZoomageMagetabParser zoomageParser = new ZoomageMagetabParser(_zoomaPath, _limpopoPath, _magetabBasePath, _minStringLength, _cutoffPercentage, _cutoffScore, _olsShortIds, _compoundAnnotationDelimiter, _logFileDelimiter, _overwriteValues, _overwriteAnnotations, _addCommentsToSDRF);
                zoomageParser.setExclusionProfiles(parseExclusionProfiles(exclusionProfilesResource));

                if (_magetabAccession == null || _magetabAccession.equals("")) {
                    HashSet<String> mageTabAccessions = parseMagetabAccessions(mageTabAccessionsResource);

                    for (String accession : mageTabAccessions) {
                        System.out.println("------------------------------------");
                        log.info("Processing " + accession + "...");

                        boolean success = zoomageParser.runFromFilesystem(accession);
                        if (success) {
                            printLog(zoomageParser, accession);
                            getLog().info("Zoomage completed successfully for " + accession);
                        } else {
                            getLog().info("Zoomage encountered errors for " + accession);
                        }
                    }

                } else {
                    zoomageParser.runFromFilesystem(_magetabAccession);
                    printLog(zoomageParser, _magetabAccession);

                    getLog().info("Zoomage completed successfully for " + _magetabAccession);
                }

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

        // what delimiter to use between annotations of a single compound annotation (eg. for heart and lung)
        if (cl.hasOption("d")) {
            _compoundAnnotationDelimiter = cl.getOptionValue("d");
            if (cl.getOptionValue("d").length() > 1) getLog().warn("Delimiter is more than a single character.");

        } else {
            _compoundAnnotationDelimiter = defaults.getProperty("zoomage.compoundAnnotationDelimiter");
            System.out.println("Using default ZOOMA property for delimiting compound annotations, " + _compoundAnnotationDelimiter);

            if (defaults.getProperty("zoomage.compoundAnnotationDelimiter").length() > 1)
                getLog().warn("Delimiter is more than a single character.");
        }

        // what base path to use for magetab files.
        if (cl.hasOption("p")) {
            _magetabBasePath = cl.getOptionValue("p");
        } else {
            _magetabBasePath = defaults.getProperty("zoomage.magetabBasePath");
            System.out.println("Using default ZOOMA property for the base path of magetab files to parse, " + _magetabBasePath);
        }

        // what base path to use for magetab files.
        if (cl.hasOption("z")) {
            _zoomaPath = cl.getOptionValue("z");
        } else {
            _zoomaPath = defaults.getProperty("zoomage.zoomaPath");
            System.out.println("Using default ZOOMA property for the zooma path, " + _zoomaPath);
        }

        // what base path to use for magetab files.
        if (cl.hasOption("i")) {
            _limpopoPath = cl.getOptionValue("i");
        } else {
            _limpopoPath = defaults.getProperty("zoomage.limpopoPath");
            System.out.println("Using default ZOOMA property for the limpopo path, " + _limpopoPath);
        }


        // what delimiter to use between annotations of a single compound annotation (eg. for heart and lung)
        // todo: check for tab    @tony?
        if (cl.hasOption("f")) {
            _logFileDelimiter = cl.getOptionValue("f");
            if (cl.getOptionValue("f").length() > 1)
                getLog().warn("Delimiter must be a single character; only the first character will be used.");
        } else {
            _logFileDelimiter = defaults.getProperty("zoomage.logFileDelimiter");
            System.out.println("Using default ZOOMA property for delimiting log file, " + _logFileDelimiter);
            if (defaults.getProperty("zoomage.logFileDelimiter").length() > 1)
                getLog().warn("Delimiter is more than one character.");
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
        magetabAccession.setRequired(false);
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

        // delimiter for compound annotations
        Option compoundAnnotationDelimiter = new Option(
                "d",
                "delimiterForCompoundAnnotations",
                true,
                "The character used to delimit compound annotations.");
        compoundAnnotationDelimiter.setArgName("char");
        compoundAnnotationDelimiter.setRequired(false);
        options.addOption(compoundAnnotationDelimiter);

        // delimiter for logFile
        Option logFileDelimiter = new Option(
                "f",
                "delimiterForLogFile",
                true,
                "The character used to delimit the log file.");
        logFileDelimiter.setArgName("char");
        logFileDelimiter.setRequired(false);
        options.addOption(logFileDelimiter);

        // base file path for magetab files
        Option mageTabBasePath = new Option(
                "p",
                "magetabBasePath",
                true,
                "The base path for magetab files to parse.");
        logFileDelimiter.setArgName("string");
        logFileDelimiter.setRequired(true);
        options.addOption(mageTabBasePath);

        // URL indicating which version of zooma to use
        Option zoomaPath = new Option(
                "z",
                "zoomaPath",
                true,
                "The zooma path eg: http://www.ebi.ac.uk/fgpt/zooma");
        logFileDelimiter.setArgName("string");
        logFileDelimiter.setRequired(true);
        options.addOption(zoomaPath);

        // URL indicating which version of limpopo to use
        Option limpopoPath = new Option(
                "i",
                "limpopoPath",
                true,
                "The limpopo path eg: http://wwwdev.ebi.ac.uk/fgpt/limpopo");
        logFileDelimiter.setArgName("string");
        logFileDelimiter.setRequired(true);
        options.addOption(limpopoPath);

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
    private HashSet exclusionProfiles;
    private boolean olsShortIds;
    private boolean overwriteValues;
    private boolean overwriteAnnotations;
    private boolean addCommentsToSDRF;
    private String magetabAccession;
    private String magetabBasePath;


    //Alternative constructor for instantiating programmatically instead of through the command line
    public ZoomageSearchDriver(float cutoffScore, float cutoffPercentage, int minStringLength, boolean olsShortIds, boolean addCommentsToSDRF, boolean overwriteAnnotations, boolean overwriteValues, String magetabBasePath, HashSet excludedProperties, HashSet exclusionProfiles) {
        this.cutoffScore = cutoffScore;
        this.cutoffPercentage = cutoffPercentage;
        this.minStringLength = minStringLength;
        this.excludedProperties = excludedProperties;
        this.exclusionProfiles = exclusionProfiles;
        this.olsShortIds = olsShortIds;
        this.overwriteAnnotations = overwriteAnnotations;
        this.overwriteValues = overwriteValues;
        this.addCommentsToSDRF = addCommentsToSDRF;
        this.magetabBasePath = magetabBasePath;
        System.out.println("Zoomage Driver created, ready to execute search.");
    }

//    public void executeSearch() {
//        try {
//
//            ZoomaRESTClient zoomaClient = new ZoomaRESTClient(minStringLength, cutoffPercentage, cutoffScore);
//            ZoomageMagetabParser zoomageParser = new ZoomageMagetabParser(zoomaClient, magetabAccession, olsShortIds, overwriteAnnotations, excludedProperties, exclusionProfiles);
//            zoomageParser.runFromFilesystem(overwriteValues, overwriteAnnotations, addCommentsToSDRF);
//
//            System.out.println("Zoomage completed successfully.");
//
//        } catch (Exception e) {
//            System.out.println("Zoomage did not complete successfully: " + e.getMessage());
//            System.exit(1);
//        }
//    }

    private static HashSet<String> parseExclusionProfiles(String exclusionProfilesResource) {
        HashSet<String> exclusionProfiles = new HashSet<String>();

        // read sources from file
        try {
            InputStream in = ZoomageSearchDriver.class.getClassLoader().getResourceAsStream(exclusionProfilesResource);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String exclusionLine;
            while ((exclusionLine = reader.readLine()) != null) {
                if (!exclusionLine.startsWith("#") && !exclusionLine.isEmpty()) {
                    int indexFirstDelim = exclusionLine.indexOf(_logFileDelimiter);
                    if (indexFirstDelim != 0) {
                        String type = exclusionLine.substring(0, indexFirstDelim);
                        String normalisedType = ZoomageTextUtils.normaliseType(type);

                        if (!type.equalsIgnoreCase(normalisedType)) {
                            //todo: check that this doesn't replace more than the type
                            exclusionLine = exclusionLine.replace(type + _logFileDelimiter, normalisedType + _logFileDelimiter);
                        }
                    }
                    exclusionProfiles.add(exclusionLine);
                }
            }
        } catch (FileNotFoundException e) {
            getLog().error("Failed to load properties: could not locate file '" + exclusionProfilesResource + "'.  " +
                    "No properties will be excluded");
        } catch (IOException e) {
            getLog().error("Failed to load properties: could not read file '" + exclusionProfilesResource + "'.  " +
                    "No properties will be excluded");
        }

        return exclusionProfiles;
    }

    private static HashSet<String> parseMagetabAccessions(String mageTabAccessionsResource) {
        HashSet<String> mageTabAccessions = new HashSet<String>();

        // read sources from file
        try {
            InputStream in = ZoomageSearchDriver.class.getClassLoader().getResourceAsStream(mageTabAccessionsResource);
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

    public static void printLog(ZoomageMagetabParser zoomageParser, String magetabAccession) throws IOException {

        ArrayList<String> zoomificationsApplied = zoomageParser.getCacheOfZoomificationsApplied();
        ArrayList<String> exclusionsApplied = zoomageParser.getCacheOfExclusionsApplied();
        ArrayList<String> itemsRequiringCuration = zoomageParser.getCacheOfItemsRequiringCuration();
        HashSet<String> legacyAnnotations = zoomageParser.getCacheOfLegacyAnnotations();
        ArrayList<String> noResults = zoomageParser.getCacheOfItemsWithNoResults();

        String d = _logFileDelimiter;
        String header = "ORIGINAL TYPE" + d + "ORIGINAL VALUE" + d + "ZOOMA VALUE" + d + "ONT LABEL" + d + "TERM SOURCE REF" + d + "TERM ACCESSION" + d + "MAGETAB ACCESSION";
        String blankLine = (d + d + d + d + d + d + d);

        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(magetabAccession + "-zoomifications-log.txt", false))); //todo: change file ending

        // print zoomifications log

        if (zoomificationsApplied.size() == 0) out.println("NO ZOOMIFICATIONS APPLIED" + d + d + d + d + d + d);
        else {
            if (_overwriteValues)
                out.println("ZOOMIFICATIONS APPLIED" + d + "original values overwritten" + d + d + d + d + d);
            else out.println("ZOOMIFICATIONS APPLIED" + d + "original values retained" + d + d + d + d + d);
            out.println(header);

            for (String eachline : zoomificationsApplied) {
                out.println(eachline);
            }
        }

        out.println(blankLine);


        // print legacy annotations, if applicable

        if (_overwriteAnnotations) {

            if (legacyAnnotations.size() == 0) out.println("NO ANNOTATIONS WERE OVERWRITTEN" + d + d + d + d + d + d);
            else {
                out.println("ORIGINAL ANNOTATIONS OVERWRITTEN" + d + d + d + d + d + d);

                out.println(header);

                for (String eachline : legacyAnnotations) {
                    out.println(eachline);
                }
            }
        } else {
            if (legacyAnnotations.size() == 0) out.println("NO LEGACY ANNOTATIONS WERE FOUND" + d + d + d + d + d + d);
            else {

                out.println("ORIGINAL ANNOTATIONS PRESERVED" + d + d + d + d + d + d);

                out.println(header + d + "NUMBER OF ZOOMA RESULTS");

                for (String eachline : legacyAnnotations) {
                    out.println(eachline);
                }
            }
        }

        out.println(blankLine);

        // print log of items with no results

        if (noResults.size() == 0)
            out.println("There was no input for which Zooma returned no results" + d + d + d + d + d + d);

        else {
            out.println("ITEMS WITH ZOOMA ERRORS or NO RESULTS" + d + d + d + d + d + d);
            out.println(header);

            for (String eachline : noResults) {
                out.println(eachline);
            }
        }

        out.println(blankLine);


        // print exclusions log

        if (exclusionsApplied.size() == 0) out.println("NO EXCLUSIONS APPLIED" + d + d + d + d + d + d);

        else {
            // print exclusions log
            out.println("EXCLUSIONS APPLIED" + d + "asterisk indicates reason" + d + d + d + d + d);
            out.println(header);

            for (String eachline : exclusionsApplied) {
                out.println(eachline);
            }
        }

        out.println(blankLine);

        // print curation log

        if (itemsRequiringCuration.size() == 0) out.println("NO ITEMS REQUIRE CURATION" + d + d + d + d + d + d);

        else {
            out.println("ITEMS REQUIRING CURATION" + d + d + d + d + d + d);
            out.println(header + d + "Number of Zooma Results");

            for (String eachline : itemsRequiringCuration) {
                out.println(eachline);
            }
        }

        out.println(blankLine);

        // print settings log
        out.println("SETTINGS USED" + d + d + d + d + d + d);
        out.println("Zooma base URL" + d + _zoomaPath);
        out.println("Limpopo base URL" + d + _limpopoPath);
        out.println("Date run" + d + new Date());
        out.println("Cutoff score" + d + _cutoffScore);
        out.println("Cutoff percentage" + d + _cutoffPercentage);
        out.println("Minimum string length" + d + _minStringLength);
        out.println("Use OLS Short ID format" + d + _olsShortIds);
        out.println("Overwrite values" + d + _overwriteValues);
        out.println("Overwrite annotations" + d + _overwriteAnnotations);
        out.println("Add comments to SDRF file" + d + _addCommentsToSDRF);

        out.println(blankLine);


        out.close();
    }
}
