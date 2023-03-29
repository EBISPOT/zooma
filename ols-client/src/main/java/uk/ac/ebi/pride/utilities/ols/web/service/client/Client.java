package uk.ac.ebi.pride.utilities.ols.web.service.client;

import org.springframework.web.client.RestClientException;
import uk.ac.ebi.pride.utilities.ols.web.service.model.Ontology;
import uk.ac.ebi.pride.utilities.ols.web.service.model.Term;

import java.util.List;

/**
 * @author Yasset Perez-Riverol (ypriverol@gmail.com)
 * Cration date 01/03/2016
 */
public interface Client {
    /**
     * This function retrieve the list of Ontologies in the OLS. Each ontology contain the summary description
     * about the ontology including name, IDs, etc.
     * @return List of ontologies.
     */
    // Zooma
    List<Ontology> getOntologies() throws RestClientException;


    /**
     * This function retrieve all the terms that contains in the name the partialName.
     * @param partialName Substring to lookup in the name term
     * @param ontologyId Ontology term
     * @param reverseKeyOrder sort the hash in a reverse order
     * @return list of terms.
     */
    // Zooma
    List<Term> getTermsByName(String partialName, String ontologyId, boolean reverseKeyOrder) throws RestClientException;

    /**
     * Retrieve an specific ontology information for an ID
     * @param ontologyId ontology Identifier
     * @return Ontology result.
     * @throws RestClientException if there are problems connecting to the REST service.
     */
    // Zooma
    Ontology getOntology(String ontologyId) throws RestClientException;
}
