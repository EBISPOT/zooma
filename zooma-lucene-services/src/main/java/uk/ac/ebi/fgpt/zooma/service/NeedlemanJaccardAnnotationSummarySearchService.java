package uk.ac.ebi.fgpt.zooma.service;

import uk.ac.ebi.fgpt.zooma.datasource.PropertyDAO;
import uk.ac.ebi.fgpt.zooma.model.AnnotationSummary;
import uk.ac.ebi.fgpt.zooma.model.Property;
import uk.ac.ebi.fgpt.zooma.util.AnnotationSummarySearchCommand;
import uk.ac.ebi.fgpt.zooma.util.ScoreBasedSorter;
import uk.ac.ebi.fgpt.zooma.util.SearchStringProcessor;
import uk.ac.shef.wit.simmetrics.similaritymetrics.JaccardSimilarity;
import uk.ac.shef.wit.simmetrics.similaritymetrics.NeedlemanWunch;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;


/**
 * This class extends AnnotationSummarySearchServiceDecorator and adds fuzzy string searching, in other words,
 * functionality to find approximate matchings. Specifically, the metrics "Needleman-Wunsch"  and "Jaccard similarity"
 * are included. Implementations of simMetrics library are used. http://sourceforge.net/projects/simmetrics/
 *
 * @author Jose Iglesias
 * @author Tony Burdett
 * @date 16/08/13
 */
public class NeedlemanJaccardAnnotationSummarySearchService extends AnnotationSummarySearchServiceDecorator {
    private PropertyDAO propertyDAO;

    private Collection<String> propertyValueDictionary;

    private SearchStringProcessor searchStringProcessor;

    public NeedlemanJaccardAnnotationSummarySearchService(AnnotationSummarySearchService annotationSummarySearchService) {
        super(annotationSummarySearchService);
    }

    public PropertyDAO getPropertyDAO() {
        return propertyDAO;
    }

    public void setPropertyDAO(PropertyDAO propertyDAO) {
        this.propertyDAO = propertyDAO;
    }

    public SearchStringProcessor getSearchStringProcessor() {
        return searchStringProcessor;
    }

    public void setSearchStringProcessor(SearchStringProcessor searchStringProcessor) {
        this.searchStringProcessor = searchStringProcessor;
    }

    public Collection<String> getPropertyValueDictionary() {
        return propertyValueDictionary;
    }

    @Override
    public Collection<AnnotationSummary> search(String propertyValuePattern, final URI... sources) {
        return doExpandedSearch(propertyValuePattern, new AnnotationSummarySearchCommand() {
            @Override public Collection<AnnotationSummary> executeSearch(String propertyValue) {
                return NeedlemanJaccardAnnotationSummarySearchService.super.search(propertyValue, sources);
            }
        });
    }

    @Override
    public Collection<AnnotationSummary> search(final String propertyType,
                                                String propertyValuePattern,
                                                final URI... sources) {
        return doExpandedSearch(propertyValuePattern, new AnnotationSummarySearchCommand() {
            @Override public Collection<AnnotationSummary> executeSearch(String propertyValue) {
                return NeedlemanJaccardAnnotationSummarySearchService.super.search(propertyType,
                                                                                   propertyValue,
                                                                                   sources);
            }
        });
    }

    @Override public Collection<AnnotationSummary> searchByPreferredSources(String propertyValuePattern,
                                                                            final List<URI> preferredSources,
                                                                            final URI... requiredSources) {
        return doExpandedSearch(propertyValuePattern, new AnnotationSummarySearchCommand() {
            @Override public Collection<AnnotationSummary> executeSearch(String propertyValue) {
                return NeedlemanJaccardAnnotationSummarySearchService.super.searchByPreferredSources(
                        propertyValue,
                        preferredSources,
                        requiredSources);
            }
        });
    }

    @Override public Collection<AnnotationSummary> searchByPreferredSources(final String propertyType,
                                                                            String propertyValuePattern,
                                                                            final List<URI> preferredSources,
                                                                            final URI... requiredSources) {
        return doExpandedSearch(propertyValuePattern, new AnnotationSummarySearchCommand() {
            @Override public Collection<AnnotationSummary> executeSearch(String propertyValue) {
                return NeedlemanJaccardAnnotationSummarySearchService.super.searchByPreferredSources(
                        propertyType,
                        propertyValue,
                        preferredSources,
                        requiredSources);
            }
        });
    }

    /**
     * Uses Needleman-Wunsch and Jaccard similarity algorithms to find strings similar to the one supplied.  This method
     * requires a dictionary of terms, which is acquired during initialization by loading all property values from the
     * property DAO
     *
     * @param string the property value being used to expand
     * @return a set of similar property values mapped to a metric indicating their similarity
     */
    private Map<String, Float> findSimilarStrings(String string) {
        Collection<String> processedStrings = new HashSet<>();
        if (getSearchStringProcessor().canProcess(string)) {
            processedStrings = getSearchStringProcessor().processSearchString(string);
        }

        Map<String, Float> results = new HashMap<>();
        for (String processedString : processedStrings) {
            Map<String, Float> annotations = useNeedlemanWunschExpansion(processedString, 0.90f, 1, 0.0f);
            if (annotations.isEmpty()) {
                if (getLog().isTraceEnabled()) {
                    getLog().trace("No results from NeedlemanWunsch expansion, running Jaccard expansion...");
                }
                annotations = useJaccardExpansion(processedString, 0.525f, 1, 0.999f);
            }
            results.putAll(annotations);
        }
        return results;
    }

    /**
     * Uses the supplied annotation summary search command to execute the "original" search, followed by a search
     * against an expanded set of property values (using NeedlemanWunsch/Jaccard algorithms) if no results were obtained
     * from the original search.
     *
     * @param propertyValuePattern the property value pattern to search for
     * @param command              a command that encapsulates the search to execute
     * @return a collection of annotation summaries that satisfy the query
     */
    private Collection<AnnotationSummary> doExpandedSearch(String propertyValuePattern,
                                                           AnnotationSummarySearchCommand command) {
        // execute "original" search
        Collection<AnnotationSummary> results = command.executeSearch(propertyValuePattern);

        // if results are empty, find lexically similar strings and execute "expanded" query
        if (results.isEmpty()) {
            // use "Needleman-Wunsch"  and "Jaccard similarity" to find approximate matchings
            Map<String, Float> similarStrings = findSimilarStrings(propertyValuePattern);

            for (String s : similarStrings.keySet()) {
                if (haveEqualPolarity(s, propertyValuePattern)) {
                    results.addAll(command.executeSearch(s));
                }
            }
        }
        return results;
    }

    /**
     * This methods finds matching properties using "Needleman-Wunsch" distance. Here, simmetrics library is used
     *
     * @param propertyValue       the property value to search for
     * @param min_score           the ZOOMA minimum score parameter
     * @param num_max_annotations the maximum number of annotations that should be returned
     * @param pct_cutoff          the ZOOMA cutoff percentage score
     * @return properties identified by the Needleman-Wunsch algorithm and their similarity score
     */
    private Map<String, Float> useNeedlemanWunschExpansion(String propertyValue,
                                                           float min_score,
                                                           int num_max_annotations,
                                                           float pct_cutoff) {
        try {
            initOrWait();
        }
        catch (InterruptedException e) {
            throw new RuntimeException(getClass().getSimpleName() + " initialization failed", e);
        }

        Map<String, Float> expandedPropertyMap = new HashMap<>();
        NeedlemanWunch nwSimilarity = new NeedlemanWunch();
        if (!getPropertyValueDictionary().isEmpty()) {
            for (String comparedPropertyValue : getPropertyValueDictionary()) {
                float result = nwSimilarity.getSimilarity(propertyValue, comparedPropertyValue);
                if (getLog().isTraceEnabled()) {
                    if (result > 0) {
                        getLog().trace(
                                "Needleman-Wunsch comparison: " + propertyValue + " <=> " + comparedPropertyValue +
                                        " := " + result);
                    }
                }
                if (result >= min_score) {
                    expandedPropertyMap.put(comparedPropertyValue, result);
                }
            }
        }

        Map<String, Float> result = new HashMap<>();
        if (!expandedPropertyMap.isEmpty()) {
            result = filter(expandedPropertyMap, min_score, num_max_annotations, pct_cutoff);
        }
        return result;
    }

    /**
     * This methods finds matching properties using "Jaccard" similarity.  Here, simmetrics library is used
     *
     * @param propertyValue       the property value to search for
     * @param min_score           the ZOOMA minimum score parameter
     * @param num_max_annotations the maximum number of annotations that should be returned
     * @param pct_cutoff          the ZOOMA cutoff percentage score
     * @return properties identified by the Jaccard algorithm and their similarity score
     */
    private Map<String, Float> useJaccardExpansion(String propertyValue,
                                                   float min_score,
                                                   int num_max_annotations,
                                                   float pct_cutoff) {
        try {
            initOrWait();
        }
        catch (InterruptedException e) {
            throw new RuntimeException(getClass().getSimpleName() + " initialization failed", e);
        }

        Map<String, Float> expandedPropertyMap = new HashMap<>();
        JaccardSimilarity jaccardSimilarity = new JaccardSimilarity();
        if (!getPropertyValueDictionary().isEmpty()) {
            for (String comparedPropertyValue : getPropertyValueDictionary()) {
                float result = jaccardSimilarity.getSimilarity(propertyValue, comparedPropertyValue);
                if (getLog().isTraceEnabled()) {
                    if (result > 0) {
                        getLog().trace(
                                "Jaccard comparison: " + propertyValue + " <=> " + comparedPropertyValue +
                                        " := " + result);
                    }
                }
                if (result >= min_score) {
                    expandedPropertyMap.put(comparedPropertyValue, result);
                }
            }
        }

        Map<String, Float> result = new HashMap<>();
        if (!expandedPropertyMap.isEmpty()) {
            result = filter(expandedPropertyMap, min_score, num_max_annotations, pct_cutoff);
        }
        return result;
    }

    /**
     * Filters a map of strings linked to similarity scores based on parameters supplied.  The resulting map will
     * contain a subset of the original map, filtering out any strings with a quality score below the minimum score,
     * with a score that is more than the percentage cutoff away from the top score, or fall outside the maximum number
     * of annotations once sorted by score.
     *
     * @param scoredStrings      the strings to filter, mapped to a quality score
     * @param minScore           the minimum score allowed in the result set
     * @param maxNumberOfStrings the maximum number of strings that should be returned
     * @param cutoffPercentage   anything with a score less than the product of this and the highest score will be
     *                           excluded
     * @return a map of retained, high quality strings
     */
    private Map<String, Float> filter(Map<String, Float> scoredStrings,
                                      float minScore,
                                      int maxNumberOfStrings,
                                      float cutoffPercentage) {
        Map<String, Float> results = new HashMap<>();
        if (!scoredStrings.isEmpty()) {
            ScoreBasedSorter<String> sorter = new ScoreBasedSorter<>();
            List<String> sortedStrings = sorter.sort(scoredStrings);
            if (!sortedStrings.isEmpty()) {
                float top_score = scoredStrings.get(sortedStrings.get(0));
                for (int i = 0; i < maxNumberOfStrings; i++) {
                    String s = sortedStrings.get(i);
                    float score = scoredStrings.get(s);
                    if (score >= minScore) {
                        if (score >= top_score * cutoffPercentage) {
                            results.put(s, scoredStrings.get(s));
                            if (results.size() >= maxNumberOfStrings) {
                                break;
                            }
                        }
                    }
                }
            }
        }
        return results;
    }

    /**
     * Returns true if an only if the sentence contains a negative qualifer (e.g. "no diabetes")
     *
     * @param sentence the sentence to test
     * @return true if it contains a negative assertion, false otherwise
     */
    private boolean isNegative(String sentence) {
        if (sentence.contains(" not ") || sentence.contains(" no ") || sentence.contains(" non ") ||
                sentence.contains(" not-") || sentence.contains(" no-") || sentence.contains(" non-") ||
                sentence.contains(" dont ") || sentence.contains(" don't ") || sentence.contains(" didn't ") ||
                sentence.contains(" n't ") || sentence.contains(" never ")) {
            return true;
        }
        else if (sentence.startsWith("not ") || sentence.startsWith("no ") || sentence.startsWith("non ") ||
                sentence.startsWith("not-") || sentence.startsWith("no-") || sentence.startsWith("non-") ||
                sentence.startsWith("dont ") || sentence.startsWith("don't ") || sentence.startsWith("didn't ") ||
                sentence.startsWith("n't ") || sentence.startsWith("never ")) {
            return true;
        }
        return false;
    }

    /**
     * Returns true if both terms are positive statements or if both terms are negative statements.  So, "diabetes" and
     * "diabetes" returns true, as does "no diabetes" and "no diabetes", whereas "diabetes" and "no diabetes" returns
     * false
     *
     * @param term1 the first term to test
     * @param term2 the second term to test
     * @return true if both statements have an equivalent "polarity" i.e. are both positive or are both negative
     */
    private boolean haveEqualPolarity(String term1, String term2) {
        return isNegative(term1) == isNegative(term2);
    }

    /**
     * Initializes this service.  At startup, a {@link PropertyDAO} is used to extract all properties known to ZOOMA.
     * Returned properties are normalized and cached in a dictionary for future use.
     *
     * @throws IOException
     */
    protected void doInitialization() throws IOException {
        long time_start, time_end;
        time_start = System.currentTimeMillis();

        // get all properties
        Collection<Property> properties = getPropertyDAO().read();

        propertyValueDictionary = new HashSet<>();
        for (Property p : properties) {
            String propertyValue = p.getPropertyValue();
            if (getSearchStringProcessor().canProcess(propertyValue)) {
                propertyValueDictionary.addAll(getSearchStringProcessor().processSearchString(propertyValue));
            }
            else {
                propertyValueDictionary.add(propertyValue);
            }
        }
        time_end = System.currentTimeMillis();
        getLog().debug("Loaded property value dictionary of " + propertyValueDictionary.size() + " entries in " +
                               (time_end - time_start) + " milliseconds");
    }
}