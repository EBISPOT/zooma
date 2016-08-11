package uk.ac.ebi.spot.model;

import java.net.URI;
import java.util.Collection;

/**
 * Represents any type of entity (which may be a biological sample, an assay, or something else) which can be named and
 * identified, and is the target of some Zooma annotation
 *
 * @author Simon Jupp
 * @date 07/10/2013 Functional Genomics Group EMBL-EBI
 */
public interface AnnotationTarget {
    /**
     * Returns the type that was assigned to this annotation target entity
     *
     * @return the name of this biological entity
     */
    Collection<URI> getTypes();
}
