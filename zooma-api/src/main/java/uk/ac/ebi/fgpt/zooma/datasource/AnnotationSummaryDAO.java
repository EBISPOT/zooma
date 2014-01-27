package uk.ac.ebi.fgpt.zooma.datasource;

import uk.ac.ebi.fgpt.zooma.model.AnnotationSummary;
import uk.ac.ebi.fgpt.zooma.model.Property;

import java.util.Collection;

/**
 * A data access object that defines methods to create, retrieve, update and delete annotation summaries from a ZOOMA
 * datasource.
 *
 * @author Simon Jupp
 * @date 08/11/13
 */
public interface AnnotationSummaryDAO extends ZoomaDAO<AnnotationSummary> {

    /**
     * Search summaries given a property value
     * @param property property must have a URI
     * @return
     */
    Collection<AnnotationSummary> readByProperty(Property property);


    /**
     * Wildcard search annotation summaries by an optional type.
     * Type is matched exactly
     * @param type optional property type (exact match)
     * @param value wildcard search of property types
     * @return
     */
    Collection<AnnotationSummary> matchByProperty(String type, String value);

    /**
     * Wildcard search annotation summaries by property value
     * @param value
     * @return
     */
    Collection<AnnotationSummary> matchByProperty(String value);

}
