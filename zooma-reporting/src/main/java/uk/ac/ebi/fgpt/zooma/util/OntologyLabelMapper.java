package uk.ac.ebi.fgpt.zooma.util;

import java.net.URI;

/**
 * An interface for mapping between ontology labels and URIs.  It is assumed that a unique pairing of label to URI is
 * possible for each mapper.  Implementations are free to decide how to handle cases where this assumption fails.  One
 * possible approach is to create more than one label mapper per ontology, for example.
 *
 * @author Tony Burdett
 * @date 03/04/13
 */
public interface OntologyLabelMapper {
    /**
     * Lookup the label for the concept in an ontology with the given URI
     *
     * @param uri the URI of the concept
     * @return the rdfs:label of this concept
     */
    String getLabel(URI uri);

    /**
     * Returns the URI for the concept in the ontology with the given label
     *
     * @param label the label to lookup
     * @return the URI of the concept with this label
     */
    URI getURI(String label);
}
