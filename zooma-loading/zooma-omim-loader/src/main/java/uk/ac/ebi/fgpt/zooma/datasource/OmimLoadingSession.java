package uk.ac.ebi.fgpt.zooma.datasource;

import java.net.URI;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 02/12/13
 */
public class OmimLoadingSession extends AbstractAnnotationLoadingSession {
    @Override protected URI mintStudyURI(String studyAccession, String studyID) {
        return null;
    }

    @Override protected URI mintBioentityURI(String bioentityID, String bioentityName, String... studyAccessions) {
        return null;
    }

    @Override protected URI mintAnnotationURI(String annotationID) {
        return null;
    }
}
