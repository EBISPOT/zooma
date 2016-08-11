package uk.ac.ebi.spot.model;

/**
 * A representation of the "source" from which an annotation was obtained.  This will normally be a database or an
 * ontology or some other similar type of resource that contains the sort of annotation type data that ZOOMA can
 * represent.
 * <p/>
 * Annotation source objects contain a URI to identify the datasource (if the datasource does not have an official URI,
 * this would normally be the homepage of the resource) and typing information.
 *
 * @author Tony Burdett
 * @date 04/10/13
 */
public interface AnnotationSource {
    /**
     * Returns the type of this annotation source
     *
     * @return the annotation source type
     */
    Type getType();

    /**
     * Returns the short name that was assigned to this source target
     *
     * @return the shortname for this annotation source
     */
    String getName();

    /**
     * The type that an annotation sources can take.
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
