package uk.ac.ebi.spot.zooma.repository.neo4j;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;
import uk.ac.ebi.spot.zooma.model.neo4j.BiologicalEntity;

/**
 * Created by olgavrou on 27/07/2017.
 */
@Repository
public interface BiologicalEntityRepository extends Neo4jRepository<BiologicalEntity, String> {
}
