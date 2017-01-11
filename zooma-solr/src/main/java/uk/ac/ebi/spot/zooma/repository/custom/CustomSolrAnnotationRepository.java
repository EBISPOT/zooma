package uk.ac.ebi.spot.zooma.repository.custom;

import uk.ac.ebi.spot.zooma.model.solr.Annotation;
import uk.ac.ebi.spot.zooma.model.solr.AnnotationSummary;
import uk.ac.ebi.spot.zooma.repository.SolrAnnotationRepository;

import java.util.List;

/**
 * This custom temp of the {@link SolrAnnotationRepository} gives the opportunity for other queries, other than the
 * standard ones provided by the {@link org.springframework.data.solr.repository.SolrCrudRepository}, to be constructed. An implementation of
 * this temp can be defined as long as it is named "SolrAnnotationRepositoryImpl" and as long as the {@link SolrAnnotationRepository}
 * extends this temp. The SolrAnnotationRepositoryImpl will be picked up as an extension of the {@link SolrAnnotationRepository}
 * and it's methods will be available to use through it.
 *
 * Created by olgavrou on 14/10/2016.
 */
public interface CustomSolrAnnotationRepository {

    /**
     * Takes a value and queries solr for it's {@link Annotation}s, and returns the {@link AnnotationSummary}s calculated from the
     * found Annotations
     *
     * @param propertyValue the term that will be searched for
     * @return  a list of the {@link AnnotationSummary}s that are produced from the {@link Annotation}s found
     */
    List<AnnotationSummary> findAnnotationSummariesByPropertyValue(String propertyValue);

    /**
     * Takes a value and queries solr for it's {@link Annotation}s, and returns the {@link AnnotationSummary}s calculated from the
     * found Annotations. Takes a list of source names and limit's the search to those sources.
     *
     * @param propertyValue the term that will be searched for
     * @param sourceNames a list of the source names we want to limit the search to
     * @return  a list of the {@link AnnotationSummary}s that are produced from the {@link Annotation}s found
     */
    List<AnnotationSummary> findAnnotationSummariesByPropertyValue(String propertyValue, List<String> sourceNames);

    /**
     * Takes a value and a type and queries solr for it's {@link Annotation}s, and returns the {@link AnnotationSummary}s calculated from the
     * found Annotations
     *
     * @param propertyType the type of a term
     * @param propertyValue the term that will be searched for

     * @return a list of the {@link AnnotationSummary}s that are produced from the {@link Annotation}s found
     */
    List<AnnotationSummary> findAnnotationSummariesByPropertyValueAndPropertyType(String propertyType, String propertyValue);

    /**
     * Takes a value and a type and queries solr for it's {@link Annotation}s, and returns the {@link AnnotationSummary}s calculated from the
     * found Annotations. Takes a list of source names and limit's the search to those sources.
     *
     * @param propertyType the type of a term
     * @param sourceNames a list of the source names we want to limit the search to
     * @param propertyValue the term that will be searched for

     * @return a list of the {@link AnnotationSummary}s that are produced from the {@link Annotation}s found
     */
    List<AnnotationSummary> findAnnotationSummariesByPropertyValueAndPropertyType(String propertyType, String propertyValue, List<String> sourceNames);

}
