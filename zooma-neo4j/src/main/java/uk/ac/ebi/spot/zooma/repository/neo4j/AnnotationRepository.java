package uk.ac.ebi.spot.zooma.repository.neo4j;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.stereotype.Repository;
import uk.ac.ebi.spot.zooma.model.neo4j.*;

/**
 * Created by olgavrou on 21/02/2017.
 */
@Repository
public interface AnnotationRepository extends GraphRepository<Annotation> {

    @Query("match (n:Annotation{quality:{4}})-[:HAS_PROPERTY]->(p:Property)" +
            "match (n)-[:HAS_PROVENANCE]->(prov:AnnotationProvenance)-[:SOURCE]->(s:Source)" +
            "where prov.annotationDate = {3}" +
            "and p.propertyType = {0} and p.propertyValue = {1}" +
            "and s.name = {2}" +
            "return n")
    Annotation find(String propertyType, String propertyValue, String sourceName, String annotatedDate, float quality);

    Annotation findByMongoId(String mongoid);
}
