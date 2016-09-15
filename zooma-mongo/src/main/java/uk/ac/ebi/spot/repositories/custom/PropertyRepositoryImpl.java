package uk.ac.ebi.spot.repositories.custom;

import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ebi.spot.model.Property;
import uk.ac.ebi.spot.repositories.AnnotationRepository;

import java.util.List;


/**
 * This class implements the methods of the Custom repository. It uses the {@link AnnotationRepository} to query and return nested fields
 * of the {@link uk.ac.ebi.spot.model.Annotation} document.
 *
 * As {@link uk.ac.ebi.spot.model.Property} mongo implementation is stored as a nested document of the {@link uk.ac.ebi.spot.model.Annotation} document,
 * and not in a separate collection, we need to define the custom repository to return the nested fields through the {@link uk.ac.ebi.spot.repositories.AnnotationRepository},
 * and not through it's own repository queries.
 *
 *
 * Created by olgavrou on 15/09/2016.
 */
public class PropertyRepositoryImpl implements CustomPropertyRepository {

    @Autowired
    AnnotationRepository annotationRepository;

    @Override
    public List<Property> findDistinctAnnotatedProperties() {
        return annotationRepository.findDistinctAnnotatedProperties();
    }

    @Override
    public List<String> findAllPropertyTypes() {
        return annotationRepository.findAllPropertyTypes();
    }

    @Override
    public List<Property> findPropertyFromPropertyType(String type) {
        return annotationRepository.findAnnotatedPropertyByAnnotatedPropertyPropertyType(type);
    }

    @Override
    public List<Property> findPropertyFromPropertyValue(String value) {
        return annotationRepository.findAnnotatedPropertyByAnnotatedPropertyPropertyValue(value);
    }

    @Override
    public List<Property> findPropertyFromPropertyTypeAndPropertyValue(String type, String value) {
        return annotationRepository.findAnnotatedPropertyByAnnotatedPropertyPropertyTypeAndByAnnotatedPropertyPropertyValue(type, value);
    }
}
