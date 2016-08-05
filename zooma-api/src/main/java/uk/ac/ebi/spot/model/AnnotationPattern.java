package uk.ac.ebi.spot.model;

import java.net.URI;
import java.util.Collection;

/**
 * This interface defines a basic annotation pattern consisting of a property type, property value, semantic tags and a
 * datasource.
 *
 * @author Simon Jupp
 * @date 28/01/2014 Functional Genomics Group EMBL-EBI
 */
public interface AnnotationPattern extends Identifiable {

    URI getPropertyURI();

    String getPropertyType();

    String getPropertyValue();

    Collection<URI> getSemanticTags();

    AnnotationSource getAnnotationSource();

    boolean isReplaced();

}