package uk.ac.ebi.fgpt.zooma.datasource;

import java.net.URI;
import java.util.Set;

/**
 * A data access object that defines methods to retrieve information about ontology classes in ZOOMA
 *
 * @author Tony Burdett
 * @date 13/09/12
 */
public interface OntologyDAO {
    /**
     * Retrieves the rdfs:label annotation on the ontology class with the given URI, if available, or an empty string if
     * there is no such label.
     *
     * @param semanticTagURI the URI of the ontology class to retrieve the label for
     * @return the rdfs:label
     */
    String getSemanticTagLabel(URI semanticTagURI);

    /**
     * Retrieves annotations that represent synonyms on the ontology class with the given URI.  Unlike labels, there is
     * no standard implementation in rdfs to describe synonyms so different implementations may use different annotation
     * types for this method.  Usually, this will delegate to {@link #getSemanticTagSynonyms(java.net.URI,
     * java.net.URI)} supplying a sensible default.
     *
     * @param semanticTagURI the URI of the ontology class to retrieve the synonym for
     * @return a set of synonyms
     */
    Set<String> getSemanticTagSynonyms(URI semanticTagURI);

    /**
     * Retrieves annotations that represent synonyms on the ontology class with the given URI.  The way synonyms are
     * annotated in the ontology is specified by the first argument to this method.
     *
     * @param semanticTagURI the URI of the ontology class to retrieve the synonym for
     * @return a set of synonyms
     */
    Set<String> getSemanticTagSynonyms(URI synonymTypeURI, URI semanticTagURI);
}
