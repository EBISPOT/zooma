package uk.ac.ebi.spot.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.ac.ebi.spot.model.SimpleProperty;

/**
 * Created by olgavrou on 09/08/2016.
 */
@Repository
public interface PropertyRepository extends MongoRepository<SimpleProperty, String> {
}
