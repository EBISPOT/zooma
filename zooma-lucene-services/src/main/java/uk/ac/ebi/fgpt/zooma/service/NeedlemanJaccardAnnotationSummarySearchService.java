package uk.ac.ebi.fgpt.zooma.service;

import uk.ac.ebi.fgpt.zooma.datasource.PropertyDAO;
import uk.ac.ebi.fgpt.zooma.model.AnnotationSummary;
import uk.ac.ebi.fgpt.zooma.model.Property;
import uk.ac.ebi.fgpt.zooma.model.TypedProperty;
import uk.ac.ebi.fgpt.zooma.util.SearchStringProcessor;
import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.JaccardSimilarity;
import uk.ac.shef.wit.simmetrics.similaritymetrics.NeedlemanWunch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
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

        if (!propertyValuePattern.contains(" and ")) {
            // use "Needleman-Wunsch"  and "Jaccard similarity" to find approximate matchings
            Map<String, Float> similarStrings = findSimilarProperties(null, propertyValuePattern);

            for (String s : similarStrings.keySet()) {
                if (areBothNegative(s, propertyValuePattern)) {
                    Map<AnnotationSummary, Float> modifiedResults = super.searchAndScore(s);

                    for (AnnotationSummary as : modifiedResults.keySet()) {
                        if (results.containsKey(as)) {
                            // results already contains this result
                            float zoomaScore = results.get(as);
                            // so calculalate the weight of the lexical score based on similarity
                            float ourScore = modifiedResults.get(as) * similarStrings.get(s) * similarStrings.get(s);

                            getLog().debug("zoomaScore: " + zoomaScore + "; ourScore: " + ourScore);
                            if (ourScore > zoomaScore) {
                                // if the lexical score is higher than the zooma score, override zooma result
                                // todo - is this correct?
                                results.put(as, ourScore);
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

        getLog().debug("\nFinal scores: ");
        for (AnnotationSummary as : results.keySet()) {
            float finalScore = results.get(as);
            getLog().debug("\t" + finalScore);
        }
        return results;
    }

    @Override
    public Map<AnnotationSummary, Float> searchAndScore(String propertyType,
                                                        String propertyValuePattern) {
        // original query results
        Map<AnnotationSummary, Float> results = super.searchAndScore(propertyType, propertyValuePattern);
        if (!propertyValuePattern.contains(" and ")) {
            // use algorithm to find matching properties
            Map<String, Float> similarStrings = findSimilarProperties(propertyType, propertyValuePattern);

            for (String s : similarStrings.keySet()) {
                if (areBothNegative(s, propertyValuePattern)) {
                    Map<AnnotationSummary, Float> modifiedResults =
                            super.searchAndScore(propertyType, s);
                    for (AnnotationSummary as : modifiedResults.keySet()) {
                        if (results.containsKey(as)) {
                            // results already contains this result!
                            float zoomaScore = results.get(as);
                            float ourScore = modifiedResults.get(as) * similarStrings.get(s) * similarStrings.get(s);

                            if (ourScore > zoomaScore) {
                                results.put(as, ourScore);
                            }
                        }
                        else {
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
        // todo - only use processors that are suitable based on type
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
            result = filterAnnotations(expandedPropertyMap, min_score, num_max_annotations, pct_cutoff);
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
            result = filterAnnotations(expandedPropertyMap, min_score, num_max_annotations, pct_cutoff);
        }
        return result;
    }

    /**
     * Initializes this service.  At startup, a {@link PropertyDAO} is used to extract all properties known to ZOOMA.
     * Returned properties are normalized and cached in a dictionary for future use.
     *
     * @throws IOException
     */
    public void doInitialization() throws IOException {
        long time_start, time_end;
        time_start = System.currentTimeMillis();

        // get all properties 
        Collection<Property> properties = getPropertyDAO().read();

        propertyValueDictionary = new ArrayList<>();
        for (Property p : properties) {
            String propertyValue = p.getPropertyValue();
            String propertyType = p instanceof TypedProperty ? ((TypedProperty) p).getPropertyType() : null;
            // todo - only use processors that are suitable based on type
            if (getSearchStringProcessor().canProcess(propertyValue)) {
                propertyValueDictionary.addAll(getSearchStringProcessor().processSearchString(propertyValue));
            }
        }
        time_end = System.currentTimeMillis();
        getLog().debug("Loaded property value dictionary of " + propertyValueDictionary.size() + " entries in " +
                               (time_end - time_start) + " milliseconds");
    }

    /**
     * The filter receives a set of annotations and selects the best annotations using several criteria (min_score,
     * num_max_annotations, pct_cutoff). These criteria can be adjusted.
     *
     * @param annotations         the annotations to filter
     * @param min_score           the minimum score
     * @param num_max_annotations
     * @param pct_cutoff
     * @return
     */
    public Map<String, Float> filterAnnotations(Map<String, Float> annotations,
                                                float min_score,
                                                int num_max_annotations,
                                                float pct_cutoff) {
        Map<String, Float> final_annotations = new HashMap<>();
        float top_score = 0.0f;
        float min_score_cut_off = 0.0f;

        if (annotations.size() > 0) {
            Map<String, Float> sortedMap = sortByComparator(annotations);
            Iterator iterator = sortedMap.entrySet().iterator();
            for (int i = 0; i < num_max_annotations; i++) {
                if (iterator.hasNext()) {
                    Map.Entry entry = (Map.Entry) iterator.next();
                    float score_annotation = (Float) entry.getValue();
                    if (score_annotation >= min_score_cut_off) {
                        final_annotations.put((String) entry.getKey(), score_annotation);
                        if (i == 0) {
                            top_score = score_annotation;
                            min_score_cut_off = top_score * pct_cutoff;
                        }
                    }
                }
            }
        }
        return final_annotations;
    }

    /* This method sorts a Map of annotations by score */
    private Map<String, Float> sortByComparator(Map<String, Float> unsortMap) {
        List<Map.Entry<String, Float>> list = new LinkedList<>(unsortMap.entrySet());
        // sort list based on comparator
        Collections.sort(list, new Comparator<Map.Entry<String, Float>>() {
            public int compare(Map.Entry<String, Float> o1, Map.Entry<String, Float> o2) {
                int com = o1.getValue().compareTo(o2.getValue());
                //Descending order
                return com * (-1);
            }
        });

        // put sorted list into map again
        // LinkedHashMap make sure order in which keys were inserted
        Map<String, Float> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<String, Float> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }

    public boolean isNegative(String sentence) {
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

    public boolean areBothNegative(String term1, String term2) {
        return isNegative(term1) == isNegative(term2);
    }
}