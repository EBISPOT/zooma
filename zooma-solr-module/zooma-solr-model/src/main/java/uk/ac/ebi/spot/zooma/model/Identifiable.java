package uk.ac.ebi.spot.zooma.model;

import java.io.Serializable;

/**
 * Represents any entity that can be uniquely identified with a {@link String}.  For identifiable objects, the URI is
 * considered to be immutable: implementation must not supply a public <code>setId()</code> method.
 *
 * @author Tony Burdett
 * @date 13/03/12
 */
public interface Identifiable extends Serializable {
    /**
     * Returns the id of this identifiable object.
     *
     * @return the id of this entity
     */
    String getId();
}
