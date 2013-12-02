package uk.ac.ebi.fgpt.zooma.datasource;

import uk.ac.ebi.fgpt.zooma.Namespaces;

import java.net.URI;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 02/12/13
 */
public class OmimLoadingSession extends AbstractAnnotationLoadingSession {
    @Override protected URI mintStudyURI(String studyAccession, String studyID) {
        throw new UnsupportedOperationException("Cannot mint study URIs for OMIM - the current implementation " +
                                                        "does not record study information.");
    }

    @Override protected URI mintBioentityURI(String bioentityID,
                                             String bioentityName, String... studyAccessions) {
        throw new UnsupportedOperationException("Cannot mint bioentity URIs for OMIM - the current implementation " +
                                                        "does not record bioentity information.");
    }


    @Override protected URI mintAnnotationURI(String annotationID) {
        return URI.create(Namespaces.ZOOMA_RESOURCE.getURI().toString() + "omim/" + annotationID);
    }
}
