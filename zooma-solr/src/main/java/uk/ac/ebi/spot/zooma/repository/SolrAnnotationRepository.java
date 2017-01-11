package uk.ac.ebi.spot.zooma.repository;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.solr.repository.SolrCrudRepository;
import uk.ac.ebi.spot.zooma.model.solr.Annotation;
import uk.ac.ebi.spot.zooma.repository.custom.CustomSolrAnnotationRepository;

import java.util.List;


/**
 * Created by olgavrou on 13/10/2016.
 */
//@RepositoryRestResource(exported = false)
public interface SolrAnnotationRepository extends SolrCrudRepository<Annotation, String>, CustomSolrAnnotationRepository {

    List<Annotation> findByPropertyValue(String propertyValue);
}
