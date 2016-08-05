package uk.ac.ebi.spot.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.ac.ebi.spot.model.SimpleStudy;

/**
 * Created by olgavrou on 05/08/2016.
 */
@Repository
public interface StudyRepository extends MongoRepository<SimpleStudy, String> {
}
