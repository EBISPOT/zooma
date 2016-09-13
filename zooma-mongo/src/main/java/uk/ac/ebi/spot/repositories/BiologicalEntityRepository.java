package uk.ac.ebi.spot.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.ac.ebi.spot.model.BiologicalEntity;
import uk.ac.ebi.spot.model.SimpleBiologicalEntity;

import java.util.List;

/**
 * Created by olgavrou on 05/08/2016.
 */
@Repository
public interface BiologicalEntityRepository extends MongoRepository<SimpleBiologicalEntity, String> {

}
