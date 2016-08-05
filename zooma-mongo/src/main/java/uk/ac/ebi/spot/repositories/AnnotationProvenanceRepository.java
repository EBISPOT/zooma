package uk.ac.ebi.spot.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.ac.ebi.spot.model.SimpleAnnotationProvenance;

/**
 * Created by olgavrou on 04/08/2016.
 */
@Repository
public interface AnnotationProvenanceRepository extends MongoRepository<SimpleAnnotationProvenance, String> {
}
