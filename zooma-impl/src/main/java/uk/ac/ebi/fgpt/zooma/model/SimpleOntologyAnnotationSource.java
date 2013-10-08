package uk.ac.ebi.fgpt.zooma.model;

import java.net.URI;

/**
 * A basic implementation of an Annotation Source, declaring the source of an annotation as an ontology
 *
 * @author Tony Burdett
 * @date 04/10/13
 */
public class SimpleOntologyAnnotationSource extends SimpleAnnotationSource {
    public SimpleOntologyAnnotationSource(URI source) {
        super(source, Type.ONTOLOGY);
    }
}
