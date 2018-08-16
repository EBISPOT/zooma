package uk.ac.ebi.spot.zooma.repository.neo4j;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;
import uk.ac.ebi.spot.zooma.model.neo4j.Property;

/**
 * Created by olgavrou on 27/07/2017.
 */
@Repository
public interface PropertyRepository extends Neo4jRepository<Property, String> {
}
