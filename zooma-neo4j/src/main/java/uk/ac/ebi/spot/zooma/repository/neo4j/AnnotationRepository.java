package uk.ac.ebi.spot.zooma.repository.neo4j;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.stereotype.Repository;
import uk.ac.ebi.spot.zooma.model.neo4j.*;

import java.util.List;


/**
 * Created by olgavrou on 21/02/2017.
 */
@Repository
public interface AnnotationRepository extends Neo4jRepository<Annotation, Long> {

    Annotation findByMongoid(@Param("mongoid") String mongoid);

    @Query("match (a:Annotation) where a.mongoid={mongoid} return a")
    Annotation findAnnotationByMongoid(@Param("mongoid") String mongoid);

    @Query("match (p:Property)<-[propR:HAS_PROPERTY]-(a:Annotation)-[semTagR:HAS_SEMANTIC_TAG]->(s:SemanticTag) " +
            "where p.propertyValue =~ {propertyValue} " +
            "and p.propertyType = {propertyType} " +
            "return distinct p.propertyValue, p.propertyType, a, s, propR, semTagR")
    List<Annotation> findByAnnotationPropertyPropertyTypeAndAnnotationPropertyPropertyValue(@Param("propertyType") String propertyType,
                                                                                            @Param("propertyValue") String propertyValue);

//    List<Annotation> findByBiologicalEntityBioEntity(@Param("bioentity") String bioEntity);
}
