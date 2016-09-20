package uk.ac.ebi.spot.repositories.custom;

import uk.ac.ebi.spot.model.BiologicalEntity;
import uk.ac.ebi.spot.model.Property;
import uk.ac.ebi.spot.model.Study;

import java.net.URI;
import java.util.List;

/**
 * This custom interface of the {@link uk.ac.ebi.spot.repositories.MongoAnnotationRepository} gives the opportunity for other queries, other than the
 * standard ones provided by the {@link org.springframework.data.mongodb.repository.MongoRepository}, to be constructed. An implementation of
 * this interface can be defined as long as it is named "MongoAnnotationRepositoryImpl" and as long as the {@link uk.ac.ebi.spot.repositories.MongoAnnotationRepository}
 * extends this interface. The MongoAnnotationRepositoryImpl will be picked up as an extension of the {@link uk.ac.ebi.spot.repositories.MongoAnnotationRepository}
 * and it's methods will be available to use through it.
 *
 *
 * Created by olgavrou on 14/09/2016.
 */
public interface CustomMongoAnnotationRepository {

    //for BiologicalEntityRepository
    List<BiologicalEntity> findDistinctAnnotatedBiologicalEntities();
    List<BiologicalEntity> findDistinctAnnotatedBiologicalEntitiesByAnnotatedBiologicalEntitiesStudiesAccession(String accession);

    //for StudyRepository
    List<Study> findDistinctAnnotatedBiologicalEntitiesStudies();
    List<Study> findDistinctAnnotatedBiologicalEntitiesStudiesBySemanticTags(URI... semanticTags);
    List<Study> findDistinctAnnotatedBiologicalEntitiesStudiesByAccession(String accession);
    List<Study> findDistinctAnnotatedBiologicalEntitiesStudiesByAnnotatedProperty(Property property);

    //for PropertyRepository
    List<Property> findDistinctAnnotatedProperties();
    List<String> findAllPropertyTypes();
    List<Property> findDistinctAnnotatedPropertyByAnnotatedPropertyPropertyType(String type);
    List<Property> findDistinctAnnotatedPropertyByAnnotatedPropertyPropertyValue(String value);
    List<Property> findDistinctAnnotatedPropertyByAnnotatedPropertyPropertyTypeAndByAnnotatedPropertyPropertyValue(String type, String value);
}
