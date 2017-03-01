package uk.ac.ebi.spot.zooma.repository.solr;

import org.springframework.data.solr.repository.SolrCrudRepository;
import org.springframework.stereotype.Repository;
import uk.ac.ebi.spot.zooma.model.solr.Annotation;

import java.util.List;


/**
 * Created by olgavrou on 13/10/2016.
 */
//@RepositoryRestResource(exported = false)
@Repository
public interface SolrAnnotationRepository extends SolrCrudRepository<Annotation, String>{
//
//    Annotation findById(String id);
//    Annotation findByMongoid(String mongoid);
//    List<Annotation> findByPropertyValue(String propertyValue);
}
