package uk.ac.ebi.fgpt.zooma.datasource;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

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
        return URI.create(namespace.toString() + "/" + encode(studyAccession));
    }

    @Override protected URI mintBioentityURI(String bioentityID,
                                             String bioentityName, String... studyAccessions) {
        return URI.create(namespace.toString() + "/" + encode(studyAccessions[0]) + "/" + bioentityID);

    }

    @Override
    protected Collection<URI> mintBioentityURITypes(Collection<String> bioentityTypeName) {
        Set<URI> typeUris = new HashSet<URI>();
        for (String name : bioentityTypeName) {
            try {
                typeUris.add(URI.create(this.namespace + URLEncoder.encode(name, "UTF-8")));
            }
            catch (UnsupportedEncodingException e) {
                getLog().error("Couldn't create a URI from bioentity type name: " + name);
            }

        }
        return typeUris;
    }

    @Override protected URI mintAnnotationURI(String annotationID) {
        return URI.create(namespace.toString() + "/" + annotationID);
    }
}
