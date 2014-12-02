package uk.ac.ebi.fgpt.zooma.datasource;

import uk.ac.ebi.fgpt.zooma.Namespaces;

import java.net.URI;

/**
 * An annotation loading session that mints URIs for annotation objects acquired from the Uniprot database
 *
 * @author Tony Burdett
 * @date 04/07/14
 */
public class UniprotHumanDiseaseAnnotationSession extends AbstractAnnotationLoadingSession {
    @Override protected URI mintStudyURI(String studyID) {
        throw new UnsupportedOperationException("Cannot mint study URIs for Uniprot - the current implementation " +
                                                        "does not record study information.");
    }

    @Override protected URI mintBioentityURI(String bioentityID) {
        throw new UnsupportedOperationException("Cannot mint bioentity URIs for Uniprot - the current implementation " +
                                                        "does not record bioentity information.");
    }


    @Override protected URI mintAnnotationURI(String annotationID) {
        return URI.create(Namespaces.ZOOMA_RESOURCE.getURI().toString() + "uniprot/" + annotationID);
    }
}
