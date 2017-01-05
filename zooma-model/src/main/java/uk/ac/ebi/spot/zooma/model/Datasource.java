package uk.ac.ebi.spot.zooma.model;

import java.net.URI;

/**
 * A representation of the "datasource" from which an annotation was obtained.  This will normally be a database or an
 * ontology or some other similar type of resource that contains the sort of annotation-like data that ZOOMA can
 * represent.
 * <p/>
 * Datasource objects should contain a URI to identify the datasource (if the datasource does not have an official URI,
 * this would normally be the homepage of the resource) and typing information.
 *
 * @author Tony Burdett
 * @date 04/10/13
 */
public interface Datasource {
    /**
     * Returns the {@link Datasource.Type type} of this datasource
     *
     * @return the annotation source type
     */
    Type getType();

    /**
     * Returns the short name that was assigned to this datasource
     *
     * @return the shortname for this datasource
     */
    String getName();

    /**
     * Returns the URI of this datasource (which is either an official URI or possibly a project homepage)
     *
     * @return the datasource {@link URI}
     */
    URI getUri();

    /**
     * Returns a string describing the "topic" of this datasource. Topics describe the sort of data this datasource
     * might contain and ideally align to EDAM ontology terms.
     *
     * @return a short descriptive topic string
     */
    String getTopic();

    /**
     * The type that a datasource can take.
     */
    public enum Type {
        DATABASE,
        ONTOLOGY;

        public static Type lookup(String id) {
            for (Type e : Type.values()) {
                if (e.name().equals(id)) {
                    return e;
                }
            }
            return Type.DATABASE;
        }
    }
}
