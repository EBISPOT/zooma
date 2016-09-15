package uk.ac.ebi.spot.repositories.custom;

import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ebi.spot.model.Property;
import uk.ac.ebi.spot.model.Study;
import uk.ac.ebi.spot.repositories.AnnotationRepository;

import java.net.URI;
import java.util.List;

/**
 * This class implements the methods of the Custom repository. It uses the {@link AnnotationRepository} to query and return nested fields
 * of the {@link uk.ac.ebi.spot.model.Annotation} document.
 *
 * As {@link uk.ac.ebi.spot.model.Study} mongo implementation is stored as a nested document of the {@link uk.ac.ebi.spot.model.Annotation} document,
 * and not in a separate collection, we need to define the custom repository to return the nested fields through the {@link uk.ac.ebi.spot.repositories.AnnotationRepository},
 * and not through it's own repository queries.
 *
 * Created by olgavrou on 14/09/2016.
 */
public class StudyRepositoryImpl implements CustomStudyRepository {

    @Autowired
    AnnotationRepository annotationRepository;

    @Override
    public List<Study> findDistinctStudies() {
        return annotationRepository.findDistinctAnnotatedBiologicalEntitiesStudies();
    }

    @Override
    public List<Study> findBySemanticTags(URI... semanticTags) {
        return annotationRepository.findDistinctAnnotatedBiologicalEntitiesStudiesBySemanticTags(semanticTags);
    }

    @Override
    public List<Study> findByAccession(String accession) {
        return annotationRepository.findDistinctAnnotatedBiologicalEntitiesStudiesByAccession(accession);
    }

    @Override
    public List<Study> findByProperty(Property... properties) {
        return null;
    }
}
