package uk.ac.ebi.fgpt.zooma.owl;

import org.semanticweb.owlapi.model.IRI;

import java.util.Map;
import java.util.Set;

/**
 * An ontology loader that providex some abstraction around core concepts in ontologies loaded via the OWLAPI.  This
 * interface provides a mechanism to decouple ontology loading and processing from the activity of generating ZOOMA
 * annotations.  This allows for a variety of ontology loading strategies and implementations, as long as you can
 * extract text to class IRI mappings.
 *
 * @author Tony Burdett
 * @date 03/06/13
 */
public interface OntologyLoader {
    /**
     * Get the ontology IRI.  This returns the IRI of the ontology that was actually loaded, and may be different from
     * the ontologyURI specified if declared differently in the loaded file.
     *
     * @return IRI of the ontology
     */
    IRI getOntologyIRI();

    /**
     * Get the ontology name.  This is a short name for the ontology, for example "efo" for the experimental factor
     * ontology
     *
     * @return IRI of the ontology
     */
    String getOntologyName();

    /**
     * Returns a mapping between the IRIs that identify classes in the loaded ontology and the corresponding class
     * rdfs:label.
     *
     * @return the class labels in this ontology, indexed by class IRI
     */
    Map<IRI, String> getOntologyClassLabels();

    /**
     * Returns a mapping between the IRIs that identify classes in the loaded ontology and the rdfs:label of each of
     * their asserted parent classes.
     *
     * @return the class type labels in this ontology, indexed by class IRI
     */
    Map<IRI, Set<String>> getOntologyClassTypeLabels();

    /**
     * Returns a mapping between the IRIs that identify classes in the loaded ontology and the corresponding class
     * synonym.  Synonyms are specified by the synonymURI property.
     *
     * @return the class labels in this ontology, indexed by class IRI
     */
    Map<IRI, Set<String>> getOntologyClassSynonyms();
}
