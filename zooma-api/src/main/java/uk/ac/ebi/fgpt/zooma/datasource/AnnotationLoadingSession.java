package uk.ac.ebi.fgpt.zooma.datasource;

import uk.ac.ebi.fgpt.zooma.model.Annotation;
import uk.ac.ebi.fgpt.zooma.model.AnnotationProvenance;
import uk.ac.ebi.fgpt.zooma.model.BiologicalEntity;
import uk.ac.ebi.fgpt.zooma.model.Property;
import uk.ac.ebi.fgpt.zooma.model.Study;

import java.net.URI;

/**
 * A session that should be opened when you begin loading annotations from a source.  Each session should retain a cache
 * of previously encountered studies, biological entities, properties and provenances to avoid unnecessary duplication.
 *
 * @author Tony Burdett
 * @date 28/09/12
 */
public interface AnnotationLoadingSession {
    Study getOrCreateStudy(String studyAccession);

    Study getOrCreateStudy(String studyAccession, String studyID);

    Study getOrCreateStudy(String studyAccession, URI studyURI);

    BiologicalEntity getOrCreateBiologicalEntity(String bioentityName, Study... studies);

    BiologicalEntity getOrCreateBiologicalEntity(String bioentityName,
                                                 String bioentityID, Study... studies);

    BiologicalEntity getOrCreateBiologicalEntity(String bioentityName,
                                                 URI bioentityURI, Study... studies);

    Property getOrCreateProperty(String propertyType, String propertyValue);

    Property getOrCreateProperty(String propertyType, String propertyValue, String propertyID);

    Property getOrCreateProperty(String propertyType, String propertyValue, URI propertyURI);

    Annotation getOrCreateAnnotation(Property p,
                                     AnnotationProvenance ap, URI semanticTag, BiologicalEntity... bioentities);

    Annotation getOrCreateAnnotation(Property property,
                                     AnnotationProvenance annotationProvenance,
                                     URI semanticTag, URI annotationURI, BiologicalEntity... bioentities);

    Annotation getOrCreateAnnotation(Property p,
                                     AnnotationProvenance ap,
                                     URI semanticTag,
                                     String annotationID, BiologicalEntity... bioentities);

    void clearCaches();
}
