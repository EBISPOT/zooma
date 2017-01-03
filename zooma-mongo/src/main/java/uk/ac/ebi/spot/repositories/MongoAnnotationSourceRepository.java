package uk.ac.ebi.spot.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;
import uk.ac.ebi.spot.model.MongoAnnotationSource;


/**
 * Created by olgavrou on 23/11/2016.
 */
@Repository
@RepositoryRestResource(exported = false)
public interface MongoAnnotationSourceRepository  extends MongoRepository<MongoAnnotationSource, String> {
    MongoAnnotationSource findByName(String name);
}
