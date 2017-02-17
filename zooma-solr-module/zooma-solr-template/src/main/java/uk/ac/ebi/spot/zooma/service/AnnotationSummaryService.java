package uk.ac.ebi.spot.zooma.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.zooma.model.AnnotationSummary;
import uk.ac.ebi.spot.zooma.repository.AnnotationSummaryRepository;
import uk.ac.ebi.spot.zooma.utils.Scorer;
import uk.ac.ebi.spot.zooma.utils.SummaryUtils;

import java.util.*;

/**
 * Created by olgavrou on 13/02/2017.
 */
@Service
public class AnnotationSummaryService {

    @Autowired
    Scorer<AnnotationSummary> scorer;

    @Autowired
    AnnotationSummaryRepository annotationSummaryRepository;

    public List<AnnotationSummary> findAnnotationSummariesByPropertyValue(String propertyValue) {
        return findAnnotationSummariesByPropertyValue(propertyValue, null);
    }

    public List<AnnotationSummary> findAnnotationSummariesByPropertyValue(String propertyValue, List<String> sourceNames) {
        Map<String, String> criteriaMap = new HashMap<>();
        criteriaMap.put("propertyValue", propertyValue);
        return  findAnnotationSummariesByCriteria(criteriaMap, sourceNames, propertyValue);

    }

    /**
     * Defines the criteria based on the propertyValue and propertyType and then performs a search
     *
     * @param propertyType the type of the term that will be searched for,
     *                     and that that can help boost the score of the results
     * @param propertyValue the term that will be searched for

     * @return the list of {@link AnnotationSummary}s that where calculated
     */
    public List<AnnotationSummary> findAnnotationSummariesByPropertyValueAndPropertyType(String propertyType, String propertyValue) {
        return findAnnotationSummariesByPropertyValueAndPropertyType(propertyType, propertyValue, null);
    }

    public List<AnnotationSummary> findAnnotationSummariesByPropertyValueAndPropertyType(String propertyType, String propertyValue, List<String> sourceNames) {
        Map<String, String> criteriaMap = new HashMap<>();
        criteriaMap.put("propertyType", propertyType);
        criteriaMap.put("propertyValue", propertyValue);
        return findAnnotationSummariesByCriteria(criteriaMap, sourceNames, propertyValue);
    }

    /**
     *
     */
    private List<AnnotationSummary>  findAnnotationSummariesByCriteria(Map<String, String> criteriaMap, List<String> desiredSources, String propertyValue) {

        List<AnnotationSummary> annotations = annotationSummaryRepository.findAnnotationSummariesByCriteria(criteriaMap, desiredSources);

        List<AnnotationSummary> finalAnnotations = getSummariesAfterSimilarityScore(annotations, propertyValue);

        return finalAnnotations;

    }

    private List<AnnotationSummary> getSummariesAfterSimilarityScore(List<AnnotationSummary> annotations, String propertyValue) {
        float max = 1.0f;

        //Get max and MIN qualities
        for (AnnotationSummary annotationSummary : annotations){
            if (annotationSummary.getQuality() > max){
                max = annotationSummary.getQuality();
            }
        }

        //Score
        Map<AnnotationSummary, Float> annotationsToScore = scorer.score(annotations, propertyValue);

        ArrayList<AnnotationSummary> summaries = new ArrayList<>();
        //Normalise Score
        for (AnnotationSummary annotation : annotationsToScore.keySet()){
            float normScore = SummaryUtils.normaliseScore(annotationsToScore.get(annotation), max);
            annotation.setQuality(normScore);
            summaries.add(annotation);
        }

        return summaries;
    }

}
