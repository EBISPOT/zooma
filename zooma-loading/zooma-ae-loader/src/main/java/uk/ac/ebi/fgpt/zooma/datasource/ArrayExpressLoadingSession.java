package uk.ac.ebi.fgpt.zooma.datasource;

import uk.ac.ebi.fgpt.zooma.Namespaces;

import java.net.URI;

/**
 * An annotation loading session that is capable of minting URIs specific to ArrayExpress
 *
 * @author Tony Burdett
 * @date 03/10/12
 */
public abstract class ArrayExpressLoadingSession extends AbstractAnnotationLoadingSession {
    @Override protected URI mintStudyURI(String studyAccession, String studyID) {
        return URI.create(Namespaces.AE_RESOURCE.getURI().toString() + "experiment/" + encode(studyAccession));
    }

    @Override protected URI mintPropertyURI(String propertyID,
                                            String propertyType,
                                            String propertyValue) {
        return URI.create(Namespaces.AE_RESOURCE.getURI().toString() + "property/" + propertyID);
    }

    @Override protected URI mintAnnotationURI(String annotationID) {
        return URI.create(Namespaces.AE_RESOURCE.getURI().toString() + "annotation/" + annotationID);
    }
}
