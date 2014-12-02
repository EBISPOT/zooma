package uk.ac.ebi.fgpt.zooma.datasource;

import uk.ac.ebi.fgpt.zooma.Namespaces;
import uk.ac.ebi.fgpt.zooma.model.AnnotationProvenance;
import uk.ac.ebi.fgpt.zooma.model.SimpleAnnotationProvenanceTemplate;
import uk.ac.ebi.fgpt.zooma.model.SimpleDatabaseAnnotationSource;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Date;

/**
 * An annotation loading session that is capable of minting generate URIs specific to some data contained in a CSV file
 *
 * @author Tony Burdett
 * @date 23/10/12
 */
public class CSVLoadingSession extends AbstractAnnotationLoadingSession {
    /**
     * Takes a string representing the namespace URI of this datasource and a short name for the datasource
     *
     * @param namespace    the URI of this datasource
     * @param resourceName the shortname for this resource
     */
    public CSVLoadingSession(URI namespace, String resourceName) {
        this(namespace, resourceName, resourceName, null, null);
    }

    public CSVLoadingSession(URI namespace, String resourceName, String annotationCreator) {
        this(namespace, resourceName, annotationCreator, null, null);
    }

    /**
     * Takes a string representing the namespace URI of this datasource and a short name for the datasource
     *
     * @param resourceName               the shortname for this resource
     * @param defaultBiologicalEntityUri the URI representing the type of biological entities stored in this datasource
     * @param defaultStudyEntityUri      the URI representing the type of studies stored in this datasource
     */
    public CSVLoadingSession(String resourceName, URI defaultBiologicalEntityUri, URI defaultStudyEntityUri)
            throws UnsupportedEncodingException {
        this(URI.create(
                     Namespaces.ZOOMA_RESOURCE.getURI().toString() +
                             URLEncoder.encode(resourceName.trim(), "UTF-8")),
             resourceName,
             resourceName,
             defaultBiologicalEntityUri,
             defaultStudyEntityUri);
    }

    /**
     * Takes a string representing the namespace URI of this datasource and a short name for the datasource
     *
     * @param namespace                  the namespace to use as the base URI of entities created by this loading
     *                                   session (can be null)
     * @param resourceName               the shortname for this resource
     * @param defaultBiologicalEntityUri the shortname for this resource
     * @param defaultStudyEntityUri      the shortname for this resource
     */
    public CSVLoadingSession(URI namespace,
                             String resourceName,
                             String annotationCreator,
                             URI defaultBiologicalEntityUri,
                             URI defaultStudyEntityUri) {
        super(new SimpleAnnotationProvenanceTemplate(
                      new SimpleDatabaseAnnotationSource(namespace, resourceName),
                      AnnotationProvenance.Evidence.MANUAL_CURATED,
                      AnnotationProvenance.Accuracy.NOT_SPECIFIED,
                      annotationCreator,
                      new Date(),
                      null,
                      null),
              defaultBiologicalEntityUri,
              defaultStudyEntityUri);
    }
}
