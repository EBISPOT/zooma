package uk.ac.ebi.spot.zooma.repository.neo4j;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;
import uk.ac.ebi.spot.zooma.model.neo4j.*;

import java.util.List;


/**
 * Created by olgavrou on 21/02/2017.
 */
@Repository
public interface AnnotationRepository extends Neo4jRepository<Annotation, Long> {

    @Query("match (a:Annotation) where a.mongoid={0} return a")
    Annotation findByMongoid(String mongoid);


    @Query("match (p:Property)<-[propR:HAS_PROPERTY]-(a:Annotation)-[semTagR:HAS_SEMANTIC_TAG]->(s:SemanticTag) " +
            "where p.propertyValue =~ {1} " +
            "and p.propertyType = {0} " +
            "return distinct p.propertyValue, p.propertyType, a, s, propR, semTagR")
    List<Annotation> findByAnnotationPropertyPropertyTypeAndAnnotationPropertyPropertyValue(String propertyType,
                                                                                           String propertyValue);
}
