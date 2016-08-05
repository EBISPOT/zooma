package uk.ac.ebi.spot.service;

import uk.ac.ebi.spot.model.BiologicalEntity;

import java.net.URI;
import java.util.Collection;

/**
 * A ZOOMA service that allows searching over the set of {@link BiologicalEntity}s known to ZOOMA.  Although most
 * biological entity based operations will use the {@link uk.ac.ebi.spot.service.BiologicalEntityService} to do direct retrieval operations,
 * this search service provides a facility to collect similar biological entities based on annotations they are involved
 * in.
 *
 * @author Tony Burdett
 * @date 12/06/12
 */
public interface BiologicalEntitySearchService {
    /**
     * Search the set of biological entities in ZOOMA for those with annotations that closely match the set of supplied
     * semantic tags.
     *
     * @param semanticTagShortnames the set of semantic tags that all results should annotate to
     * @return a collection of studies that annotate to all of the supplied entities
     */
    Collection<BiologicalEntity> searchBySemanticTags(String... semanticTagShortnames);

    /**
     * Search the set of biological entities in ZOOMA for those with annotations that closely match the set of supplied
     * semantic tags.
     *
     * @param semanticTags the set of semantic tags that all results should annotate to
     * @return a collection of studies that annotate to all of the supplied entities
     */
    Collection<BiologicalEntity> searchBySemanticTags(URI... semanticTags);

    /**
     * Search the set of biological entities in ZOOMA for those with annotations that closely match the set of supplied
     * semantic tags.
     * <p/>
     * This form of the method has a flag to specify whether or not inference should be used on the set of semantic
     * tags: if true, results will annotate to all of the supplied semantic tags or any of their subtypes
     *
     * @param useInference          whether or not to use inference on the semantic tags
     * @param semanticTagShortnames the set of semantic tags that all results should annotate to
     * @return a collection of studies that annotate to all of the supplied entities
     */
    Collection<BiologicalEntity> searchBySemanticTags(boolean useInference, String... semanticTagShortnames);

    /**
     * Search the set of biological entities in ZOOMA for those with annotations that closely match the set of supplied
     * semantic tags.
     * <p/>
     * This form of the method has a flag to specify whether or not inference should be used on the set of semantic
     * tags: if true, results will annotate to all of the supplied semantic tags or any of their subtypes
     *
     * @param useInference whether or not to use inference on the semantic tags
     * @param semanticTags the set of semantic tags that all results should annotate to
     * @return a collection of studies that annotate to all of the supplied entities
     */
    Collection<BiologicalEntity> searchBySemanticTags(boolean useInference, URI... semanticTags);
}