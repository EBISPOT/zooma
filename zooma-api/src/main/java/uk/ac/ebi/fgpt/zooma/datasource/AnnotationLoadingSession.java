package uk.ac.ebi.fgpt.zooma.datasource;

import uk.ac.ebi.fgpt.zooma.model.Annotation;
import uk.ac.ebi.fgpt.zooma.model.AnnotationProvenance;
import uk.ac.ebi.fgpt.zooma.model.BiologicalEntity;
import uk.ac.ebi.fgpt.zooma.model.Property;
import uk.ac.ebi.fgpt.zooma.model.Study;

import java.net.URI;
import java.util.Collection;

/**
 * A session that should be opened when you begin loading annotations from a source.  Each session should retain a cache
 * of previously encountered studies, biological entities, properties and provenances to avoid unnecessary duplication.
 *
 * @author Tony Burdett
 * @author Simon Jupp
 * @date 28/09/12
 */
public interface AnnotationLoadingSession {
    Study getOrCreateStudy(String studyAccession, Collection<URI> studyTypes);

    Study getOrCreateStudy(String studyAccession, String studyID, Collection<URI> studyTypes);

//    Study getOrCreateStudy(String studyAccession, URI studyURI);

    Study getOrCreateStudy(String studyAccession, URI studyURI, Collection<URI> studyTypes);

    /**
     * A method to create a biological entity object based on the bioentity name, types and set of studies
     *
     * @param bioentityName       A name to identity the biological entity
     * @param bioentityTypesNames An optional collection of names to represent the type of biological entity
     * @param bioentityTypesURIs  An optional collection of URIs to represent the type of biological entity
     * @param studies             A collection of studies linked to the biological entity
     * @return BiologicalEntity
     */
    BiologicalEntity getOrCreateBiologicalEntity(String bioentityName,
                                                 Collection<String> bioentityTypesNames,
                                                 Collection<URI> bioentityTypesURIs,
                                                 Study... studies);

    /**
     * A method to create a biological entity object based on the bioentity name, types and set of studies
     *
     * @param bioentityName       A name to identity the biological entity
     * @param bioentityID         An id that can be used to form the URI of the biological entity
     * @param bioentityTypesNames An optional collection of names to represent the type of biological entity
     * @param bioentityTypesURIs  An optional collection of URIs to represent the type of biological entity
     * @param studies             A collection of studies linked to the biological entity
     * @return BiologicalEntity
     */
    BiologicalEntity getOrCreateBiologicalEntity(String bioentityName,
                                                 String bioentityID,
                                                 Collection<String> bioentityTypesNames,
                                                 Collection<URI> bioentityTypesURIs,
                                                 Study... studies);

    /**
     * A method to create a biological entity object based on the bioentity name, types and set of studies
     *
     * @param bioentityName       A name to identity the biological entity
     * @param bioentityURI        A URI that will be used to identify the biological entity
     * @param bioentityTypesNames An optional collection of names to represent the type of biological entity
     * @param bioentityTypesURIs  An optional collection of URIs to represent the type of biological entity
     * @param studies             A collection of studies linked to the biological entity
     * @return BiologicalEntity
     */
    BiologicalEntity getOrCreateBiologicalEntity(String bioentityName,
                                                 URI bioentityURI,
                                                 Collection<String> bioentityTypesNames,
                                                 Collection<URI> bioentityTypesURIs,
                                                 Study... studies);

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
