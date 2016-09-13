package uk.ac.ebi.spot.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.ac.ebi.spot.model.*;

import java.net.URI;
import java.util.Collection;
import java.util.List;

/**
 * Created by olgavrou on 03/08/2016.
 */
@Repository
public interface AnnotationRepository extends MongoRepository<SimpleAnnotation, String> {

    List<SimpleAnnotation> findByAnnotatedPropertyPropertyValue(@Param("propertyValue") String propertyValue);

    List<SimpleAnnotation> findBySemanticTagsIn(@Param("semanticTags") Collection<URI> semanticTags);

    List<SimpleAnnotation> findByAnnotatedBiologicalEntitiesName(@Param("name") String name);

    List<SimpleAnnotation> findByAnnotatedBiologicalEntitiesStudiesAccession(@Param("accession") String accession);

    List<SimpleAnnotation> findByAnnotatedBiologicalEntitiesNameAndAnnotatedBiologicalEntitiesStudiesAccession(@Param("name") String name, @Param("accession") String accession);

    //will return either an Untyped or Typed property, depending on what is given as the "annotatedProperty"
    List<SimpleAnnotation> findByAnnotatedProperty(@Param("annotatedProperty") Property annotatedProperty);

    List<SimpleAnnotation> findByProvenanceSource(@Param("source") AnnotationSource source, Pageable pageable);

    List<SimpleAnnotation> findByProvenanceSourceName(@Param("name") String name, Pageable pageable);

}
