package uk.ac.ebi.spot.zooma.repository.solr;

import org.springframework.data.repository.query.Param;
import org.springframework.data.solr.repository.Query;
import org.springframework.data.solr.repository.SolrCrudRepository;
import uk.ac.ebi.spot.zooma.model.solr.Annotation;

import java.util.List;


/**
 * Created by olgavrou on 13/10/2016.
 */
//@RepositoryRestResource(exported = false)
public interface AnnotationRepository extends SolrCrudRepository<Annotation, String>{

    @Query("propertyValue:?0&defType=edismax&bf=product(votes,sourceNum,quality)")
    List<Annotation> findByPropertyValue(@Param("propertyValue") String propertyValue);
//
//    Annotation findById(String id);
//    Annotation findByMongoid(String mongoid);
//    List<Annotation> findByPropertyValue(String propertyValue);
}
