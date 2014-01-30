package uk.ac.ebi.fgpt.zooma.datasource;

import uk.ac.ebi.fgpt.zooma.model.AnnotationPattern;
import uk.ac.ebi.fgpt.zooma.model.Property;

import java.net.URI;
import java.util.Collection;

/**
 * @author Simon Jupp
 * @date 28/01/2014
 * Functional Genomics Group EMBL-EBI
 */
public interface AnnotationPatternDAO extends ZoomaDAO<AnnotationPattern> {

    /**
     * Search patterns given a property value
     * @param property property must have a URI
     * @return
     */
    Collection<AnnotationPattern> readByProperty(Property property);


    /**
     * Wildcard search annotation patterns by an optional type.
     * Type is matched exactly
     * @param type optional property type (exact match)
     * @param value wildcard search of property types
     * @return
     */
    Collection<AnnotationPattern> matchByProperty(String type, String value);

    /**
     * Wildcard search annotation patterns by property value
     * @param value
     * @return
     */
    Collection<AnnotationPattern> matchByProperty(String value);

    /**
     * Wildcard search annotation patterns by property value
     * @param semanticTagURI
     * @return
     */
    Collection<AnnotationPattern> matchBySematicTag(URI semanticTagURI);
}
