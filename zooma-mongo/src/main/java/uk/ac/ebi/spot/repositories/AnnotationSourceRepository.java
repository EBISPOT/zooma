package uk.ac.ebi.spot.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import uk.ac.ebi.spot.model.SimpleAnnotationSource;

/**
 * Created by olgavrou on 04/08/2016.
 */
public interface AnnotationSourceRepository extends MongoRepository<SimpleAnnotationSource, String> {
}
