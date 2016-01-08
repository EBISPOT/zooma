package uk.ac.ebi.fgpt.zooma.datasource;

import uk.ac.ebi.fgpt.zooma.model.AnnotationProvenance;

import java.net.URI;

/**
 * A data access object that defines methods to create, retrieve, update and delete annotation provenance from a ZOOMA
 * datasource.
 *
 * @author Simon Jupp
 * @date 11/11/13
 */
public interface AnnotationProvenanceDAO {
    /**
     * Returns the {@link AnnotationProvenance} object attached to the {@link uk.ac.ebi.fgpt.zooma.model.Annotation}
     * with the given URI.
     *
     * @param uri the URI of the annotation to get provenance for
     * @return the annotation provenance for the annotation with the given URI
     */
    AnnotationProvenance readByAnnotationURI(URI uri);
}
