package uk.ac.ebi.spot.model;

import java.net.URI;
import java.util.Collection;

/**
 * Represents a general information source.  In ZOOMA, a TargetSource is any reference to the source of a Zooma
 * AnnotationTarget Typically a source represents a database entry, like and experiment or study.
 *
 * @author Simon Jupp
 * @date 07/10/2013 Functional Genomics Group EMBL-EBI
 */
public interface TargetSource extends Identifiable {
    /**
     * Returns the types that were assigned to this source target
     *
     * @return the set of URIs of types of the target source
     */
    Collection<URI> getTypes();
}
