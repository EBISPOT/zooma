package uk.ac.ebi.spot.zooma.repository.solr;

import org.springframework.data.solr.repository.SolrCrudRepository;
import uk.ac.ebi.spot.zooma.model.solr.Annotation;


/**
 * Created by olgavrou on 13/10/2016.
 */
//@RepositoryRestResource(exported = false)
public interface AnnotationRepository extends SolrCrudRepository<Annotation, String>{
//
//    Annotation findById(String id);
//    Annotation findByMongoid(String mongoid);
//    List<Annotation> findByPropertyValue(String propertyValue);
}
