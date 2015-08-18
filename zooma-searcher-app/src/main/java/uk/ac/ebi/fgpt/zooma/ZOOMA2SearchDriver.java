package uk.ac.ebi.fgpt.zooma;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.zooma.env.ZoomaEnv;
import uk.ac.ebi.fgpt.zooma.env.ZoomaHome;
import uk.ac.ebi.fgpt.zooma.io.ZOOMAInputParser;
import uk.ac.ebi.fgpt.zooma.io.ZOOMAReportRenderer;
import uk.ac.ebi.fgpt.zooma.model.Annotation;
import uk.ac.ebi.fgpt.zooma.model.AnnotationPrediction;
import uk.ac.ebi.fgpt.zooma.model.AnnotationSummary;
import uk.ac.ebi.fgpt.zooma.model.Property;
import uk.ac.ebi.fgpt.zooma.search.ZOOMASearchClient;
import uk.ac.ebi.fgpt.zooma.search.ZOOMASearchTimer;
import uk.ac.ebi.fgpt.zooma.util.AnnotationPredictionBuilder;
import uk.ac.ebi.fgpt.zooma.util.OntologyLabelMapper;
import uk.ac.ebi.fgpt.zooma.util.ZoomaUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * A command line client for ZOOMA 2 that takes a list of properties (values, constrained by an optional type) and
 * produces a list of ontology mappings for those properties.
 *
 * @author Tony Burdett
 * @date 08/08/12
 */
public class ZOOMA2SearchDriver {
    private static boolean _evaluationMode;
    private static File _inputFile;
    private static OutputStream _out;
    private static OutputStream _err;
    private static URL _zoomaLocation;
    private static float _score;
    private static float _cutoffPercentage;
    private static int _concurrency;

    public static void main(String[] args) {
        try {
            ZOOMA2SearchDriver driver = new ZOOMA2SearchDriver();
            int statusCode = parseArguments(args);
            if (statusCode == 0) {
                if (_evaluationMode) {
                    driver.evaluateOptimalTextAnnotations(_zoomaLocation,
                                                          _score,
                                                          _cutoffPercentage,
                                                          _concurrency,
                                                          _inputFile,
                                                          _out,
                                                          _err);
                }
                else {
                    driver.findOptimalTextAnnotations(_zoomaLocation,
                                                      _score,
                                                      _cutoffPercentage,
                                                      _concurrency,
                                                      _inputFile,
                                                      _out,
                                                      _err);
                }
                System.out.println("ZOOMA completed successfully.");

                if (_out != System.out) {
                    _out.close();
                }
            }
            else {
                System.exit(statusCode);
            }
        }
        catch (IOException e) {
            System.err.println("A read/write problem occurred: " + e.getMessage());
            System.exit(1);
        }
        catch (Exception e) {
            System.err.println("ZOOMA did not complete successfully: " + e.getMessage());
            System.exit(1);
        }
    }

    private static int parseArguments(String[] args) throws IOException {
        CommandLineParser parser = new GnuParser();
        HelpFormatter help = new HelpFormatter();
        Options options = bindOptions();

        ZoomaEnv.configureZOOMAEnvironment();

        int parseArgs = 0;
        try {
            Properties defaults = new Properties();
            File configDir = new File(System.getProperty("zooma.home"), "config");
            File defaultsPath = new File(configDir, "zooma.properties");
            InputStream in = new FileInputStream(defaultsPath);
            if (in == null) {
                throw new IOException("Failed to read ZOOMA default options from " + defaultsPath.getAbsolutePath());
            }
            defaults.load(in);

            CommandLine cl = parser.parse(options, args, true);

            // check for mode help option
            if (cl.hasOption("")) {
                // print out mode help
                help.printHelp("zooma", options, true);
                parseArgs += 1;
            }
            else {
                System.out.println("Your ZOOMA search will be run with the following supplied options...");
                for (Option opt : cl.getOptions()) {
                    System.out.println("\t" + opt.getLongOpt() +
                                               (opt.hasArg() ? ": " + opt.getValue() : "") +
                                               " (" + opt.getArgName() + ")");
                }

                // required options
                if (cl.hasOption("i")) {
                    _inputFile = new File(cl.getOptionValue("i"));
                }
                if (cl.hasOption("o")) {
                    String outOpt = cl.getOptionValue("o");
                    File outFile = new File(outOpt);
                    File errFile = new File(outFile.getAbsoluteFile().getParentFile(), "zooma_unmapped_report.txt");
                    _out = new BufferedOutputStream(new FileOutputStream(outFile));
                    _err = new BufferedOutputStream(new FileOutputStream(errFile));
                }

                // optional arguments, if not supplied use defaults
                if (cl.hasOption("n")) {
                    String concurrencyOption = cl.getOptionValue("n");
                    _concurrency = Integer.parseInt(concurrencyOption);
                }
                else {
                    _concurrency = Integer.parseInt(defaults.getProperty("zooma.search.concurrent.threads"));
                    System.out.println("Using default ZOOMA concurrency, " + _concurrency);
                }
                if (cl.hasOption("z")) {
                    _zoomaLocation = URI.create(cl.getOptionValue("z")).toURL();
                }
                else {
                    _zoomaLocation = URI.create(defaults.getProperty("zooma.search.location")).toURL();
                    System.out.println("Using default ZOOMA location, '" + _zoomaLocation.toString() + "'");
                }

                // evaluation mode overrides score and cutoff to produce verbose report
                if (cl.hasOption("e")) {
                    _evaluationMode = true;
                    _score = 0;
                    _cutoffPercentage = 0;
                    System.out.println("ZOOMA running in evaluation mode: significance score set to " + _score + ", " +
                                               "cutoff percentage set to " + _cutoffPercentage);
                }
                else {

                    if (cl.hasOption("s")) {
                        String scoreOpt = cl.getOptionValue("s");
                        _score = Float.parseFloat(scoreOpt);
                    }
                    else {
                        _score = Float.parseFloat(defaults.getProperty("zooma.search.significance.score"));
                        System.out.println("Using default ZOOMA significance score, " + _score);
                    }
                    if (cl.hasOption("c")) {
                        String cutoffOpt = cl.getOptionValue("c");
                        _cutoffPercentage = Float.parseFloat(cutoffOpt);
                    }
                    else {
                        _cutoffPercentage = Float.parseFloat(defaults.getProperty("zooma.search.cutoff.score"));
                        System.out.println("Using default ZOOMA cutoff percentage, " + _cutoffPercentage);
                    }
                }
            }
        }
        catch (ParseException e) {
            System.err.println("Failed to read supplied arguments (" + e.getMessage() + ")");
            help.printHelp("zooma", options, true);
            parseArgs += 4;
        }
        catch (FileNotFoundException e) {
            System.err.println("Failed to read supplied arguments - file not found (" + e.getMessage() + ")");
            help.printHelp("zooma", options, true);
            parseArgs += 5;
        }
        catch (MalformedURLException e) {
            System.err.println("Failed to read supplied arguments - a supplied argument was not a valid URL " +
                                       "(" + e.getMessage() + ")");
            help.printHelp("zooma", options, true);
            parseArgs += 6;
        }
        return parseArgs;
    }

    private static Options bindOptions() {
        Options options = new Options();

        // help
        Option helpOption = new Option("h", "help", false, "Print the help");
        options.addOption(helpOption);

        // add input options
        OptionGroup inputGroup = new OptionGroup();
        inputGroup.setRequired(true);

        Option inputFileOption = new Option(
                "i",
                "input",
                true,
                "Input - file where ZOOMA can find properties that should be annotated");
        inputFileOption.setArgName("file");
        inputFileOption.setRequired(false);
        inputGroup.addOption(inputFileOption);

        Option evaluationOption = new Option(
                "e",
                "evaluation",
                false,
                "Evaluation mode - take properties from the input file and use ZOOMA to show " +
                        "how well all possible annotations score");
        evaluationOption.setRequired(false);
        inputGroup.addOption(evaluationOption);
        options.addOptionGroup(inputGroup);

        // add output file arguments
        Option outputOption = new Option(
                "o",
                "output",
                true,
                "Output - file where ZOOMA should write the annotation report");
        outputOption.setArgName("file");
        outputOption.setRequired(true);
        options.addOption(outputOption);

        Option scoreOption = new Option(
                "s",
                "score",
                true,
                "Score - the score that should be achieved to provide a ZOOMA result.  " +
                        "If there is more than one hit with this score, all possible annotations are shown.");
        scoreOption.setArgName("float");
        scoreOption.setRequired(false);
        options.addOption(scoreOption);

        Option cutoffOption = new Option(
                "c",
                "cutoff",
                true,
                "Cutoff - a percentage value that will filter out all hits with a score less than this, " +
                        "when compared to the best scoring hit.");
        cutoffOption.setArgName("float");
        cutoffOption.setRequired(false);
        options.addOption(cutoffOption);

        Option concurrencyOption = new Option(
                "n",
                "concurrency",
                true,
                "Concurrency - the number of queries to dispatch to ZOOMA in parallel.");
        concurrencyOption.setArgName("integer");
        concurrencyOption.setRequired(false);
        options.addOption(concurrencyOption);

        Option zoomaOption = new Option(
                "z",
                "zooma",
                true,
                "ZOOMA location - the URL of the ZOOMA service to address queries to");
        zoomaOption.setArgName("URL");
        zoomaOption.setRequired(false);
        options.addOption(zoomaOption);

        return options;
    }

    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public ZOOMA2SearchDriver() {
        ZoomaEnv.configureZOOMAEnvironment();
        ZoomaHome.checkInstall();
    }

    public void findOptimalTextAnnotations(URL zoomaLocation,
                                           float score,
                                           float cutoffPercentage,
                                           int concurrency,
                                           File inputFile,
                                           OutputStream out,
                                           OutputStream err) throws IOException {
        try {
            getLog().info("Reading properties from input file '" + inputFile.getAbsolutePath() + "'");

            // create input parser and parse supplied properties
            System.out.print("Reading properties from input file '" + inputFile.getAbsolutePath() + "'...");
            ZOOMAInputParser parser = new ZOOMAInputParser(inputFile);
            List<Property> properties = parser.parse();
            parser.close();
            System.out.println("done.");

            // exclude any ineligible properties
            excludeIneligibleProperties(properties);

            // and search
            searchZOOMA(zoomaLocation, score, cutoffPercentage, concurrency, properties, out, err);
        }
        catch (RuntimeException e) {
            getLog().error("Caught unexpected runtime exception", e);
            throw e;
        }
    }

    public void evaluateOptimalTextAnnotations(URL zoomaLocation,
                                               float score,
                                               float cutoffPercentage,
                                               int concurrency,
                                               File inputFile,
                                               OutputStream out,
                                               OutputStream err) throws IOException {
        getLog().info("Reading properties from input file '" + inputFile.getAbsolutePath() + "'");

        // create input parser and parse supplied properties
        System.out.print("Reading properties from input file '" + inputFile.getAbsolutePath() + "'...");
        ZOOMAInputParser parser = new ZOOMAInputParser(inputFile);
        List<Property> properties = parser.parse();
        parser.close();
        System.out.println("done.");

        // exclude any ineligible properties
        excludeIneligibleProperties(properties);

        // and evaluate
        evaluateZOOMA(zoomaLocation, score, cutoffPercentage, concurrency, properties, out, err);
    }

    private void excludeIneligibleProperties(List<Property> properties) {
        excludeIneligibleProperties(properties, new HashMap<Property, List<String>>());
    }

    private void excludeIneligibleProperties(List<Property> properties, Map<Property, List<String>> propertyContexts) {
        // firstly, remove any excluded types
        ZOOMA2PropertyExcluder excluder = new ZOOMA2PropertyExcluder();
        excluder.removeExcludedTypes(properties);

        // now, check for strings of a bad length and numeric values
        Iterator<Property> propertyIterator = properties.iterator();
        while (propertyIterator.hasNext()) {
            Property property = propertyIterator.next();
            String propertyValue = property.getPropertyValue();

            // if property value is 3 characters or less, or 500 characters or more in length, exclude
            if (propertyValue.length() <= 3 || propertyValue.length() >= 500) {
                getLog().debug("Property value '" + propertyValue + "' has a length " +
                                       "(" + propertyValue.length() + " characters) " +
                                       "that makes it ineligible for ZOOMA search");
                propertyIterator.remove();
                if (propertyContexts.containsKey(property)) {
                    propertyContexts.remove(property);
                }
            }
            else {
                // if property value is numeric, exclude
                if (isNumeric(property.getPropertyValue())) {
                    getLog().debug("Property value '" + propertyValue + "' is a numeric value. " +
                                           "This makes it ineligible for ZOOMA search");
                    propertyIterator.remove();
                    if (propertyContexts.containsKey(property)) {
                        propertyContexts.remove(property);
                    }
                }
            }
        }
    }

    private boolean isNumeric(String inputData) {
        ParsePosition pos = new ParsePosition(0);
        NumberFormat.getInstance().parse(inputData, pos);
        return inputData.length() == pos.getIndex();
    }

    private void searchZOOMA(URL zoomaLocation,
                             float score,
                             float cutoffPercentage,
                             int concurrency,
                             List<Property> properties,
                             OutputStream out,
                             OutputStream err) throws IOException {
        searchZOOMA(zoomaLocation,
                    score,
                    cutoffPercentage,
                    concurrency,
                    properties,
                    new HashMap<Property, List<String>>(),
                    out,
                    err);
    }

    private void searchZOOMA(final URL zoomaLocation,
                             final float score,
                             final float cutoffPercentage,
                             final int concurrency,
                             final List<Property> properties,
                             final Map<Property, List<String>> propertyContexts,
                             final OutputStream out,
                             final OutputStream err) throws IOException {
        getLog().info("Searching ZOOMA [" + zoomaLocation + "] for mappings");
        System.out.println("Starting ZOOMA search using ZOOMA at: " + zoomaLocation);

        // create a timer to time search tasks
        final ZOOMASearchTimer timer = new ZOOMASearchTimer(properties.size()).start();
        final ZOOMASearchClient searcher = new ZOOMASearchClient(zoomaLocation);
        final Map<Property, List<AnnotationPrediction>> annotations =
                Collections.synchronizedMap(new HashMap<Property, List<AnnotationPrediction>>());
        final Map<Property, Boolean> searchAchievedScore =
                Collections.synchronizedMap(new HashMap<Property, Boolean>());

        // start searching - use 'concurrent' parallel threads
        Deque<Future<Integer>> jobQueue = new ConcurrentLinkedDeque<>();
        ExecutorService service = Executors.newFixedThreadPool(concurrency);
        for (final Property property : properties) {
            // simple unit of work to perform the zooma search and update annotations with results
            jobQueue.add(service.submit(new Callable<Integer>() {
                @Override public Integer call() throws Exception {
                    // first, grab annotation summaries
                    Map<AnnotationSummary, Float> summaries = searcher.searchZOOMA(property, score);
                    if (!summaries.isEmpty()) {
                        // get well scored annotation summaries
                        Set<AnnotationSummary> goodSummaries = ZoomaUtils.filterAnnotationSummaries(summaries,
                                                                                                    cutoffPercentage);

                        // for each good summary, extract an example annotation
                        boolean achievedScore = false;
                        Set<Annotation> goodAnnotations = new HashSet<>();
                        for (AnnotationSummary goodSummary : goodSummaries) {
                            if (!achievedScore && summaries.get(goodSummary) > score) {
                                achievedScore = true;
                            }
                            URI annotationURI = goodSummary.getAnnotationURIs().iterator().next();
                            goodAnnotations.add(searcher.getAnnotation(annotationURI));
                            // trace log each annotation summary that has generated content to be written to the report
                            if (getLog().isTraceEnabled()) {
                                getLog().trace(
                                        "Next annotation result obtained:\n\t\t" +
                                                "Searched: " + property + "\t" +
                                                "Found: " + goodSummary.getAnnotatedPropertyValue() + " " +
                                                "[" + goodSummary.getAnnotatedPropertyType() + "] " +
                                                "-> " + goodSummary.getSemanticTags() + "\t" +
                                                "Score: " + summaries.get(goodSummary));
                            }
                        }

                        // and add good annotations to the annotations map
                        synchronized (annotations) {
                            // todo - need to rewrite search client to use new API for predictions!
                            List<AnnotationPrediction> predictions = new ArrayList<>();
                            for (Annotation goodAnnotation : goodAnnotations) {
                                predictions.add(AnnotationPredictionBuilder.buildPrediction(goodAnnotation).build());
                            }
                            annotations.put(property, predictions);
                        }
                        synchronized (searchAchievedScore) {
                            searchAchievedScore.put(property, achievedScore);
                        }
                    }

                    // update timing stats
                    timer.completedNext();
                    float estimatedTime = ((float) timer.getCurrentEstimate()) / 1000;
                    boolean showInMins = false;
                    if (estimatedTime > 60) {
                        showInMins = true;
                        estimatedTime = estimatedTime / 60;
                    }
                    String estimate = new DecimalFormat("#,###").format(estimatedTime);
                    System.out.print("Checked " + timer.getCompletedCount() + "/" +
                                             timer.getTotalCount() + " property values.  " +
                                             "Estimated time remaining : " +
                                             estimate + (showInMins ? " mins." : " s.") + "     \r");
                    return timer.getCompletedCount();
                }
            }));
        }

        // pop elements from the jobQueue, make sure they're done, then discard
        Future<Integer> f = jobQueue.poll();
        int failedCount = 0;
        while (f != null) {
            try {
                int total = f.get();
                getLog().trace("There are " + total + " searches are now complete");
            }
            catch (InterruptedException e) {
                failedCount++;
                getLog().error("Job " + f + " was interrupted whilst waiting for completion - " +
                                       "there are " + failedCount + " fails now");
            }
            catch (ExecutionException e) {
                failedCount++;
                getLog().error("A job failed to execute - there are " + failedCount + " fails now.  Error was:\n",
                               e.getCause());
            }
            f = jobQueue.poll();
        }

        getLog().debug("Shutting down executor service...");
        service.shutdown();
        try {
            service.awaitTermination(2, TimeUnit.MINUTES);
            getLog().debug("Executor service shutdown gracefully.");
        }
        catch (InterruptedException e) {
            getLog().error("Executor service failed to shutdown cleanly", e);
            throw new RuntimeException("Unable to cleanly shutdown ZOOMA.", e);
        }
        finally {
            getLog().info("Search complete. Writing out results...");
            System.out.print("\n\nSearch complete.  Writing results...");
            ZOOMAReportRenderer renderer =
                    new ZOOMAReportRenderer(new ZOOMALabelMapper(searcher),
                                            out,
                                            err);
            renderer.renderAnnotations(properties, propertyContexts, annotations);
            renderer.close();
            System.out.println("done.");
            getLog().info("ZOOMA report complete");

            if (failedCount > 0) {
                //noinspection ThrowFromFinallyBlock
                throw new RuntimeException("There were " + failedCount + " ZOOMA searches that encountered problems");
            }
        }
    }

    private void evaluateZOOMA(final URL zoomaLocation,
                               final float score,
                               final float cutoffPercentage,
                               final int concurrency,
                               final List<Property> properties,
                               final OutputStream out,
                               final OutputStream err) throws IOException {
        getLog().info("Evaluating ZOOMA mappings [" + zoomaLocation + "]");
        System.out.println("Starting ZOOMA search using ZOOMA at: " + zoomaLocation);

        // create a timer to time search tasks
        final ZOOMASearchTimer timer = new ZOOMASearchTimer(properties.size()).start();
        final ZOOMASearchClient searcher = new ZOOMASearchClient(zoomaLocation);
        final Map<Property, Map<AnnotationSummary, Float>> annotationSummaries = new HashMap<>();

        // start searching - use 'concurrent' parallel threads
        Deque<Future<Integer>> jobQueue = new ConcurrentLinkedDeque<>();
        ExecutorService service = Executors.newFixedThreadPool(concurrency);
        for (final Property property : properties) {
            // simple unit of work to perform the zooma search and update annotations with results
            jobQueue.add(service.submit(new Callable<Integer>() {
                @Override public Integer call() throws Exception {
                    // just grab annotation summaries
                    Map<AnnotationSummary, Float> summaries = searcher.searchZOOMA(property, score);
                    if (!summaries.isEmpty()) {
                        annotationSummaries.put(property, summaries);
                    }

                    // update timing stats
                    timer.completedNext();
                    String estimate = new DecimalFormat("#,###").format(((float) timer.getCurrentEstimate()) / 1000);
                    System.out.print("Checked " + timer.getCompletedCount() + "/" +
                                             timer.getTotalCount() + " property values.  " +
                                             "Estimated time remaining : " + estimate + " s.     \r");
                    return timer.getCompletedCount();
                }
            }));
        }

        // pop elements from the jobQueue, make sure they're done, then discard
        Future<Integer> f = jobQueue.poll();
        int failedCount = 0;
        while (f != null) {
            try {
                int total = f.get();
                getLog().trace("There are " + total + " searches are now complete");
            }
            catch (InterruptedException e) {
                failedCount++;
                getLog().error("Job " + f + " was interrupted whilst waiting for completion - " +
                                       "there are " + failedCount + " fails now");
            }
            catch (ExecutionException e) {
                failedCount++;
                getLog().error("A job failed to execute - there are " + failedCount + " fails now.  Error was:\n",
                               e.getCause());
            }
            f = jobQueue.poll();
        }

        getLog().debug("Shutting down executor service...");
        service.shutdown();
        try {
            service.awaitTermination(2, TimeUnit.MINUTES);
            getLog().debug("Executor service shutdown gracefully.");
        }
        catch (InterruptedException e) {
            getLog().error("Executor service failed to shutdown cleanly", e);
            throw new RuntimeException("Unable to cleanly shutdown ZOOMA.", e);
        }
        finally {
            getLog().info("Search complete. Writing out results...");
            System.out.print("\n\nSearch complete.  Writing results...");
            ZOOMAReportRenderer renderer = new ZOOMAReportRenderer(new ZOOMALabelMapper(searcher), out, err);
            renderer.renderAnnotationSummaries(properties, annotationSummaries);
            renderer.close();
            System.out.println("done.");
            getLog().info("ZOOMA report complete");

            if (failedCount > 0) {
                //noinspection ThrowFromFinallyBlock
                throw new RuntimeException("There were " + failedCount + " ZOOMA searches that encountered problems");
            }
        }
    }

    private class ZOOMALabelMapper implements OntologyLabelMapper {
        private ZOOMASearchClient searchClient;

        private ZOOMALabelMapper(ZOOMASearchClient searchClient) {
            this.searchClient = searchClient;
        }

        @Override public String getLabel(URI uri) {
            try {
                return searchClient.getLabel(uri);
            }
            catch (IOException e) {
                getLog().error("Failed to lookup label for " + uri);
                return "N/A";
            }
        }

        @Override public Collection<String> getSynonyms(URI uri) {
            try {
                return searchClient.getSynonyms(uri);
            }
            catch (IOException e) {
                getLog().error("Failed to lookup synonyms for " + uri);
                return Collections.singleton("N/A");
            }
        }

        @Override public URI getURI(String label) {
            throw new UnsupportedOperationException("This mapper does not support URI lookup from labels");
        }
    }
}
