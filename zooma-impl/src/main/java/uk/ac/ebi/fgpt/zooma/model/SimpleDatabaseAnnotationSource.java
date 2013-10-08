package uk.ac.ebi.fgpt.zooma.model;

import java.net.URI;

/**
 * A basic implementation of an Annotation Source, declaring the source of an annotation as a database
 *
 * @author Tony Burdett
 * @date 04/10/13
 */
public class SimpleDatabaseAnnotationSource extends SimpleAnnotationSource {
    public SimpleDatabaseAnnotationSource(URI source) {
        super(source, Type.DATABASE);
    }
}
