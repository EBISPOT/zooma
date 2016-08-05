package service;

import java.net.URI;
import java.util.Collection;
import java.util.Set;

/**
 * A ZOOMA service that offers ways to retrieve information about ontology classes (or semantic tags) that are known to
 * ZOOMA.
 *
 * @author Tony Burdett
 * @date 13/09/12
 */
public interface OntologyService {
    /**
     * Retrieves the rdfs:label, if present, of the given semantic tag
     *
     * @param semanticTagShortname the entity to fetch the label for
     * @return the rdfs:label of this entity, or an empty string if there is no label
     */
    String getLabel(String semanticTagShortname);

    /**
     * Retrieves the rdfs:label, if present, of the given semantic tag
     *
     * @param semanticTag the entity to fetch the label for
     * @return the rdfs:label of this entity, or an empty string if there is no label
     */
    String getLabel(URI semanticTag);

    /**
     * Retrieves the synonyms (using common synonym annotations, if present), of the given semantic tag
     *
     * @param semanticTag the entity to fetch the label for
     * @return the rdfs:label of this entity, or an empty string if there is no label
     */
    Set<String> getSynonyms(URI semanticTag);

    /**
     * Get subclasses of given URI
     *
     * @param semanticTag the entity to fetch the subclasses for
     * @param infer set to true to infer all descendant terms
     */

    Set<String> getChildren(URI semanticTag, boolean infer);
}