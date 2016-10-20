package uk.ac.ebi.spot.repositories.custom;

import org.springframework.data.domain.Pageable;
import uk.ac.ebi.spot.model.AnnotationSummary;
import uk.ac.ebi.spot.model.SolrAnnotation;

import java.util.List;

/**
 * Created by olgavrou on 14/10/2016.
 */
public interface CustomSolrAnnotationRepository {

    List<AnnotationSummary> findByAnnotatedPropertyValueGroupBySemanticTags(String annotatedPropertyValue);

}
