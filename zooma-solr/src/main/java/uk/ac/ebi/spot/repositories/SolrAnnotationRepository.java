package uk.ac.ebi.spot.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.solr.repository.SolrCrudRepository;
import org.springframework.stereotype.Repository;
import uk.ac.ebi.spot.model.SolrAnnotation;
import uk.ac.ebi.spot.repositories.custom.CustomSolrAnnotationRepository;

import java.util.List;


/**
 * Created by olgavrou on 13/10/2016.
 */
@Repository
public interface SolrAnnotationRepository extends SolrCrudRepository<SolrAnnotation, String>, CustomSolrAnnotationRepository {

    List<SolrAnnotation> findByAnnotatedPropertyValue(String annotatedPropertyValue);
}
