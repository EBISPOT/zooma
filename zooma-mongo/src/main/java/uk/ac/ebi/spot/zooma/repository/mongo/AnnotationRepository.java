package uk.ac.ebi.spot.zooma.repository.mongo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.stereotype.Repository;
import uk.ac.ebi.spot.zooma.model.mongo.MongoAnnotation;

import java.util.Collection;

/**
 * Created by olgavrou on 03/08/2016.
 */
@Repository
//@RepositoryRestResource(exported = false)
public interface AnnotationRepository extends MongoRepository<MongoAnnotation, String> {

    @RestResource(path = "findByPropertyValue", rel = "findByPropertyValue")
    Page<MongoAnnotation> findByPropertyPropertyValue(@Param("propertyValue") String propertyValue, Pageable pageable);

    @RestResource(path = "findByPropertyType", rel = "findByPropertyType")
    Page<MongoAnnotation> findByPropertyPropertyType(@Param("propertyType") String propertyType, Pageable pageable);

    @RestResource(path = "findByPropertyTypeAndValue", rel = "findByPropertyTypeAndValue")
    Page<MongoAnnotation> findByPropertyPropertyTypeAndPropertyPropertyValue(@Param("propertyType") String propertyType, @Param("propertyValue") String propertyValue, Pageable pageable);

    @RestResource(path = "findBySemanticTag", rel = "findBySemanticTag")
    Page<MongoAnnotation> findBySemanticTagIn(@Param("semanticTag") Collection<String> semanticTag, Pageable pageable);

    @RestResource(path = "findBySourceName", rel = "findBySourceName")
    Page<MongoAnnotation> findByProvenanceSourceName(@Param("name") String name, Pageable pageable);

    @Override
    @RestResource(exported = false)
    void delete(String s);

    @Override
    @RestResource(exported = false)
    void delete(MongoAnnotation entity);

    @Override
    @RestResource(exported = false)
    void delete(Iterable<? extends MongoAnnotation> entities);

    @Override
    @RestResource(exported = false)
    void deleteAll();
}
