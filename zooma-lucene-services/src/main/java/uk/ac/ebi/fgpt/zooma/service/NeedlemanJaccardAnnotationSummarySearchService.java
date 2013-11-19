package uk.ac.ebi.fgpt.zooma.service;

import uk.ac.ebi.fgpt.zooma.datasource.PropertyDAO;
import uk.ac.ebi.fgpt.zooma.model.AnnotationSummary;
import uk.ac.ebi.fgpt.zooma.model.Property;
import uk.ac.ebi.fgpt.zooma.model.TypedProperty;
import uk.ac.ebi.fgpt.zooma.util.ScoreBasedSorter;
import uk.ac.ebi.fgpt.zooma.util.SearchStringProcessor;
import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.JaccardSimilarity;
import uk.ac.shef.wit.simmetrics.similaritymetrics.NeedlemanWunch;

import java.io.IOException;
import java.util.ArrayList;
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
    public Map<AnnotationSummary, Float> searchAndScore(String propertyValuePattern) {
        Map<AnnotationSummary, Float> results = super.searchAndScore(propertyValuePattern);

        // if results are empty, find lexically similar strings and requery
        if (results.isEmpty()) {
            // use "Needleman-Wunsch"  and "Jaccard similarity" to find approximate matchings
            Map<String, Float> similarStrings = findSimilarProperties(null, propertyValuePattern);

            for (String s : similarStrings.keySet()) {
                if (areBothNegative(s, propertyValuePattern)) {
                    Map<AnnotationSummary, Float> modifiedResults = super.searchAndScore(s);

                    for (AnnotationSummary as : modifiedResults.keySet()) {
                        if (results.containsKey(as)) {
                            // results already contains this result
                            float previousScore = results.get(as);
                            // so calculate the weight of the lexical score based on similarity
                            float newScore = modifiedResults.get(as) * similarStrings.get(s) * similarStrings.get(s);

                            if (newScore > previousScore) {
                                // if the lexical score is higher than the zooma score, override zooma result
                                results.put(as, newScore);
                            }
                        }
                        else {
                            // add the result, and weight the lexical score based on similarity
                            results.put(as, modifiedResults.get(as) * similarStrings.get(s) * similarStrings.get(s));
                        }
                    }
                }
            }
        }
        return results;
    }

    @Override
    public Map<AnnotationSummary, Float> searchAndScore(String propertyType,
                                                        String propertyValuePattern) {
        // original query results
        Map<AnnotationSummary, Float> results = super.searchAndScore(propertyType, propertyValuePattern);

        // if results are empty, find lexically similar strings and requery
        if (results.isEmpty()) {
            // use algorithm to find matching properties
            Map<String, Float> similarStrings = findSimilarProperties(propertyType, propertyValuePattern);

            for (String s : similarStrings.keySet()) {
                if (areBothNegative(s, propertyValuePattern)) {
                    Map<AnnotationSummary, Float> modifiedResults = super.searchAndScore(propertyType, s);
                    for (AnnotationSummary as : modifiedResults.keySet()) {
                        if (results.containsKey(as)) {
                            // results already contains this result
                            float previousScore = results.get(as);
                            // so calculate the weight of the lexical score based on similarity
                            float newScore = modifiedResults.get(as) * similarStrings.get(s) * similarStrings.get(s);

                            if (newScore > previousScore) {
                                // if the lexical score is higher than the zooma score, override zooma result
                                results.put(as, newScore);
                            }
                        }
                        else {
                            // add the result, and weight the lexical score based on similarity
                            results.put(as, modifiedResults.get(as) * similarStrings.get(s) * similarStrings.get(s));
                        }
                    }
                }
            }
        }
        return results;
    }

    /**
     * Uses Needleman-Wunsch and Jaccard similarity algorithms to find similar property values to those given.  The type
     * is used to select which string processors are used
     *
     * @param propertyType         the type of the property being used to search for similar strings
     * @param propertyValuePattern the property value being used to expand
     * @return a set of similar property values mapped to a metric indicating their similarity
     */
    private Map<String, Float> findSimilarProperties(String propertyType, String propertyValuePattern) {
        Collection<String> processedStrings = new HashSet<>();
        if (getSearchStringProcessor().canProcess(propertyValuePattern)) {
            processedStrings = getSearchStringProcessor().processSearchString(propertyValuePattern);
        }

        Map<String, Float> results = new HashMap<>();
        for (String processedString : processedStrings) {
            Map<String, Float> annotations = useNeedlemanWunschExpansion(processedString, 0.90f, 1, 0.0f);
            if (annotations.isEmpty()) {
                annotations = useJaccardExpansion(processedString, 0.525f, 1, 0.999f);
            }
            results.putAll(annotations);
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
        AbstractStringMetric metric = new NeedlemanWunch();
        if (!getPropertyValueDictionary().isEmpty()) {
            for (String property_dictionary : getPropertyValueDictionary()) {
                float result = metric.getSimilarity(propertyValue, property_dictionary);
                if (result >= min_score) {
                    expandedPropertyMap.put(property_dictionary, result);
                }
            }
        }

        Map<String, Float> result = new HashMap<>();
        if (expandedPropertyMap.size() >= 1) {
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
        AbstractStringMetric metric = new JaccardSimilarity();
        if (!getPropertyValueDictionary().isEmpty()) {
            for (String property_dictionary : getPropertyValueDictionary()) {
                float result = metric.getSimilarity(propertyValue, property_dictionary);
                if (result >= min_score) {
                    expandedPropertyMap.put(property_dictionary, result);
                }
            }
        }

        Map<String, Float> result = new HashMap<>();
        if (expandedPropertyMap.size() >= 1) {
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
                    if (score > minScore) {
                        if (score > top_score * cutoffPercentage) {
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

    private boolean areBothNegative(String term1, String term2) {
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

        propertyValueDictionary = new ArrayList<>();
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