package uk.ac.ebi.fgpt.zooma.model;

import java.net.URI;

/**
 * An abstract {@link Identifiable} implementation that requires a URI.  The URI must be supplied in the constructor and
 * is used to test for equality between identifiable objects (in other words, two AbstractIdentifiables with the same
 * URI are considered to be equal.
 *
 * @author Tony Burdett
 * @date 10/04/12
 */
public class AbstractIdentifiable implements Identifiable {
    private static final long serialVersionUID = 1805438131697808302L;

    private final URI uri;

    public AbstractIdentifiable(URI uri) {
        this.uri = uri;
    }

    @Override public URI getURI() {
        return uri;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AbstractIdentifiable that = (AbstractIdentifiable) o;
        return uri != null && uri.equals(that.uri);
    }

    @Override
    public int hashCode() {
        return uri != null ? uri.hashCode() : 0;
    }
}
