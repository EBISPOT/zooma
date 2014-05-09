package uk.ac.ebi.fgpt.zooma.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.zooma.model.AnnotationSummary;
import uk.ac.ebi.fgpt.zooma.model.Property;
import uk.ac.ebi.fgpt.zooma.model.SimpleTypedProperty;
import uk.ac.ebi.fgpt.zooma.util.ZoomaUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * User: jmcmurry
 * Date: 01/05/2014
 * Representation of the set of Zooma Results before and after filters are applied as well as
 * the mapping designation (automatic, requires curation, no results).
 * The filtering algorithm is described here: https://docs.google.com/drawings/d/1GRSaqpBfq2Uouc2QhzTeYpRU0Jm3yuRfW85YBAvV9Lw
 * Where the criteria for automatic curation are satisfied, the automaticAnnotation AnnotationSummary is not null.
 */
public class ZoomaResultsProfile {

    private final String attributeType;
    private final String attributeValue;

    // map of results obtained by querying only type and value (no filters for percentile or score)
    private Map<AnnotationSummary, Float> unfilteredResults;
    // set of results obtained by filtering the unfiltered results using only the percentile; cutoff score is applied after this step
    private Set<AnnotationSummary> resultsFilteredForPercentile = null;

    //Where the criteria for automatic curation are satisfied, the automaticAnnotation AnnotationSummary is stored here.
    private AnnotationSummary automaticAnnotation = null;
    // If the criteria for automatic curation are NOT satisfied, but the criteria for manual curation are satisfied,
    // the best-matching annotation is stored here.
    private AnnotationSummary runnerUpAnnotation = null;

    private ZOOMASearchClient zoomaSearchClient = null;
    private float cutoffScore;
    private float cutoffPercent;

    private String errorMessage = null;

    private MappingCategory mappingCategory;

    public enum MappingCategory {
        AUTOMATIC, REQUIRES_CURATION, NO_RESULTS, ERROR
    }

    private Logger log = LoggerFactory.getLogger(getClass());

    public ZoomaResultsProfile(String attributeType, String attributeValue, float cutoffScore, float cutoffPercent, ZOOMASearchClient zoomaClient) throws ZoomaException {

        this.cutoffPercent = cutoffPercent;
        this.cutoffScore = cutoffScore;
        this.zoomaSearchClient = zoomaClient;

        this.attributeType = attributeType;
        this.attributeValue = attributeValue;

        checkParams();

        this.unfilteredResults = setUnfilteredResults();

        if(this.unfilteredResults == null){
            this.mappingCategory = MappingCategory.ERROR;
        }

        else if(this.unfilteredResults.isEmpty()){
            this.mappingCategory = MappingCategory.NO_RESULTS;
        }

        else {
            this.resultsFilteredForPercentile = setResultsFilteredForPercentile();
            //If appropriate, sets the automatic annotation and runner-up annotation
            this.mappingCategory =  setResultsFilteredForScore();
        }

    }

    private void checkParams() {
        if (attributeType == null || attributeValue == null || zoomaSearchClient == null) {
            String errorMessage = "Constructor parameters for ZoomaResultsProfile must not be null.";
            getLog().error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private AnnotationSummary setRunnerUpAnnotation() {

        if (resultsFilteredForPercentile == null || resultsFilteredForPercentile.isEmpty()) return null;

        // If there's at least one result, initialize best result
        AnnotationSummary bestResult = resultsFilteredForPercentile.iterator().next();

        // if there's more than one result, iterate to find best
        if (resultsFilteredForPercentile.size() > 1) {

            // if there's more than one result, iterate through to update best result
            getLog().debug("Iterating over " + resultsFilteredForPercentile.size() + " filtered Zooma results to find best match.");

            for (AnnotationSummary aresult : resultsFilteredForPercentile) {
                if (aresult.getQuality() > bestResult.getQuality()) bestResult = aresult;
            }
        }

        // log and cache the result
        if (bestResult.getSemanticTags().size() > 1) getLog().warn("Compound URI detected.");
        getLog().debug("Best ZoomaResult:" + bestResult.getID() + "\t" + bestResult.getAnnotatedPropertyType() + "\t" + bestResult.getAnnotatedPropertyValue() + "\t" + bestResult.getSemanticTags());
        getLog().debug("ZoomaResult score: " + bestResult.getQuality());

        // then return it
        return bestResult;
    }

    private Set<AnnotationSummary> setResultsFilteredForPercentile() {
        return ZoomaUtils.filterAnnotationSummaries(unfilteredResults, cutoffPercent);
    }

    private MappingCategory setResultsFilteredForScore() {

        AnnotationSummary automaticAnnotation = null;
        AnnotationSummary runnerUpAnnotation = null;
        MappingCategory mappingCategory = null;

        if (resultsFilteredForPercentile.size() == 1) {
            AnnotationSummary soleResult = resultsFilteredForPercentile.iterator().next();
            if (soleResult.getQuality() >= cutoffScore) {
                automaticAnnotation = soleResult;
                mappingCategory = MappingCategory.AUTOMATIC;
            } else {
                runnerUpAnnotation = soleResult;
                mappingCategory = MappingCategory.REQUIRES_CURATION;
            }
        }

        if (resultsFilteredForPercentile.size() > 1) {
            mappingCategory = MappingCategory.REQUIRES_CURATION;
            runnerUpAnnotation = setRunnerUpAnnotation();
        }

        this.automaticAnnotation = automaticAnnotation;
        this.runnerUpAnnotation = runnerUpAnnotation;
        return mappingCategory;
    }

    private Map<AnnotationSummary, Float> setUnfilteredResults() throws ZoomaException {

        Property property = new SimpleTypedProperty(attributeType, attributeValue);

        Map<AnnotationSummary, Float> fullResultsMap = null;
        try {
            fullResultsMap = zoomaSearchClient.searchZOOMA(property, 0);
            if(fullResultsMap == null)
                System.out.println("map == null");
        } catch (Exception e) {

            String errorMessage = e.getMessage().replaceAll("[^a-zA-Z ]", " ");
            getLog().warn(errorMessage);
            this.errorMessage = errorMessage;
            mappingCategory = MappingCategory.ERROR;

            throw new ZoomaException(e.getMessage());
        }

        return fullResultsMap;
    }

    public String getAttributeType() {
        return attributeType;
    }

    public String getAttributeValue() {
        return attributeValue;
    }

    public Set<AnnotationSummary> getResultsFilteredForPercentile() {
        return resultsFilteredForPercentile;
    }

    public AnnotationSummary getAutomaticAnnotation() {
        return automaticAnnotation;
    }

    public Map<AnnotationSummary, Float> getUnfilteredResults() {
        return unfilteredResults;
    }

    public MappingCategory getMappingCategory() {
        return mappingCategory;
    }

    public Logger getLog() {
        return log;
    }

    public AnnotationSummary getRunnerUpAnnotation() {
        return runnerUpAnnotation;
    }

    public int getNumberOfUnfilteredResults() {
        if (unfilteredResults == null) return -1;
        else return unfilteredResults.size();
    }

    public int getNumberOfFilteredResults() {
        if (resultsFilteredForPercentile == null) return -1;
        else return resultsFilteredForPercentile.size();
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
