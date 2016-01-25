package uk.ac.ebi.fgpt.zooma.access;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import uk.ac.ebi.fgpt.zooma.exception.SearchException;
import uk.ac.ebi.fgpt.zooma.exception.SearchResourcesUnavailableException;
import uk.ac.ebi.fgpt.zooma.exception.SearchTimeoutException;
import uk.ac.ebi.fgpt.zooma.model.Annotation;
import uk.ac.ebi.fgpt.zooma.model.AnnotationPrediction;
import uk.ac.ebi.fgpt.zooma.model.AnnotationPredictionTemplate;
import uk.ac.ebi.fgpt.zooma.model.AnnotationSummary;
import uk.ac.ebi.fgpt.zooma.model.Property;
import uk.ac.ebi.fgpt.zooma.util.AnnotationPredictionBuilder;
import uk.ac.ebi.fgpt.zooma.util.ZoomaUtils;

import javax.annotation.PreDestroy;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Entry point for the ZOOMA application with the most commonly used functionality incorporated.  You can use this class
 * to search properties, select annotation summaries given a property or property type/value pair, and predict new
 * annotations.
 *
 * @author Tony Burdett
 * @date 14/08/15
 */
@Controller
@RequestMapping("/services")
public class Zooma extends SourceFilteredEndpoint {
    private ZoomaProperties zoomaProperties;
    private ZoomaAnnotations zoomaAnnotations;
    private ZoomaAnnotationSummaries zoomaAnnotationSummaries;

    private final float cutoffScore;
    private final float cutoffPercentage;

    // max time zooma will allow queries to run for - includes Lucene query, retrieval and queueing time
    private final int searchTimeout;

    private final ExecutorService executorService;

    @Autowired
    public Zooma(ZoomaProperties zoomaProperties,
                 ZoomaAnnotations zoomaAnnotations,
                 ZoomaAnnotationSummaries zoomaAnnotationSummaries,
                 @Qualifier("configurationProperties") Properties configuration) {
        this.zoomaProperties = zoomaProperties;
        this.zoomaAnnotations = zoomaAnnotations;
        this.zoomaAnnotationSummaries = zoomaAnnotationSummaries;
        this.cutoffScore = Float.parseFloat(configuration.getProperty("zooma.search.significance.score"));
        this.cutoffPercentage = Float.parseFloat(configuration.getProperty("zooma.search.cutoff.score"));
        this.searchTimeout = Integer.parseInt(configuration.getProperty("zooma.search.threads.timeout"));


        int concurrency = Integer.parseInt(configuration.getProperty("zooma.search.concurrent.threads"));
        int queueSize = Integer.parseInt(configuration.getProperty("zooma.search.max.queue"));
        final AtomicInteger atomicInteger = new AtomicInteger(1);
        this.executorService =
                new ThreadPoolExecutor(
                        concurrency,
                        concurrency,
                        0L,
                        TimeUnit.MILLISECONDS,
                        new ArrayBlockingQueue<Runnable>(queueSize),
                        new ThreadFactory() {
                            @Override public Thread newThread(Runnable r) {
                                return new Thread(r, "request-processing-thread-" + atomicInteger.getAndIncrement());
                            }
                        },
                        new RejectedExecutionHandler() {
                            @Override
                            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                                getLog().warn("Request rejected - " +
                                                      "queued requests: " + executor.getQueue().size() + "; " +
                                                      "completed requests: " + executor.getCompletedTaskCount());
                            }
                        });
    }

    @RequestMapping(value = "/suggest", method = RequestMethod.GET)
    @ResponseBody List<?> suggestEndpoint(@RequestParam String prefix,
                                          @RequestParam(required = false, defaultValue = "") String filter,
                                          @RequestParam(required = false, defaultValue = "false") boolean properties) {
        if (properties) {
            SearchType searchType = validateFilterArguments(filter);
            URI[] requiredSources;
            switch (searchType) {
                case REQUIRED_ONLY:
                case REQUIRED_AND_PREFERRED:
                    requiredSources = parseRequiredSourcesFromFilter(filter);
                    return suggestWithTypeFromSources(prefix, requiredSources);
                case PREFERRED_ONLY:
                case UNRESTRICTED:
                default:
                    return suggestWithType(prefix);
            }
        }
        else {
            SearchType searchType = validateFilterArguments(filter);
            URI[] requiredSources;
            switch (searchType) {
                case REQUIRED_ONLY:
                case REQUIRED_AND_PREFERRED:
                    requiredSources = parseRequiredSourcesFromFilter(filter);
                    return suggestFromSources(prefix, requiredSources);
                case PREFERRED_ONLY:
                case UNRESTRICTED:
                default:
                    return suggest(prefix);
            }
        }
    }

    public List<String> suggest(String prefix) {
        return zoomaProperties.suggest(prefix);
    }

    public List<Property> suggestWithType(String prefix) {
        return zoomaProperties.query(prefix);
    }

    public List<String> suggestFromSources(String prefix, URI... requiredSources) {
        return zoomaProperties.suggest(prefix, requiredSources);
    }

    public List<Property> suggestWithTypeFromSources(String prefix, URI... requiredSources) {
        return zoomaProperties.query(prefix, requiredSources);
    }

    @RequestMapping(value = "/select", method = RequestMethod.GET)
    @ResponseBody List<AnnotationSummary> selectEndpoint(@RequestParam String propertyValue,
                                                         @RequestParam(required = false) String propertyType,
                                                         @RequestParam(required = false,
                                                                       defaultValue = "") String filter) {
        if (propertyType == null) {
            SearchType searchType = validateFilterArguments(filter);
            URI[] requiredSources;
            switch (searchType) {
                case REQUIRED_ONLY:
                case REQUIRED_AND_PREFERRED:
                    requiredSources = parseRequiredSourcesFromFilter(filter);
                    return selectFromSources(propertyValue, requiredSources);
                case PREFERRED_ONLY:
                case UNRESTRICTED:
                default:
                    return select(propertyValue);
            }
        }
        else {
            SearchType searchType = validateFilterArguments(filter);
            URI[] requiredSources;
            switch (searchType) {
                case REQUIRED_ONLY:
                case REQUIRED_AND_PREFERRED:
                    requiredSources = parseRequiredSourcesFromFilter(filter);
                    return selectFromSources(propertyValue, propertyType, requiredSources);
                case PREFERRED_ONLY:
                case UNRESTRICTED:
                default:
                    return select(propertyValue, propertyType);
            }
        }
    }

    public List<AnnotationSummary> select(String propertyValue) {
        return extractAnnotationSummaryList(zoomaAnnotationSummaries.queryAndScore(propertyValue));
    }

    public List<AnnotationSummary> select(String propertyValue, String propertyType) {
        return extractAnnotationSummaryList(zoomaAnnotationSummaries.queryAndScore(propertyValue, propertyType));
    }

    public List<AnnotationSummary> selectFromSources(String propertyValue, URI... requiredSources) {
        return extractAnnotationSummaryList(zoomaAnnotationSummaries.queryAndScore(propertyValue,
                                                                                   "",
                                                                                   Collections.<URI>emptyList(),
                                                                                   requiredSources));
    }

    public List<AnnotationSummary> selectFromSources(String propertyValue,
                                                     String propertyType,
                                                     URI... requiredSources) {
        return extractAnnotationSummaryList(zoomaAnnotationSummaries.queryAndScore(propertyValue,
                                                                                   propertyType,
                                                                                   Collections.<URI>emptyList(),
                                                                                   requiredSources));
    }

    @RequestMapping(value = "/annotate", method = RequestMethod.GET)
    @ResponseBody List<AnnotationPrediction> annotationEndpoint(@RequestParam String propertyValue,
                                                                @RequestParam(required = false) String propertyType,
                                                                @RequestParam(required = false,
                                                                              defaultValue = "") String filter) {
        if (propertyType == null) {
            SearchType searchType = validateFilterArguments(filter);
            URI[] requiredSources = new URI[0];
            List<URI> preferredSources = Collections.emptyList();
            switch (searchType) {
                case REQUIRED_ONLY:
                    requiredSources = parseRequiredSourcesFromFilter(filter);
                    break;
                case REQUIRED_AND_PREFERRED:
                    requiredSources = parseRequiredSourcesFromFilter(filter);
                case PREFERRED_ONLY:
                    preferredSources = parsePreferredSourcesFromFilter(filter);
                    break;
                case UNRESTRICTED:
                default:
                    return annotate(propertyValue);
            }
            return annotate(propertyValue, preferredSources, requiredSources);
        }
        else {
            SearchType searchType = validateFilterArguments(filter);
            URI[] requiredSources = new URI[0];
            List<URI> preferredSources = Collections.emptyList();
            switch (searchType) {
                case REQUIRED_ONLY:
                    requiredSources = parseRequiredSourcesFromFilter(filter);
                    break;
                case REQUIRED_AND_PREFERRED:
                    requiredSources = parseRequiredSourcesFromFilter(filter);
                case PREFERRED_ONLY:
                    preferredSources = parsePreferredSourcesFromFilter(filter);
                    break;
                case UNRESTRICTED:
                default:
                    return annotate(propertyValue, propertyType);
            }
            return annotate(propertyValue, propertyType, preferredSources, requiredSources);
        }
    }

    public List<AnnotationPrediction> annotate(final String propertyValue) {
        Future<List<AnnotationPrediction>> f = executorService.submit(
                new Callable<List<AnnotationPrediction>>() {
                    @Override
                    public List<AnnotationPrediction> call() throws Exception {
                        Map<AnnotationSummary, Float> summaries = zoomaAnnotationSummaries.queryAndScore(propertyValue);
                        return createPredictions(propertyValue, null, summaries);
                    }
                }
        );

        return waitForResults(f, propertyValue);
    }

    public List<AnnotationPrediction> annotate(final String propertyValue, final String propertyType) {
        Future<List<AnnotationPrediction>> f = executorService.submit(
                new Callable<List<AnnotationPrediction>>() {
                    @Override
                    public List<AnnotationPrediction> call() throws Exception {
                        Map<AnnotationSummary, Float> summaries = zoomaAnnotationSummaries.queryAndScore(propertyValue,
                                                                                                         propertyType);
                        return createPredictions(propertyValue, propertyType, summaries);
                    }
                }
        );

        return waitForResults(f, propertyValue);
    }

    public List<AnnotationPrediction> annotate(final String propertyValue,
                                               final List<URI> preferredSources,
                                               final URI... requiredSources) {
        Future<List<AnnotationPrediction>> f = executorService.submit(
                new Callable<List<AnnotationPrediction>>() {
                    @Override
                    public List<AnnotationPrediction> call() throws Exception {
                        Map<AnnotationSummary, Float> summaries = zoomaAnnotationSummaries.queryAndScore(propertyValue,
                                                                                                         "",
                                                                                                         preferredSources,
                                                                                                         requiredSources);
                        return createPredictions(propertyValue, null, summaries);
                    }
                }
        );
        return waitForResults(f, propertyValue);
    }

    public List<AnnotationPrediction> annotate(final String propertyValue,
                                               final String propertyType,
                                               final List<URI> preferredSources,
                                               final URI... requiredSources) {

        Future<List<AnnotationPrediction>> f = executorService.submit(
                new Callable<List<AnnotationPrediction>>() {
                    @Override
                    public List<AnnotationPrediction> call() throws Exception {
                        Map<AnnotationSummary, Float> summaries = zoomaAnnotationSummaries.queryAndScore(propertyValue,
                                                                                                         propertyType,
                                                                                                         preferredSources,
                                                                                                         requiredSources);

                        return createPredictions(propertyValue, propertyType, summaries);
                    }
                }
        );
        return waitForResults(f, propertyValue);
    }

    private List<AnnotationPrediction> waitForResults(Future<List<AnnotationPrediction>> f, String propertyValue) {
        try {
            return f.get(searchTimeout, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
            throw new SearchTimeoutException("Failed to complete a search for '" + propertyValue + "' " +
                                                     "(interrupted, probably by timeout)", e);
        }
        catch (ExecutionException e) {
            if (e.getCause() instanceof SearchException) {
                throw (SearchException) e.getCause();
            }
            else {
                throw new SearchException("Failed to complete a search for '" + propertyValue + "' " +
                                                  "(" + e.getMessage() + ")", e);
            }
        }
        catch (CancellationException e) {
            getLog().debug("Search for '" + propertyValue + "' was cancelled");
            throw new SearchTimeoutException("Search for '" + propertyValue + "' was cancelled after taking too long",
                                             e);
        }
        catch (TimeoutException e) {
            boolean isSuccessful = f.cancel(true);
            if (!isSuccessful) {
                getLog().warn("Failed cancelling thread for search with propertyValue = " + propertyValue);
            }
            throw new SearchTimeoutException("Search for '" + propertyValue + "' took too long", e);
        }
    }

    private List<String> extractPropertyValueStrings(Collection<Property> properties) {
        List<String> result = new ArrayList<>();
        for (Property p : properties) {
            if (!result.contains(p.getPropertyValue())) {
                result.add(p.getPropertyValue());
            }
        }
        return result;
    }

    private List<AnnotationSummary> extractAnnotationSummaryList(
            final Map<AnnotationSummary, Float> annotationSummaryFloatMap) {
        List<AnnotationSummary> result = new ArrayList<>();
        for (AnnotationSummary as : annotationSummaryFloatMap.keySet()) {
            if (!result.contains(as)) {
                result.add(as);
            }
        }
        result.sort(new Comparator<AnnotationSummary>() {
            @Override
            public int compare(AnnotationSummary as1, AnnotationSummary as2) {
                return annotationSummaryFloatMap.get(as1) < annotationSummaryFloatMap.get(as2) ? -1 : 1;
            }
        });
        return result;
    }

    private List<AnnotationPrediction> createPredictions(String propertyValue,
                                                         String propertyType,
                                                         Map<AnnotationSummary, Float> summaries) {
        List<AnnotationPrediction> predictions = new ArrayList<>();

        // now use client to test and filter them
        if (!summaries.isEmpty()) {
            // get well scored annotation summaries
            List<AnnotationSummary> goodSummaries = ZoomaUtils.filterAnnotationSummaries(summaries,
                                                                                         cutoffPercentage);

            // for each good summary, extract an example annotation
            boolean achievedScore = false;
            List<Annotation> goodAnnotations = new ArrayList<>();

            for (AnnotationSummary goodSummary : goodSummaries) {

                if (!achievedScore && summaries.get(goodSummary) > cutoffScore) {
                    achievedScore = true;
                }

                if (!goodSummary.getAnnotationURIs().isEmpty()) {
                    URI annotationURI = goodSummary.getAnnotationURIs().iterator().next();
                    Annotation goodAnnotation = zoomaAnnotations.getAnnotationService().getAnnotation(annotationURI);
                    if (goodAnnotation != null) {
                        goodAnnotations.add(goodAnnotation);
                    }
                    else {
                        throw new SearchException(
                                "An annotation summary referenced an annotation that " +
                                        "could not be found - ZOOMA's indexes may be out of date");
                    }
                }
                else {
                    String message = "An annotation summary with no associated annotations was found - " +
                            "this is probably an error in inferring a new summary from lexical matches";
                    getLog().warn(message);
                    throw new SearchException(message);
                }
            }

            // now we have a list of good annotations; use this list to create predicted annotations
            AnnotationPrediction.Confidence confidence;
            if (goodAnnotations.size() == 1 && achievedScore) {
                // one good annotation, so create prediction with high confidence
                confidence = AnnotationPrediction.Confidence.HIGH;
            }
            else {
                if (achievedScore) {
                    // multiple annotations each with a good score, create predictions with good confidence
                    confidence = AnnotationPrediction.Confidence.GOOD;
                }
                else {
                    if (goodAnnotations.size() == 1) {
                        // single stand out annotation that didn't achieve score, create prediction with good confidence
                        confidence = AnnotationPrediction.Confidence.GOOD;
                    }
                    else {
                        // multiple annotations, none reached score, so create prediction with medium confidence
                        confidence = AnnotationPrediction.Confidence.MEDIUM;
                    }
                }
            }

            // ... code to create new annotation predictions goes here
            for (Annotation annotation : goodAnnotations) {
                AnnotationPredictionTemplate pt = AnnotationPredictionBuilder.predictFromAnnotation(annotation);
                if (propertyType == null) {
                    pt.searchWas(propertyValue);
                }
                else {
                    pt.searchWas(propertyValue, propertyType);
                }
                pt.confidenceIs(confidence);
                predictions.add(pt.build());
            }
        }
        return predictions;
    }

    @ExceptionHandler(SearchTimeoutException.class)
    @ResponseStatus(HttpStatus.REQUEST_TIMEOUT)
    @ResponseBody String handleSearchTimeoutException(SearchTimeoutException e) {
        getLog().error(
                "A search timeout exception occurred [" + e.getClass().getSimpleName() + ": " + e.getMessage() + "]");
        getLog().debug("Search timeout exception from annotation endpoint", e);
        return "This search could not be completed: " + e.getMessage();
    }

    @ExceptionHandler(SearchResourcesUnavailableException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    @ResponseBody String handleSearchResourcesUnavailableException(SearchResourcesUnavailableException e) {
        getLog().error(
                "Unavailable resources (index or database) [" + e.getClass().getSimpleName() + ": " + e.getMessage() +
                        "]");
        getLog().debug("Search resources unavailable from annotation endpoint", e);
        return "Your search request could not be completed due to a problem accessing resources on the server: (" +
                e.getMessage() + ")";
    }

    @ExceptionHandler(RejectedExecutionException.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    @ResponseBody String handleRejectedExecutionException(RejectedExecutionException e) {
        getLog().debug("Rejected execution exception from annotation endpoint", e);
        return "Too many requests - ZOOMA is experiencing abnormally high traffic, please try again later";
    }

    @ResponseStatus(HttpStatus.REQUEST_TIMEOUT)
    @ExceptionHandler(IllegalStateException.class)
    public @ResponseBody String handleStateException(IllegalStateException e) {
        getLog().error("Possible session time out? [" + e.getClass().getSimpleName() + ": " + e.getMessage() + "]");
        getLog().debug("Illegal state exception from annotation endpoint", e);
        return "Session in conflict - your session may have timed out whilst results were being generated " +
                "(" + e.getMessage() + ")";
    }

    @ExceptionHandler(SearchException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody String handleSearchException(SearchException e) {
        getLog().error("Unexpected search exception [" + e.getClass().getSimpleName() + ": " + e.getMessage() + "]");
        getLog().debug("Search exception from annotation endpoint", e);
        return "ZOOMA encountered a problem that it could not recover from (" + e.getMessage() + ")";
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public @ResponseBody String handleException(Exception e) {
        getLog().error("Unexpected exception [" + e.getClass().getSimpleName() + ": " + e.getMessage() + "]");
        getLog().debug("Exception from annotation endpoint", e);
        return "The server encountered a problem it could not recover from " +
                "(" + e.getMessage() + ")";
    }

    @PreDestroy public void destroy() throws Exception {
        List<Runnable> runnables = executorService.shutdownNow();
        if (runnables.size() > 0) {
            getLog().warn("Zooma shutdown attempted with " + runnables.size() + " search jobs still executing");
        }
    }
}