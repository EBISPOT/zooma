package uk.ac.ebi.fgpt.zooma.datasource;

import uk.ac.ebi.fgpt.zooma.Namespaces;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * An annotation loading session that is capable of minting generate URIs specific to some data contained in a CSV file
 *
 * @author Tony Burdett
 * @date 23/10/12
 */
public class CSVLoadingSession extends AbstractAnnotationLoadingSession {

    private URI namespace;
    private String resourceName;


    /**
     * Takes a short name for the datasource
     * @param resourceName the shortname for this resource
     */
    public CSVLoadingSession (String  resourceName) {
        this(null, resourceName);
    }

    /**
     * Takes a string representing the namespace URI of this datasource and a short name for the datasource
     *
     * @param namespace the namespace to use as the base URI of entities created by this loading session
     * @param resourceName the shortname for this resource
     */
    public CSVLoadingSession(String namespace, String resourceName) {
        this(namespace, resourceName, null, null);
    }

    /**
     * Takes a string representing the namespace URI of this datasource and a short name for the datasource
     *
     * @param resourceName the shortname for this resource
     * @param defaultBiologicalEntityUri the shortname for this resource
     * @param defaultStudyEntityUri the shortname for this resource
     */
    public CSVLoadingSession(String resourceName, URI defaultBiologicalEntityUri, URI defaultStudyEntityUri) {
        this(null, resourceName, defaultBiologicalEntityUri, defaultStudyEntityUri);
    }

    /**
     * Takes a string representing the namespace URI of this datasource and a short name for the datasource
     *
     * @param namespace the namespace to use as the base URI of entities created by this loading session (can be null)
     * @param resourceName the shortname for this resource
     * @param defaultBiologicalEntityUri the shortname for this resource
     * @param defaultStudyEntityUri the shortname for this resource
     */
    public CSVLoadingSession(String namespace, String resourceName, URI defaultBiologicalEntityUri, URI defaultStudyEntityUri) {
        super(defaultBiologicalEntityUri, defaultStudyEntityUri);
        if (namespace == null) {
            try {
                this.namespace = URI.create(Namespaces.ZOOMA_RESOURCE.getURI().toString() + URLEncoder.encode(resourceName.trim(), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                getLog().warn("Couldn't create namespace URI for " + resourceName);
            }
        }
        else {
            this.namespace = URI.create(namespace);
        }
        this.resourceName = resourceName;
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

    public URI getNamespace() {
        return namespace;
    }

    public String getResourceName() {
        return resourceName;
    }
}
