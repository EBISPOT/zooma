package uk.ac.ebi.spot.repositories.custom;

import org.springframework.data.repository.query.Param;
import uk.ac.ebi.spot.model.BiologicalEntity;

import java.util.List;

/**
 * This custom interface of the {@link uk.ac.ebi.spot.repositories.BiologicalEntityRepository} gives the opportunity for other queries, other than the
 * standard ones provided by the {@link org.springframework.data.mongodb.repository.MongoRepository}, to be constructed. An implementation of this interface
 * can be defined as long as it is named "BiologicalEntityRepositoryImpl" and as long as the {@link uk.ac.ebi.spot.repositories.BiologicalEntityRepository}
 * extends this interface.
 *
 *
 * Created by olgavrou on 13/09/2016.
 */
public interface CustomBiologicalEntityRepository {
    List<BiologicalEntity> findDistinctByStudiesAccession(@Param("accession") String accession);
    List<BiologicalEntity> findDistinctAnnotatedBiologicalEntities();
}
