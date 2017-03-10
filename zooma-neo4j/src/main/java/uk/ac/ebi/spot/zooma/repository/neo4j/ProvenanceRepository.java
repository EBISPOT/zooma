package uk.ac.ebi.spot.zooma.repository.neo4j;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import uk.ac.ebi.spot.zooma.model.neo4j.Provenance;

import java.util.List;

/**
 * Created by olgavrou on 10/03/2017.
 */
public interface ProvenanceRepository extends Neo4jRepository<Provenance, Long> {
    List<Provenance> findById(Long id);
}
