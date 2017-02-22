package uk.ac.ebi.spot.zooma.repository.neo4j;

import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.stereotype.Repository;
import uk.ac.ebi.spot.zooma.model.neo4j.Annotation;

/**
 * Created by olgavrou on 21/02/2017.
 */
@Repository
public interface AnnotationRepository extends GraphRepository<Annotation> {
}
