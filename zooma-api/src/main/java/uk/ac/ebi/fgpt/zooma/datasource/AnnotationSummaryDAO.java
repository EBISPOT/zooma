package uk.ac.ebi.fgpt.zooma.datasource;

import uk.ac.ebi.fgpt.zooma.model.*;

import java.net.URI;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * A data access object that defines methods to create, retrieve, update and delete annotation summaries from a ZOOMA
 * datasource.
 *
 * @author Simon Jupp
 * @date 08/11/13
 */
public interface AnnotationSummaryDAO extends ZoomaDAO<AnnotationSummary> {

//    /**
//     * Retrieves a map of unique combinations of Property to Semantic tag mappings
//     * @return the map of property to semantic tag mappings
//     */
//    Map<URI, Collection<URI>> readDistinctPropertyToSemanticTag();
//
//    /**
//     * Retrieves a collection of all annotations for a property and semantic tag pair
//     * @return the map of annotations
//     */
//    Collection<Annotation> readAnnotationsByPropertyToSemanticTag(URI propertyUri, Collection<URI> semanticTag);

}
