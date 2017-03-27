package uk.ac.ebi.spot.zooma.repository.solr;

import org.springframework.data.repository.query.Param;
import org.springframework.data.solr.repository.Query;
import org.springframework.data.solr.repository.SolrCrudRepository;
import uk.ac.ebi.spot.zooma.model.solr.AnnotationSummary;

import java.util.List;


/**
 * Created by olgavrou on 13/10/2016.
 */
//@RepositoryRestResource(exported = false)
public interface AnnotationSummaryRepository extends SolrCrudRepository<AnnotationSummary, String>{

    @Query("propertyValue:?0&defType=edismax&bf=product(votes,sourceNum,quality)")
    List<AnnotationSummary> findByPropertyValue(@Param("propertyValue") String propertyValue);
//
//    AnnotationSummary findById(String id);
//    AnnotationSummary findByMongoid(String mongoid);
//    List<AnnotationSummary> findByPropertyValue(String propertyValue);
}
