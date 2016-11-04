package uk.ac.ebi.spot.repositories.custom;

import uk.ac.ebi.spot.model.AnnotationSummary;

import java.util.List;

/**
 * Created by olgavrou on 14/10/2016.
 */
public interface CustomSolrAnnotationRepository {

    List<AnnotationSummary> findAnnotationSummariesByPropertyValue(String propertyValue);

}
