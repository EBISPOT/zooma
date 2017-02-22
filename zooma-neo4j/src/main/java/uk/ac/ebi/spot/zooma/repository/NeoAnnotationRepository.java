package uk.ac.ebi.spot.zooma.repository;

import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.stereotype.Repository;
import uk.ac.ebi.spot.zooma.model.Annotation;

/**
 * Created by olgavrou on 21/02/2017.
 */
@Repository
public interface NeoAnnotationRepository extends GraphRepository<Annotation> {
}
