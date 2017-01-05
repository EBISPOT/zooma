package uk.ac.ebi.spot.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;
import uk.ac.ebi.spot.model.*;
import uk.ac.ebi.spot.repositories.custom.CustomMongoAnnotationRepository;

import java.util.Collection;
import java.util.List;

/**
 * Created by olgavrou on 03/08/2016.
 */
@Repository
@RepositoryRestResource(exported = false)
public interface MongoAnnotationRepository extends MongoRepository<MongoAnnotation, String>, CustomMongoAnnotationRepository {

    List<MongoAnnotation> findByAnnotatedPropertyPropertyValue(@Param("propertyValue") String propertyValue);

    List<MongoAnnotation> findBySemanticTagsIn(@Param("semanticTags") Collection<String> semanticTags);

    List<MongoAnnotation> findByAnnotatedBiologicalEntitiesName(@Param("name") String name);

    List<MongoAnnotation> findByAnnotatedBiologicalEntitiesStudiesAccession(@Param("accession") String accession);

    List<MongoAnnotation> findByAnnotatedBiologicalEntitiesNameAndAnnotatedBiologicalEntitiesStudiesAccession(@Param("name") String name, @Param("accession") String accession);

    //will return either an Untyped or Typed property, depending on what is given as the "annotatedProperty"
    List<MongoAnnotation> findByAnnotatedProperty(@Param("annotatedProperty") Property annotatedProperty);

    List<MongoAnnotation> findByProvenanceSource(@Param("source") AnnotationSource source, Pageable pageable);

    List<MongoAnnotation> findByProvenanceSourceName(@Param("name") String name, Pageable pageable);

}
