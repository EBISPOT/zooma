package uk.ac.ebi.fgpt.zooma.datasource;

import uk.ac.ebi.fgpt.zooma.Namespaces;

import java.net.URI;

/**
 * An OMIA annotation loading session that can generate URIs specific to annotations from OMIA.
 *
 * @author Tony Burdett
 * @date 26/07/13
 */
public class OmiaLoadingSession extends AbstractAnnotationLoadingSession {
    @Override protected URI mintStudyURI(String studyAccession, String studyID) {
        return URI.create(Namespaces.PUBMED.getURI().toString() + studyAccession);
    }

    @Override protected URI mintBioentityURI(String bioentityID,
                                             String bioentityName, String... studyAccessions) {
        return URI.create(Namespaces.OMIA_RESOURCE.getURI().toString() + bioentityID);
    }

    @Override protected URI mintPropertyURI(String propertyID,
                                            String propertyType,
                                            String propertyValue) {
        return URI.create(Namespaces.OMIA_PROPERTY.getURI().toString() + propertyID);
    }

    @Override protected URI mintAnnotationURI(String annotationID) {
        return URI.create(Namespaces.OMIA_ANNOTATION.getURI().toString() + annotationID);
    }
}
