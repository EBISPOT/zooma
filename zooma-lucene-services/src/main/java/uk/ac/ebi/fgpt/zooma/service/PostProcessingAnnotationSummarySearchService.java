package uk.ac.ebi.fgpt.zooma.service;

import uk.ac.ebi.fgpt.zooma.model.AnnotationSummary;
import uk.ac.ebi.fgpt.zooma.model.SimpleAnnotationSummary;
import uk.ac.ebi.fgpt.zooma.util.SearchStringProcessor;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

/**
 * An {@link AnnotationSummarySearchServiceDecorator} that extends the functionality of an {@link
 * AnnotationSummarySearchService} to support post-processing of the search string if zero results are obtained with a
 * normal search.
 * <p/>
 * This class performs the original search, then, if no results are acquired, makes use of the supplied {@link
 * uk.ac.ebi.fgpt.zooma.util.SearchStringProcessor} to expand the search.  It then retries a series of searches with the
 * expanded set of search strings and combines the results.
 * <p/>
 * This service returns aggregated {@link AnnotationSummary} objects that represent an inferred mapping between the
 * searched property and the list of semantic tags obtained from one or more ZOOMA searches.
 * <p/>
 * Because expanding results in this way gives us a (possibly very) large set of summaries that require merging, this
 * class currently only supports merging if the processed string returns at most two distinct parts.
 *
 * @author Jose Iglesias
 * @date 12/8/13
 */
public class PostProcessingAnnotationSummarySearchService extends AnnotationSummarySearchServiceDecorator {
    private static final float partialStringBoost = 0.7f;

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
    public Map<AnnotationSummary, Float> searchAndScore(String propertyValuePattern) {
        Map<AnnotationSummary, Float> rawResults = super.searchAndScore(propertyValuePattern);

        // if raw results are empty, attempt to process the string and requery
        if (rawResults.isEmpty()) {
            try {
                initOrWait();
            }
            catch (InterruptedException e) {
                throw new RuntimeException("Initialization failed, cannot query", e);
            }

            // check if we can process the string
            // todo - only use processors that are suitable based on type
            if (getSearchStringProcessor().canProcess(propertyValuePattern)) {
                Collection<String> parts = getSearchStringProcessor().processSearchString(propertyValuePattern);

                // for now, we only work with cases where the string returns at most 2 processed forms
                if (parts.size() < 3) {
                    if (parts.size() == 0) {
                        return rawResults;
                    }
                    else if (parts.size() == 1) {
                        return super.searchAndScore(propertyValuePattern);
                    }
                    else {
                        Iterator<String> partsIterator = parts.iterator();
                        String firstPart = partsIterator.next();
                        String secondPart = partsIterator.next();
                        Map<AnnotationSummary, Float> firstPartResults =
                                super.searchAndScore(firstPart);
                        Map<AnnotationSummary, Float> secondPartResults =
                                super.searchAndScore(secondPart);

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

    @Override
    public Map<AnnotationSummary, Float> searchAndScore(String propertyType,
                                                        String propertyValuePattern) {
        Map<AnnotationSummary, Float> rawResults = super.searchAndScore(propertyType, propertyValuePattern);

        // if raw results are empty, attempt to process the string and requery
        if (rawResults.isEmpty()) {
            try {
                initOrWait();
            }
            catch (InterruptedException e) {
                throw new RuntimeException("Initialization failed, cannot query", e);
            }

            // check if we can process the string
            // todo - only use processors that are suitable based on type
            if (getSearchStringProcessor().canProcess(propertyValuePattern)) {
                Collection<String> parts = getSearchStringProcessor().processSearchString(propertyValuePattern);

                // for now, we only work with cases where the string returns at most 2 processed forms
                if (parts.size() < 3) {
                    if (parts.size() == 0) {
                        return rawResults;
                    }
                    else if (parts.size() == 1) {
                        return super.searchAndScore(propertyType, propertyValuePattern);
                    }
                    else {
                        Iterator<String> partsIterator = parts.iterator();
                        String firstPart = partsIterator.next();
                        String secondPart = partsIterator.next();
                        Map<AnnotationSummary, Float> firstPartResults =
                                super.searchAndScore(firstPart);
                        Map<AnnotationSummary, Float> secondPartResults =
                                super.searchAndScore(secondPart);

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
        return rawResults;
    }

    protected Map<AnnotationSummary, Float> mergeResults(String propertyValuePattern,
                                                         String firstPart,
                                                         Map<AnnotationSummary, Float> firstPartResults,
                                                         String secondPart,
                                                         Map<AnnotationSummary, Float> secondPartResults) {
        Map<AnnotationSummary, Float> results = new HashMap<>();

        int firstPartLength = firstPart.length();
        int secondPartLength = secondPart.length();

        for (AnnotationSummary firstPartSummary : firstPartResults.keySet()) {
            Float firstPartScore = firstPartResults.get(firstPartSummary);
            String firstPartType = firstPartSummary.getAnnotatedPropertyType();

            for (AnnotationSummary secondPartSummary : secondPartResults.keySet()) {
                Float secondPartScore = secondPartResults.get(secondPartSummary);
                String secondPartType = secondPartSummary.getAnnotatedPropertyType();

                float scoreFinal =
                        ((firstPartLength * firstPartScore + secondPartLength * secondPartScore) /
                                (firstPartLength + secondPartLength)) * partialStringBoost;
                float qualityScoreFinal = ((firstPartLength * firstPartSummary.getQualityScore() +
                        secondPartLength * secondPartSummary.getQualityScore()) /
                        (firstPartLength + secondPartLength)) * partialStringBoost;

                HashSet<URI> aggregatedTags = new HashSet<>();
                HashSet<URI> aggregatedURIs = new HashSet<>();

                Collection<URI> firstPartSemanticTags = firstPartSummary.getSemanticTags();
                Collection<URI> secondPartSemanticTags = secondPartSummary.getSemanticTags();
                Collection<URI> firstPartAnnotationURIs = firstPartSummary.getAnnotationURIs();
                Collection<URI> secondPartAnnotationURIs = secondPartSummary.getAnnotationURIs();

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

                String type = null;
                if (firstPartType.contentEquals(secondPartType)) {
                    type = firstPartType;
                }

                AnnotationSummary newAnnotationSummary = new SimpleAnnotationSummary(null,
                                                                                     type,
                                                                                     propertyValuePattern,
                                                                                     aggregatedTags,
                                                                                     aggregatedURIs,
                                                                                     qualityScoreFinal);

                results.put(newAnnotationSummary, scoreFinal);
            }
        }
        return results;
    }
}