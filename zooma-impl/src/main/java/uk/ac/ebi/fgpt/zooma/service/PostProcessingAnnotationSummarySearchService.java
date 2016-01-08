package uk.ac.ebi.fgpt.zooma.service;

import uk.ac.ebi.fgpt.zooma.model.AnnotationSummary;
import uk.ac.ebi.fgpt.zooma.model.SimpleAnnotationSummary;
import uk.ac.ebi.fgpt.zooma.util.AnnotationSummarySearchCommand;
import uk.ac.ebi.fgpt.zooma.util.SearchStringProcessor;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * An {@link AnnotationSummarySearchServiceDecorator} that extends the functionality of an {@link
 * AnnotationSummarySearchService} to support post-processing of the search string if zero results are obtained with a
 * normal search.
 * <p>
 * This class performs the original search, then, if no results are acquired, makes use of the supplied {@link
 * uk.ac.ebi.fgpt.zooma.util.SearchStringProcessor} to expand the search.  It then retries a series of searches with the
 * expanded set of search strings and combines the results.
 * <p>
 * This service returns aggregated {@link AnnotationSummary} objects that represent an inferred mapping between the
 * searched property and the list of semantic tags obtained from one or more ZOOMA searches.
 * <p>
 * Because expanding results in this way gives us a (possibly very) large set of summaries that require merging, this
 * class currently only supports merging if the processed string returns at most two distinct parts.
 *
 * @author Jose Iglesias
 * @date 12/8/13
 */
public class PostProcessingAnnotationSummarySearchService extends AnnotationSummarySearchServiceDecorator {
    private static final float partialStringBoost = 0.7f;
    private static final int limitTo = 20;

    private SearchStringProcessor searchStringProcessor;

    public SearchStringProcessor getSearchStringProcessor() {
        return searchStringProcessor;
    }

    public void setSearchStringProcessor(SearchStringProcessor searchStringProcessor) {
        this.searchStringProcessor = searchStringProcessor;
    }

    public PostProcessingAnnotationSummarySearchService(AnnotationSummarySearchService annotationSummarySearchService) {
        super(annotationSummarySearchService);
    }

    @Override
    public Collection<AnnotationSummary> search(String propertyValuePattern, final URI... sources) {
        return doProcessedSearch(propertyValuePattern, new AnnotationSummarySearchCommand() {
            @Override public Collection<AnnotationSummary> executeSearch(String propertyValue) {
                return PostProcessingAnnotationSummarySearchService.super.search(propertyValue, sources);
            }
        });
    }

    @Override
    public Collection<AnnotationSummary> search(final String propertyType,
                                                String propertyValuePattern,
                                                final URI... sources) {
        return doProcessedSearch(propertyValuePattern, new AnnotationSummarySearchCommand() {
            @Override public Collection<AnnotationSummary> executeSearch(String propertyValue) {
                return PostProcessingAnnotationSummarySearchService.super.search(propertyType, propertyValue, sources);
            }
        });
    }

    @Override public Collection<AnnotationSummary> searchByPreferredSources(String propertyValuePattern,
                                                                            final List<URI> preferredSources,
                                                                            final URI... requiredSources) {
        return doProcessedSearch(propertyValuePattern, new AnnotationSummarySearchCommand() {
            @Override public Collection<AnnotationSummary> executeSearch(String propertyValue) {
                return PostProcessingAnnotationSummarySearchService.super.searchByPreferredSources(propertyValue,
                                                                                                   preferredSources,
                                                                                                   requiredSources);
            }
        });
    }

    @Override public Collection<AnnotationSummary> searchByPreferredSources(final String propertyType,
                                                                            String propertyValuePattern,
                                                                            final List<URI> preferredSources,
                                                                            final URI... requiredSources) {
        return doProcessedSearch(propertyValuePattern, new AnnotationSummarySearchCommand() {
            @Override public Collection<AnnotationSummary> executeSearch(String propertyValue) {
                return PostProcessingAnnotationSummarySearchService.super.searchByPreferredSources(propertyType,
                                                                                                   propertyValue,
                                                                                                   preferredSources,
                                                                                                   requiredSources);
            }
        });
    }

    /**
     * Uses the supplied annotation summary search command to execute the "original" search, followed by a search
     * against the results of processing the supplied property value pattern using this classes search string processor
     * if no results were obtained from the original search.
     * <p>
     * Note that this implementation can only handle the case where the original string can be processed into two or
     * fewer parts; this limit is set to prevent a combinatorial explosion of possible results when merging individual
     * responses
     *
     * @param propertyValuePattern the property value pattern to search for
     * @param command              a command that encapsulates the search to execute
     * @return a collection of annotation summaries that satisfy the query
     */
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
                getLog().debug("Search for '" + propertyValuePattern + "' failed to return results, " +
                                       "using " + getSearchStringProcessor().getClass().getSimpleName() + " " +
                                       "to expand results");

                Collection<String> parts = getSearchStringProcessor().processSearchString(propertyValuePattern);

                // for now, we only work with cases where the string returns at most 2 processed forms
                if (parts.size() < 3) {
                    if (parts.size() == 0) {
                        return rawResults;
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
        }
        // if we get to here, either we got raw results or we couldn't process the string into parts, so just return
        return rawResults;
    }

    protected Collection<AnnotationSummary> mergeResults(String propertyValuePattern,
                                                         String firstPart,
                                                         Collection<AnnotationSummary> firstPartResults,
                                                         String secondPart,
                                                         Collection<AnnotationSummary> secondPartResults) {
        return mergeResults(propertyValuePattern, limitTo, firstPart, firstPartResults, secondPart, secondPartResults);
    }

    protected Collection<AnnotationSummary> mergeResults(String propertyValuePattern,
                                                         int limitTo,
                                                         String firstPart,
                                                         Collection<AnnotationSummary> firstPartResults,
                                                         String secondPart,
                                                         Collection<AnnotationSummary> secondPartResults) {
        Collection<AnnotationSummary> results = new HashSet<>();

        int firstPartLength = firstPart.length();
        int secondPartLength = secondPart.length();

        List<AnnotationSummary> firstPartResultsList = new ArrayList<>();
        Iterator<AnnotationSummary> firstPartResultsIt = firstPartResults.iterator();
        for (int i = 0; i < limitTo; i++) {
            if (!firstPartResultsIt.hasNext()) {
                break;
            }
            else {
                firstPartResultsList.add(firstPartResultsIt.next());
            }
        }
        List<AnnotationSummary> secondPartResultsList = new ArrayList<>();
        Iterator<AnnotationSummary> secondPartResultsIt = secondPartResults.iterator();
        for (int i = 0; i < limitTo; i++) {
            if (!secondPartResultsIt.hasNext()) {
                break;
            }
            else {
                secondPartResultsList.add(secondPartResultsIt.next());
            }
        }

        for (AnnotationSummary firstPartSummary : firstPartResultsList) {
            float firstPartScore = firstPartSummary.getQuality();
            String firstPartType = firstPartSummary.getAnnotatedPropertyType();

            for (AnnotationSummary secondPartSummary : secondPartResultsList) {
                float secondPartScore = secondPartSummary.getQuality();
                String secondPartType = secondPartSummary.getAnnotatedPropertyType();

                float scoreFinal =
                        ((firstPartLength * firstPartScore + secondPartLength * secondPartScore) /
                                (firstPartLength + secondPartLength)) * partialStringBoost;

                HashSet<URI> aggregatedTags = new HashSet<>();
                HashSet<URI> aggregatedURIs = new HashSet<>();
                HashSet<URI> aggregatedSourceURIs = new HashSet<>();

                Collection<URI> firstPartSemanticTags = firstPartSummary.getSemanticTags();
                Collection<URI> secondPartSemanticTags = secondPartSummary.getSemanticTags();
                Collection<URI> firstPartAnnotationURIs = firstPartSummary.getAnnotationURIs();
                Collection<URI> secondPartAnnotationURIs = secondPartSummary.getAnnotationURIs();
                Collection<URI> firstPartAnnotationSourceRIs = firstPartSummary.getAnnotationSourceURIs();
                Collection<URI> secondPartAnnotationSourceURIs = secondPartSummary.getAnnotationSourceURIs();
                if (firstPartSemanticTags != null && !firstPartSemanticTags.isEmpty()) {
                    aggregatedTags.addAll(firstPartSemanticTags);
                }

                if (secondPartSemanticTags != null && !secondPartSemanticTags.isEmpty()) {
                    aggregatedTags.addAll(secondPartSemanticTags);
                }

                if (firstPartAnnotationURIs != null && !firstPartAnnotationURIs.isEmpty()) {
                    aggregatedURIs.addAll(firstPartAnnotationURIs);
                }

                if (secondPartAnnotationURIs != null && !secondPartAnnotationURIs.isEmpty()) {
                    aggregatedURIs.addAll(secondPartAnnotationURIs);
                }

                if (firstPartAnnotationSourceRIs != null && !firstPartAnnotationSourceRIs.isEmpty()) {
                    aggregatedSourceURIs.addAll(firstPartAnnotationSourceRIs);
                }

                if (secondPartAnnotationSourceURIs != null && !secondPartAnnotationSourceURIs.isEmpty()) {
                    aggregatedSourceURIs.addAll(secondPartAnnotationSourceURIs);
                }

                String type = null;
                if (firstPartType.contentEquals(secondPartType)) {
                    type = firstPartType;
                }

                AnnotationSummary newAnnotationSummary = new SimpleAnnotationSummary(null,
                                                                                     null,
                                                                                     type,
                                                                                     propertyValuePattern,
                                                                                     aggregatedTags,
                                                                                     aggregatedURIs,
                                                                                     scoreFinal,
                                                                                     aggregatedSourceURIs);

                results.add(newAnnotationSummary);
            }
        }
        return results;
    }
}