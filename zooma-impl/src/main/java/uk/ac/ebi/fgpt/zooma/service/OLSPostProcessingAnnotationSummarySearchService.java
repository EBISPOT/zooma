package uk.ac.ebi.fgpt.zooma.service;

import uk.ac.ebi.fgpt.zooma.model.AnnotationSummary;
import uk.ac.ebi.fgpt.zooma.util.AnnotationSummarySearchCommand;
import uk.ac.ebi.fgpt.zooma.util.SearchStringProcessor;

import java.net.URI;
import java.util.*;

/**
 * Created by olgavrou on 19/05/2016.
 */
public class OLSPostProcessingAnnotationSummarySearchService extends AnnotationSummarySearchServiceDecorator {

    private static final float partialStringBoost = 0.7f;
    private static final int limitTo = 20;

    private SearchStringProcessor searchStringProcessor;

    public SearchStringProcessor getSearchStringProcessor() {
        return searchStringProcessor;
    }

    public void setSearchStringProcessor(SearchStringProcessor searchStringProcessor) {
        this.searchStringProcessor = searchStringProcessor;
    }

    public OLSPostProcessingAnnotationSummarySearchService(AnnotationSummarySearchService annotationSummarySearchService) {
        super(annotationSummarySearchService);
    }

    @Override
    public Collection<AnnotationSummary> search(String propertyValuePattern, final URI... sources) {
        return doProcessedSearch(propertyValuePattern, new AnnotationSummarySearchCommand() {
            @Override public Collection<AnnotationSummary> executeSearch(String propertyValue) {
                return OLSPostProcessingAnnotationSummarySearchService.super.search(propertyValue, sources);
            }
        });
    }

    @Override
    public Collection<AnnotationSummary> search(final String propertyType,
                                                String propertyValuePattern,
                                                final URI... sources) {
        return doProcessedSearch(propertyValuePattern, new AnnotationSummarySearchCommand() {
            @Override public Collection<AnnotationSummary> executeSearch(String propertyValue) {
                return OLSPostProcessingAnnotationSummarySearchService.super.search(propertyType, propertyValue, sources);
            }
        });
    }

    private Collection<AnnotationSummary> doProcessedSearch(String propertyValuePattern,
                                                            AnnotationSummarySearchCommand command) {
        Collection<AnnotationSummary> rawResults = command.executeSearch(propertyValuePattern);

        // if raw results are empty, attempt to process the string and requery
        if (rawResults.isEmpty()) {

            try {
                initOrWait();
            }
            catch (InterruptedException e) {
                throw new RuntimeException("Initialization failed, cannot query", e);
            }

            // check if we can process the string
            if (getSearchStringProcessor().canProcess(propertyValuePattern)) {
                return getProcessedSearch(getSearchStringProcessor(), propertyValuePattern, command);
            }

        }
        // if we get to here, either we got raw results or we couldn't process the string into parts, so just return
        return rawResults;
    }

    Collection<AnnotationSummary> getProcessedSearch(SearchStringProcessor searchStringProcessor, String propertyValuePattern, AnnotationSummarySearchCommand command){
        // check if we can process the string
        if (searchStringProcessor.canProcess(propertyValuePattern)) {
            getLog().debug("Search for '" + propertyValuePattern + "' failed to return results, " +
                    "using " + searchStringProcessor.getClass().getSimpleName() + " " +
                    "to expand results");

            try {
                Collection<String> parts = searchStringProcessor.processSearchString(propertyValuePattern);
                // for now, we only work with cases where the string returns at most 2 processed forms
                if (parts.size() < 3) {
                    if (parts.size() == 0) {
                        return new ArrayList<>();
                    }
                    else if (parts.size() == 1) {
                        return command.executeSearch(propertyValuePattern);
                    }
                    else {
                        Iterator<String> partsIterator = parts.iterator();
                        String firstPart = partsIterator.next();
                        String secondPart = partsIterator.next();
                        Collection<AnnotationSummary> firstPartResults = command.executeSearch(firstPart);
                        Collection<AnnotationSummary> secondPartResults = command.executeSearch(secondPart);
                        return mergeResults(propertyValuePattern,
                                firstPart,
                                firstPartResults,
                                secondPart,
                                secondPartResults);
                    }
                }
                else {
                    getLog().warn("Cannot currently support merging more than 2 processed results, " +
                            "due to limits in generating combined summaries.  " +
                            "Query '" + propertyValuePattern + "' may have lost results");
                }
            }
            catch (InterruptedException e) {
                getLog().warn("Expanding search results for '" + propertyValuePattern + "' using " +
                        searchStringProcessor.getClass().getSimpleName() + " took too long, " +
                        "expanded search results will not be available");
            }
        }
        return new ArrayList<>();
    }

    protected Collection<AnnotationSummary> mergeResults(String propertyValuePattern,
                                                         String firstPart,
                                                         Collection<AnnotationSummary> firstPartResults,
                                                         String secondPart,
                                                         Collection<AnnotationSummary> secondPartResults) {
        return mergeResults(firstPartResults, secondPartResults);
    }

    protected Collection<AnnotationSummary> mergeResults(Collection<AnnotationSummary> firstPartResults,
                                                         Collection<AnnotationSummary> secondPartResults) {
        Collection<AnnotationSummary> results = new HashSet<>();

        results.addAll(firstPartResults);
        results.addAll(secondPartResults);

        return results;

    }

}
