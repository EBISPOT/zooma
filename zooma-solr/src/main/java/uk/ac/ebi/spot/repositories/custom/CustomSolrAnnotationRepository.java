package uk.ac.ebi.spot.repositories.custom;

import uk.ac.ebi.spot.model.AnnotationSummary;

import java.util.List;

/**
 * This custom interface of the {@link uk.ac.ebi.spot.repositories.SolrAnnotationRepository} gives the opportunity for other queries, other than the
 * standard ones provided by the {@link org.springframework.data.solr.repository.SolrCrudRepository}, to be constructed. An implementation of
 * this interface can be defined as long as it is named "SolrAnnotationRepositoryImpl" and as long as the {@link uk.ac.ebi.spot.repositories.SolrAnnotationRepository}
 * extends this interface. The SolrAnnotationRepositoryImpl will be picked up as an extension of the {@link uk.ac.ebi.spot.repositories.SolrAnnotationRepository}
 * and it's methods will be available to use through it.
 *
 * Created by olgavrou on 14/10/2016.
 */
public interface CustomSolrAnnotationRepository {

    /**
     * Takes a value and queries solr for it's {@link uk.ac.ebi.spot.model.Annotation}s, and returns the {@link AnnotationSummary}s calculated from the
     * found Annotations
     *
     * @param propertyValue the term that will be searched for
     * @return  a list of the {@link AnnotationSummary}s that are produced from the {@link uk.ac.ebi.spot.model.Annotation}s found
     */
    List<AnnotationSummary> findAnnotationSummariesByPropertyValue(String propertyValue);

    /**
     * Takes a value and a type and queries solr for it's {@link uk.ac.ebi.spot.model.Annotation}s, and returns the {@link AnnotationSummary}s calculated from the
     * found Annotations
     *
     * @param propertyType the type of a term
     * @param propertyValue the term that will be searched for

     * @return a list of the {@link AnnotationSummary}s that are produced from the {@link uk.ac.ebi.spot.model.Annotation}s found
     */
    List<AnnotationSummary> findAnnotationSummariesByPropertyValueAndPropertyType(String propertyType, String propertyValue);

}
