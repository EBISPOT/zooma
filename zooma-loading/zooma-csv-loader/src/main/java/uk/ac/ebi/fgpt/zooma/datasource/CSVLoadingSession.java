package uk.ac.ebi.fgpt.zooma.datasource;

import uk.ac.ebi.fgpt.zooma.model.AnnotationProvenance;
import uk.ac.ebi.fgpt.zooma.model.SimpleDatabaseAnnotationSource;
import uk.ac.ebi.fgpt.zooma.util.AnnotationProvenanceBuilder;

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
        super();
        setAnnotationProvenanceTemplate(
                AnnotationProvenanceBuilder
                        .createTemplate(uri.toString(), new Date())
                        .sourceIs(new SimpleDatabaseAnnotationSource(uri, name))
                        .evidenceIs(AnnotationProvenance.Evidence.MANUAL_CURATED));
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
        super();
        setAnnotationProvenanceTemplate(
                AnnotationProvenanceBuilder
                        .createTemplate(uri.toString(), new Date())
                        .sourceIs(new SimpleDatabaseAnnotationSource(uri, name))
                        .evidenceIs(AnnotationProvenance.Evidence.MANUAL_CURATED)
                        .accuracyIs(AnnotationProvenance.Accuracy.NOT_SPECIFIED)
                        .annotatorIs(annotationCreator));
    }
}
