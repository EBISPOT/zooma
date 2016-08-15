package uk.ac.ebi.spot.repositories;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.ac.ebi.spot.model.SimpleStudy;

import java.util.List;

/**
 * Created by olgavrou on 05/08/2016.
 */
@Repository
public interface StudyRepository extends MongoRepository<SimpleStudy, String> {
}
