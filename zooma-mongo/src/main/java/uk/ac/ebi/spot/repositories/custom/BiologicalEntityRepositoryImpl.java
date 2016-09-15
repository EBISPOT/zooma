package uk.ac.ebi.spot.repositories.custom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import uk.ac.ebi.spot.model.BiologicalEntity;
import uk.ac.ebi.spot.repositories.AnnotationRepository;

import java.util.List;

/**
 * This class implements the methods of the Custom repository. It uses the {@link AnnotationRepository} to query and return nested fields
 * of the {@link uk.ac.ebi.spot.model.Annotation} document.
 *
 * As {@link uk.ac.ebi.spot.model.BiologicalEntity} mongo implementation is stored as a nested document of the {@link uk.ac.ebi.spot.model.Annotation} document,
 * and not in a separate collection, we need to define the custom repository to return the nested fields through the {@link uk.ac.ebi.spot.repositories.AnnotationRepository},
 * and not through it's own repository queries.
 *
 * Created by olgavrou on 14/09/2016.
 */
public class BiologicalEntityRepositoryImpl implements CustomBiologicalEntityRepository {

    @Autowired
    AnnotationRepository annotationRepository;

    @Override
    public List<BiologicalEntity> findDistinctByStudiesAccession(@Param("accession") String accession) {
        return annotationRepository.findDistinctAnnotatedBiologicalEntitiesByAnnotatedBiologicalEntitiesStudiesAccession(accession);
    }


    @Override
    public List<BiologicalEntity> findDistinctAnnotatedBiologicalEntities() {
        return annotationRepository.findDistinctAnnotatedBiologicalEntities();
    }
}
