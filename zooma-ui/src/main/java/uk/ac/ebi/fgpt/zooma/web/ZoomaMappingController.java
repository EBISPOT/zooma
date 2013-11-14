package uk.ac.ebi.fgpt.zooma.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import uk.ac.ebi.fgpt.zooma.io.ZOOMAReportRenderer;
import uk.ac.ebi.fgpt.zooma.model.Annotation;
import uk.ac.ebi.fgpt.zooma.model.AnnotationSummary;
import uk.ac.ebi.fgpt.zooma.model.Property;
import uk.ac.ebi.fgpt.zooma.model.SimpleTypedProperty;
import uk.ac.ebi.fgpt.zooma.model.SimpleUntypedProperty;
import uk.ac.ebi.fgpt.zooma.model.TypedProperty;
import uk.ac.ebi.fgpt.zooma.search.ZOOMASearchTimer;
import uk.ac.ebi.fgpt.zooma.service.AnnotationService;
import uk.ac.ebi.fgpt.zooma.service.AnnotationSummarySearchService;
import uk.ac.ebi.fgpt.zooma.service.OntologyService;
import uk.ac.ebi.fgpt.zooma.util.OntologyLabelMapper;
import uk.ac.ebi.fgpt.zooma.util.ZoomaUtils;

import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
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
 * A controller stereotype that provides a REST-API endpoint to run a ZOOMA search over a series of properties.
 * <p/>
 * This class is essentially an alternative to {@link uk.ac.ebi.fgpt.zooma.search.ZOOMASearchClient}, optimized to run
 * searches in a web-application context instead of standalone application.
 *
 * @author Tony Burdett
 * @date 02/04/13
 */
@Controller
@RequestMapping("/services/map")
public class ZoomaMappingController {
    private Properties zoomaProperties;

    private AnnotationSummarySearchService annotationSummarySearchService;
    private AnnotationService annotationService;
    private OntologyService ontologyService;

    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public Properties getZoomaProperties() {
        return zoomaProperties;
    }

    @Autowired
    @Qualifier("zoomaProperties")
    public void setZoomaProperties(Properties zoomaProperties) {
        this.zoomaProperties = zoomaProperties;
    }

    public AnnotationSummarySearchService getAnnotationSummarySearchService() {
        return annotationSummarySearchService;
    }

    @Autowired
    @Qualifier("annotationSummarySearchService")
    public void setAnnotationSummarySearchService(AnnotationSummarySearchService annotationSummarySearchService) {
        this.annotationSummarySearchService = annotationSummarySearchService;
    }

    public AnnotationService getAnnotationService() {
        return annotationService;
    }

    @Autowired
    public void setAnnotationService(AnnotationService annotationService) {
        this.annotationService = annotationService;
    }

    public OntologyService getOntologyService() {
        return ontologyService;
    }

    @Autowired
    public void setOntologyService(OntologyService ontologyService) {
        this.ontologyService = ontologyService;
    }

    @RequestMapping(method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody String requestMapping(@RequestBody ZoomaMappingRequest request, HttpSession session)
            throws IOException {
        List<Property> properties = parseMappingRequest(request);
        session.setAttribute("properties", properties);
        session.setAttribute("progress", 0);
        int concurrency = Integer.parseInt(getZoomaProperties().getProperty("zooma.concurrency"));
        float cutoffScore = Float.parseFloat(getZoomaProperties().getProperty("zooma.cutoff.score"));
        float cutoffPercentage = Float.parseFloat(getZoomaProperties().getProperty("zooma.cutoff.percentage"));
        searchZOOMA(properties, session, concurrency, cutoffScore, cutoffPercentage);
        return "Mapping request of " + properties.size() + " properties was successfully received";
    }

    @RequestMapping(value = "/sample", method = RequestMethod.GET)
    public @ResponseBody List<Property> getSampleData() {
        List<Property> properties = new ArrayList<>();
        properties.add(new SimpleTypedProperty("compound", "broccoli"));
        properties.add(new SimpleTypedProperty("disease state", "lung adenocarcinoma"));
        properties.add(new SimpleTypedProperty("cell type", "CD4-positive"));
        return properties;
    }

    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public @ResponseBody String test(HttpSession session) throws IOException {
        List<Property> properties = getSampleData();
        session.setAttribute("properties", properties);
        session.setAttribute("progress", 0f);
        int concurrency = Integer.parseInt(getZoomaProperties().getProperty("zooma.concurrency"));
        float cutoffScore = Float.parseFloat(getZoomaProperties().getProperty("zooma.cutoff.score"));
        float cutoffPercentage = Float.parseFloat(getZoomaProperties().getProperty("zooma.cutoff.percentage"));
        searchZOOMA(properties, session, concurrency, cutoffScore, cutoffPercentage);
        return "Doing ZOOMA search";
    }

    @RequestMapping(value = "/status", method = RequestMethod.GET)
    public @ResponseBody float checkMappingStatus(HttpSession session) {
        float progress;
        if (session.getAttribute("progress") != null) {
            //noinspection RedundantCast
            float done = (float) ((Integer) session.getAttribute("progress"));
            float total = (float) ((List) session.getAttribute("properties")).size();
            if (done > 0) {
                progress = done / total;
            }
            else {
                progress = 0;
            }
        }
        else {
            progress = 0;
        }
        getLog().debug("Progress = " + progress);
        return progress;
    }

    @RequestMapping(method = RequestMethod.GET, produces = "text/plain")
    public @ResponseBody String getMappingReport(HttpSession session) {
        getLog().debug("Getting result");
        Object result = session.getAttribute("result");
        if (checkMappingStatus(session) == 1f) {
            while (result == null) {
                getLog().debug("Pending result, report rendering?");
                synchronized (this) {
                    try {
                        this.wait(500);
                    }
                    catch (InterruptedException e) {
                        getLog().error("Interrupted", e);
                    }
                }
                result = session.getAttribute("result");
            }
        }
        if (result != null && !result.toString().isEmpty()) {
            getLog().debug("Result: " + result);
            return result.toString();
        }
        else {
            getLog().debug("Result is empty");
            return "";
        }
    }

    @RequestMapping(method = RequestMethod.GET, produces = "application/json", params = "json")
    public @ResponseBody Map<String, Object> getMappingResult(HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        Exception exception = (Exception) session.getAttribute("exception");
        if (exception == null) {
            response.put("status", "OK");
        }
        else {
            response.put("status", exception.getMessage());
        }

        String report = getMappingReport(session);
        response.put("data", parseMappingReport(report));
        return response;
    }

    @RequestMapping(value = "/reset", method = RequestMethod.GET)
    public @ResponseBody String resetSession(HttpSession session) {
        try {
            session.invalidate();
            return "Your mapping session was cleared successfully";
        }
        catch (Exception e) {
            return "Your mapping session could not be reset";
        }
    }

    @ResponseStatus(HttpStatus.REQUEST_TIMEOUT)
    @ExceptionHandler(IllegalStateException.class)
    public @ResponseBody String handleStateException(IllegalStateException e) {
        return "Session in conflict - your session may have timed out whilst results were being generated " +
                "(" + e.getMessage() + ")";
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public @ResponseBody String handleException(Exception e) {
        return "The server encountered a problem it could not recover from " +
                "(" + e.getMessage() + ")";
    }

    private List<Property> parseMappingRequest(ZoomaMappingRequest request) {
        List<Property> result = new ArrayList<>();
        for (ZoomaMappingRequestItem item : request) {
            if (item.getPropertyType() != null) {
                result.add(new SimpleTypedProperty(item.getPropertyType(), item.getPropertyValue()));
            }
            else {
                result.add(new SimpleUntypedProperty(item.getPropertyValue()));
            }
        }
        return result;
    }

    private List<String[]> parseMappingReport(String report) {
        // create results
        List<String[]> results = new ArrayList<>();

        // read and parse mapping report
        BufferedReader reader = new BufferedReader(new StringReader(report));
        String line;
        try {
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                // skip first 6 lines, they're header
                if (lineNumber > 7) {
                    String[] values = line.split("\\t", -1);
                    results.add(values);
                }
                lineNumber++;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return results;
    }

    private void searchZOOMA(final List<Property> properties,
                             final HttpSession session,
                             final int concurrency,
                             final float cutoffScore,
                             final float cutoffPercentage) {
        getLog().info("Searching ZOOMA for mappings.  Parameters: " +
                              "{[concurrency:" + concurrency + "]," +
                              "[cutoffScore:" + cutoffScore + "]," +
                              "[cutoffPercentage:" + cutoffPercentage + "]}");

        // create a timer to time search tasks
        final ZOOMASearchTimer timer = new ZOOMASearchTimer(properties.size()).start();

        final Map<Property, Set<Annotation>> annotations =
                Collections.synchronizedMap(new HashMap<Property, Set<Annotation>>());

        final Map<Property, Boolean> searchAchievedScore =
                Collections.synchronizedMap(new HashMap<Property, Boolean>());

        // start searching - use 'concurrent' parallel threads
        final Deque<Future<Integer>> jobQueue = new ConcurrentLinkedDeque<>();
        final ExecutorService service = Executors.newFixedThreadPool(concurrency);
        for (final Property property : properties) {
            // simple unit of work to perform the zooma search and update annotations with results
            jobQueue.add(service.submit(new Callable<Integer>() {
                @Override public Integer call() throws Exception {
                    // first, grab annotation summaries
                    Map<AnnotationSummary, Float> summaries;
                    if (property instanceof TypedProperty) {
                        summaries = getAnnotationSummarySearchService().searchAndScore(
                                ((TypedProperty) property).getPropertyType(), property.getPropertyValue());
                    }
                    else {
                        summaries = getAnnotationSummarySearchService().searchAndScore(property.getPropertyValue());
                    }

                    // now use client to test and filter them
                    if (!summaries.isEmpty()) {
                        // get well scored annotation summaries
                        Set<AnnotationSummary> goodSummaries = ZoomaUtils.filterAnnotationSummaries(summaries,
                                                                                                    cutoffPercentage);

                        // for each good summary, extract an example annotation
                        boolean achievedScore = false;
                        Set<Annotation> goodAnnotations = new HashSet<>();

                        for (AnnotationSummary goodSummary : goodSummaries) {
                            if (!achievedScore && summaries.get(goodSummary) > cutoffScore) {
                                achievedScore = true;
                            }

                            if (!goodSummary.getAnnotationURIs().isEmpty()) {
                                URI annotationURI = goodSummary.getAnnotationURIs().iterator().next();
                                goodAnnotations.add(getAnnotationService().getAnnotation(annotationURI));
                            }
                            else {
                                String message = "An annotation summary with no associated annotations was found - " +
                                        "this is probably an error in inferring a new summary from lexical matches";
                                getLog().warn(message);
                                throw new RuntimeException(message);
                            }

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
                            annotations.put(property, goodAnnotations);
                        }
                        synchronized (searchAchievedScore) {
                            searchAchievedScore.put(property, achievedScore);
                        }
                    }

                    // update timing stats
                    timer.completedNext();
                    session.setAttribute("progress", timer.getCompletedCount());
                    return timer.getCompletedCount();
                }
            }));
        }

        // create a thread to run until all ZOOMA searches have finished, then update session
        new Thread(new Runnable() {
            @Override public void run() {
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
                        timer.completedNext(); // update here so progress doesn't stall
                        getLog().error("Job " + f + " was interrupted whilst waiting for completion - " +
                                               "there are " + failedCount + " fails now");
                    }
                    catch (ExecutionException e) {
                        failedCount++;
                        timer.completedNext(); // update here so progress doesn't stall
                        getLog().error(
                                "A job failed to execute - there are " + failedCount + " fails now.  " +
                                        "Error was:\n", e.getCause());
                    }
                    catch (Exception e) {
                        failedCount++;
                        timer.completedNext(); // update here so progress doesn't stall
                        getLog().error(
                                "A job failed with an unknown exception - there are " + failedCount + " fails now.  " +
                                        "Error was:\n", e.getCause());
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
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    ZOOMAReportRenderer renderer =
                            new ZOOMAReportRenderer(new LabelMapper(getOntologyService()),
                                                    out,
                                                    out); // unmapped elements go in same report
                    Map<Property, List<String>> propertyContexts = new HashMap<>();
                    renderer.renderAnnotations(properties, propertyContexts, annotations, searchAchievedScore);
                    try {
                        renderer.close();
                        getLog().debug("ZOOMA search complete, results will be stored in a session attribute");
                        session.setAttribute("progress", timer.getCompletedCount());
                        session.setAttribute("result", out.toString());

                        if (failedCount > 0) {
                            session.setAttribute("exception", new RuntimeException(
                                    "There were " + failedCount + " ZOOMA searches that encountered problems"));
                        }
                    }
                    catch (IOException e) {
                        session.setAttribute("exception", e);
                    }
                    getLog().info(
                            "Successfully generated ZOOMA report for " + timer.getCompletedCount() + " searches," +
                                    " HTTP session '" + session.getId() + "'");
                }
            }
        }).start();
    }

    private class LabelMapper implements OntologyLabelMapper {
        private OntologyService ontologyService;

        private LabelMapper(OntologyService ontologyService) {
            this.ontologyService = ontologyService;
        }

        @Override public String getLabel(URI uri) {
            return ontologyService.getLabel(uri);
        }

        @Override public URI getURI(String label) {
            throw new UnsupportedOperationException("This mapper does not support URI lookup from labels");
        }
    }
}
