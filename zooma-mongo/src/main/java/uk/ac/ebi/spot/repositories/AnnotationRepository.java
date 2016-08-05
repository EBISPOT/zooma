package uk.ac.ebi.spot.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import uk.ac.ebi.spot.model.Property;
import uk.ac.ebi.spot.model.SimpleAnnotation;

import java.net.URI;

/**
 * Created by olgavrou on 03/08/2016.
 */
public interface AnnotationRepository extends MongoRepository<SimpleAnnotation, String> {

    SimpleAnnotation findByAnnotatedProperty(@Param("annotatedProperty") Property annotatedProperty);

    SimpleAnnotation findByUri(@Param("uri") URI uri);

    SimpleAnnotation findById(@Param("Id") String id);

}
