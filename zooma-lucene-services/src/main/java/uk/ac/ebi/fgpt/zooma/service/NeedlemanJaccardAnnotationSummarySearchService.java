package uk.ac.ebi.fgpt.zooma.service;

import uk.ac.ebi.fgpt.zooma.datasource.PropertyDAO;
import uk.ac.ebi.fgpt.zooma.model.AnnotationSummary;
import uk.ac.ebi.fgpt.zooma.model.Property;
import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.JaccardSimilarity;
import uk.ac.shef.wit.simmetrics.similaritymetrics.NeedlemanWunch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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

    private NormalizerLexicalTechniques normalizer;
    private FilterLexicalTechniques filter;

    public NeedlemanJaccardAnnotationSummarySearchService(AnnotationSummarySearchService annotationSummarySearchService) {
        super(annotationSummarySearchService);
    }

    public PropertyDAO getPropertyDAO() {
        return propertyDAO;
    }

    public void setPropertyDAO(PropertyDAO propertyDAO) {
        this.propertyDAO = propertyDAO;
    }

    public NormalizerLexicalTechniques getNormalizer() {
        return normalizer;
    }

    public void setNormalizer(NormalizerLexicalTechniques normalizer) {
        this.normalizer = normalizer;
    }

    public FilterLexicalTechniques getFilter() {
        return filter;
    }

    public void setFilter(FilterLexicalTechniques filter) {
        this.filter = filter;
    }

    public Collection<String> getPropertyValueDictionary() {
        return propertyValueDictionary;
    }

    @Override
    public Map<AnnotationSummary, Float> searchAndScore_QueryExpansion(String propertyValuePattern) {
        try {
            initOrWait();
        }
        catch (InterruptedException e) {
            throw new RuntimeException("Initialization failed, cannot query", e);
        }

        getLog().debug("Calling searchAndScore_MaxScoreBooleanQuery... ");
        Map<AnnotationSummary, Float> results = super.searchAndScore_QueryExpansion(propertyValuePattern);

        if (!propertyValuePattern.contains(" and ")) {
            // use "Needleman-Wunsch"  and "Jaccard similarity" to find approximate matchings
            Map<String, Float> similarStrings = findApproximateMatchings(propertyValuePattern);

            for (String s : similarStrings.keySet()) {
                if (FilterLexicalTechniques.pass_filter_by_affirmative_negative(s, propertyValuePattern)) {
                    getLog().debug("Calling searchAndScore_MaxScoreBooleanQuery... ");
                    Map<AnnotationSummary, Float> modifiedResults = super.searchAndScore_QueryExpansion(s);

                    for (AnnotationSummary as : modifiedResults.keySet()) {
                        if (results.containsKey(as)) {
                            // results already contains this result!
                            float zoomaScore = results.get(as);
                            //More weight for the lexical score 
                            float ourScore = modifiedResults.get(as) * similarStrings.get(s) * similarStrings.get(s);

                            getLog().debug("zoomaScore: " + zoomaScore + "   " + "ourScore: " + ourScore);
                            if (ourScore > zoomaScore) {
                                //getLog().debug("Increasing score: " + modifiedResults.get(as) +" * " + similarStrings.get(s)+ " = " + modifiedResults.get(as) * similarStrings.get(s));
                                results.put(as, ourScore);
                            }
                        }
                        else {
                            //getLog().debug("Adding new AnnotationSummary. " + modifiedResults.get(as) +" * " + similarStrings.get(s)+ " = " + modifiedResults.get(as) * similarStrings.get(s));
                            //More weight for the lexical score 
                            results.put(as, modifiedResults.get(as) * similarStrings.get(s) * similarStrings.get(s));
                        }
                    }
                }
            }
        }

        getLog().debug("\nFinal scores: ");
        for (AnnotationSummary as : results.keySet()) {

            float FinalScore = results.get(as);
            getLog().debug("\t" + FinalScore);
        }
        return results;

    }

    @Override
    public Map<AnnotationSummary, Float> searchAndScore_QueryExpansion(String propertyType,
                                                                       String propertyValuePattern) {
        try {
            initOrWait();
        }
        catch (InterruptedException e) {
            throw new RuntimeException("Initialization failed, cannot query", e);
        }

        // original query results
        Map<AnnotationSummary, Float> results = super.searchAndScore_QueryExpansion(propertyType, propertyValuePattern);
        if (!propertyValuePattern.contains(" and ")) {
            // use algorithm to find matching properties
            Map<String, Float> similarStrings = findApproximateMatchings(propertyValuePattern);

            for (String s : similarStrings.keySet()) {
                if (FilterLexicalTechniques.pass_filter_by_affirmative_negative(s, propertyValuePattern)) {
                    Map<AnnotationSummary, Float> modifiedResults =
                            super.searchAndScore_QueryExpansion(propertyType, s);
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
     * Uses Needleman-Wunsch and Jaccard similarity algorithms to find approximate matches.
     *
     * @param propertyValuePattern
     * @return
     */
    private Map<String, Float> findApproximateMatchings(String propertyValuePattern) {
        String property_value_normalized = propertyValuePattern.toLowerCase();
        property_value_normalized = getNormalizer().removeStopWords(property_value_normalized);
        property_value_normalized = getNormalizer().removeCharacters(property_value_normalized);

        Map<String, Float> annotations = useNeedlemanWunschExpansion(property_value_normalized, 0.90f, 1, 0.0f);
        if (annotations.isEmpty()) {
            annotations = useJaccardExpansion(property_value_normalized, 0.525f, 1, 0.999f);
        }
        return annotations;
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
        Map<String, Float> expandedPropertyMap = new HashMap<>();
        AbstractStringMetric metric = new NeedlemanWunch();
        if (getPropertyValueDictionary().isEmpty()) {
            for (String property_dictionary : getPropertyValueDictionary()) {
                float result = metric.getSimilarity(propertyValue, property_dictionary);
                if (result >= min_score) {
                    expandedPropertyMap.put(property_dictionary, result);
                }
            }
        }

        Map<String, Float> result = new HashMap<>();
        if (expandedPropertyMap.size() >= 1) {
            result = getFilter().filterAnnotations(expandedPropertyMap, min_score, num_max_annotations, pct_cutoff);
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
            result = getFilter().filterAnnotations(expandedPropertyMap, min_score, num_max_annotations, pct_cutoff);
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
            String property_value = p.getPropertyValue().toLowerCase();

            property_value = getNormalizer().removeStopWords(property_value);
            property_value = getNormalizer().removeCharacters(property_value);

            propertyValueDictionary.add(property_value);
        }
        time_end = System.currentTimeMillis();

        getLog().debug("Loaded property value dictionary of " + propertyValueDictionary.size() + " entries in " +
                               (time_end - time_start) + " milliseconds");
    }
}