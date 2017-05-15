package uk.ac.ebi.spot.zooma.repository.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.stereotype.Repository;
import uk.ac.ebi.spot.zooma.model.mongo.Annotation;
import uk.ac.ebi.spot.zooma.model.mongo.AnnotationProvenance;

import java.time.LocalDateTime;
import java.util.Collection;

/**
 * Created by olgavrou on 03/08/2016.
 */
@Repository
//@RepositoryRestResource(exported = false)
public interface AnnotationRepository extends MongoRepository<Annotation, String> {

    Annotation findByChecksum(@Param("checksum") String checksum);

    @RestResource(path = "findByAnnotation", rel = "findByAnnotation")
    Annotation findByBiologicalEntitiesBioEntityAndBiologicalEntitiesStudiesStudyAndPropertyPropertyTypeAndPropertyPropertyValueAndSemanticTagAndProvenanceAnnotatorAndProvenanceAnnotatedDateAndProvenanceEvidenceAndProvenanceSourceUri(
            @Param("bioEntity") String bioEntity, @Param("study") String study,
            @Param("propertyType") String propertyType, @Param("propertyValue") String propertyValue,
            @Param("semanticTag") Collection<String> semanticTag,
            @Param("annotator") String annotator,
            @Param("annotatedDate") LocalDateTime annotatedDate,
            @Param("evidence") AnnotationProvenance.Evidence evidence,
            @Param("sourceUri") String sourceUri
    );

    @Override
    @RestResource(exported = false)
    void delete(String s);

    @Override
    @RestResource(exported = false)
    void delete(Annotation entity);

    @Override
    @RestResource(exported = false)
    void delete(Iterable<? extends Annotation> entities);

    @Override
    @RestResource(exported = false)
    void deleteAll();


}
