package uk.ac.ebi.fgpt.zooma.datasource;

import java.net.URI;

/**
 * An annotation loading session that is capable of minting generate URIs specific to some data contained in a CSV file
 *
 * @author Tony Burdett
 * @date 23/10/12
 */
public class CSVLoadingSession extends AbstractAnnotationLoadingSession {
    private final URI namespace;

    /**
     * Takes a string representing the namespace URI of this datasource
     *
     * @param namespace the namespace to use as the base URI of entities created by this loading session
     */
    public CSVLoadingSession(String namespace) {
        this.namespace = URI.create(namespace);
    }

    @Override protected URI mintStudyURI(String studyAccession, String studyID) {
        return URI.create(namespace.toString() + "study/" + encode(studyAccession));
    }

    @Override protected URI mintBioentityURI(String bioentityID,
                                             String bioentityName, String... studyAccessions) {
        return URI.create(namespace.toString() + "study/" + encode(studyAccessions[0]) + "/bioentity/" + bioentityID);

    }

    @Override protected URI mintPropertyURI(String propertyID,
                                            String propertyType,
                                            String propertyValue) {
        return URI.create(namespace.toString() + "property/" + propertyID);
    }

    @Override protected URI mintAnnotationURI(String annotationID) {
        return URI.create(namespace.toString() + "annotation/" + annotationID);
    }
}
