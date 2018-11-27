package uk.ac.ebi.spot.zooma.repository.solr;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.solr.repository.Query;
import org.springframework.data.solr.repository.SolrCrudRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Repository;
import uk.ac.ebi.spot.zooma.model.solr.Annotation;

import java.time.LocalDateTime;
import java.util.List;


/**
 * Created by olgavrou on 13/10/2016.
 */
@Repository
@RestResource(exported = false)
public interface AnnotationRepository extends SolrCrudRepository<Annotation, String>{

//    @Query(defType = "edismax", value = "propertyValue:\"?0\" OR propertyValueStr:\"?0\"^10 OR propertyValue:?0") //bf="sum(div(1,sum(0.001,ms(\"?1Z\",lastModified))),product(sum(votes,quality,sourceNum),0.001))"
//    Page<Annotation> findByPropertyValue(@Param("propertyValue") String propertyValue, @Param("lastModified") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime lastModified, Pageable pageable);
//
////    @CustomQuery("propertyValue:\"?0\" OR propertyValueStr:\"?0\"^10 OR propertyValue:?0" +
////            "&defType=edismax&fl=*,score&rows=1")
//    Annotation findByPropertyValueOrderByLastModifiedDesc(@Param("propertyValue") String propertyValue);
//
//    @org.springframework.data.solr.repository.Query("propertyValue:\"?1\" AND propertyType:\"?0\" OR propertyValueStr:\"?1\"^10 OR propertyValue:?1" +
//            "&sum(product(recip(ms(NOW,lastModified),3.16e-11,1,1),1000),product(sum(votes,quality,sourceNum),0.001))" +
//            "&defType=edismax&fl=*,score")
//    List<Annotation> findByPropertyTypeAndPropertyValue(@Param("propertyType") String propertyType, @Param("propertyValue") String propertyValue);

}
