package uk.ac.ebi.fgpt.zooma.datasource;

import uk.ac.ebi.fgpt.zooma.model.AnnotationProvenance;
import uk.ac.ebi.fgpt.zooma.model.SimpleAnnotationProvenanceTemplate;
import uk.ac.ebi.fgpt.zooma.model.SimpleDatabaseAnnotationSource;

import java.net.URI;
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
     * @param uri  the URI of this datasource
     * @param name the shortname for this resource
     */
    public CSVLoadingSession(URI uri, String name) {
        super(new SimpleAnnotationProvenanceTemplate(
                      new SimpleDatabaseAnnotationSource(uri, name),
                      AnnotationProvenance.Evidence.MANUAL_CURATED,
                      uri.toString(),
                      new Date()),
              null,
              null);
    }

    /**
     * Takes a string representing the namespace URI of this datasource and a short name for the datasource
     *
     * @param uri               the URI of this datasource
     * @param name              the shortname for this resource
     * @param annotationCreator the creator of all annotations in this resource.  Can be overridden when creating
     *                          annotations
     */
    public CSVLoadingSession(URI uri, String name, String annotationCreator) {
        super(new SimpleAnnotationProvenanceTemplate(
                      new SimpleDatabaseAnnotationSource(uri, name),
                      AnnotationProvenance.Evidence.MANUAL_CURATED,
                      AnnotationProvenance.Accuracy.NOT_SPECIFIED,
                      uri.toString(),
                      new Date(),
                      annotationCreator,
                      null),
              null,
              null);
    }

    /**
     * Takes a string representing the namespace URI of this datasource and a short name for the datasource
     *
     * @param uri                        the namespace to use as the base URI of entities created by this loading
     *                                   session (can be null)
     * @param name                       the shortname for this resource
     * @param defaultBiologicalEntityUri the shortname for this resource
     * @param defaultStudyEntityUri      the shortname for this resource
     */
    public CSVLoadingSession(URI uri,
                             String name,
                             URI defaultBiologicalEntityUri,
                             URI defaultStudyEntityUri) {
        super(new SimpleAnnotationProvenanceTemplate(
                      new SimpleDatabaseAnnotationSource(uri, name),
                      AnnotationProvenance.Evidence.MANUAL_CURATED,
                      uri.toString(),
                      new Date()),
              defaultBiologicalEntityUri,
              defaultStudyEntityUri);
    }

    /**
     * Takes a string representing the namespace URI of this datasource and a short name for the datasource
     *
     * @param uri                        the namespace to use as the base URI of entities created by this loading
     *                                   session (can be null)
     * @param name                       the shortname for this resource
     * @param defaultBiologicalEntityUri the shortname for this resource
     * @param defaultStudyEntityUri      the shortname for this resource
     */
    public CSVLoadingSession(URI uri,
                             String name,
                             String annotationCreator,
                             URI defaultBiologicalEntityUri,
                             URI defaultStudyEntityUri) {
        super(new SimpleAnnotationProvenanceTemplate(
                      new SimpleDatabaseAnnotationSource(uri, name),
                      AnnotationProvenance.Evidence.MANUAL_CURATED,
                      AnnotationProvenance.Accuracy.NOT_SPECIFIED,
                      uri.toString(),
                      new Date(),
                      annotationCreator,
                      null),
              defaultBiologicalEntityUri,
              defaultStudyEntityUri);
    }
}
