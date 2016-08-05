package uk.ac.ebi.spot.model;

import java.io.Serializable;
import java.net.URI;

/**
 * Represents any entity that can be uniquely identified with a {@link URI}.  For identifiable objects, the URI is
 * considered to be immutable: implementation must not supply a public <code>setURI()</code> method.
 *
 * @author Tony Burdett
 * @date 13/03/12
 */
public interface Identifiable extends Serializable {
    /**
     * Returns the uniform resource identifier of this identifiable object.
     *
     * @return the URI of this entity
     */
    URI getURI();
}
