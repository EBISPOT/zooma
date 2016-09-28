package uk.ac.ebi.pride.utilities.ols.web.service.client;

import org.springframework.web.client.RestClientException;
import uk.ac.ebi.pride.utilities.ols.web.service.model.Identifier;
import uk.ac.ebi.pride.utilities.ols.web.service.model.Ontology;
import uk.ac.ebi.pride.utilities.ols.web.service.model.Term;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Yasset Perez-Riverol (ypriverol@gmail.com)
 * Cration date 01/03/2016
 */
public interface Client {

    /**
     * This function retrieve the term by the accession of the term in the ontology and the id of the ontology
     * if the term is not found it, the NULL is returned.
     *
     * @param termId Term ID in the ontology
     * @param ontologyId The ontology name
     * @return return the name of the Ontology term
     */
    Term getTermById(Identifier termId, String ontologyId) throws RestClientException;

    /**
     * This function retrieve the metadata for an specific term, specially the description
     * if the term is not present in the ontology the NULL value is retrieved. The metadata contains for example
     * the definition and the comment related with the Term.
     *
     * @param termId Term ID in the ontology
     * @param ontologyId The ontology name
     * @return return the metadata (description) of the Ontology term
     */
    List<String> getTermDescription(Identifier termId, String ontologyId) throws RestClientException;

    /**
     * The geTXRefTerm in the ontology is used to retrieve this value
     * @param termId Term ID in the ontology
     * @param ontologyId The ontology name
     * @return a HashMap with all the references to the specific Term.
     */
    Map<String, List<String>> getAnnotations(Identifier termId, String ontologyId) throws RestClientException;

    /**
     * This function retrieve the list of Ontologies in the OLS. Each ontology contain the summary description
     * about the ontology including name, IDs, etc.
     * @return List of ontologies.
     */
    List<Ontology> getOntologies() throws RestClientException;

    /**
     * Retrieve all the terms in an specific ontology. The Map contains in the key the term identifier and in the value
     * contains the Term. The ontology graph can be reproduce by taking the information in every term and the list of children terms.
     * @param ontologyId The ontology ID.
     * @return map of the terms
     */
    List<Term> getAllTermsFromOntology(String ontologyId) throws RestClientException;

    /**
     * This function use an ontology name to retrieve only the the root terms for the ontology
     * @param ontologyId The ontology name
     * @return Map of the terms
     */
    List<Term> getRootTerms(String ontologyId) throws RestClientException;

    /**
     * This function retrieve all the terms that contains in the name the partialName.
     * @param partialName Substring to lookup in the name term
     * @param ontologyId Ontology term
     * @param reverseKeyOrder sort the hash in a reverse order
     * @return list of terms.
     */
    List<Term> getTermsByName(String partialName, String ontologyId, boolean reverseKeyOrder) throws RestClientException;

    /**
     * This function retrieve one specific term that is equal to the exactName.
     * @param exactName String to lookup in the name term
     * @param ontologyId Ontology term
     * @return The extracted term.
     */
    Term getExactTermByName(String exactName, String ontologyId);

    /**
     * This method retrieve a List with the child terms for an specific term.
     * @param termOBOId Term Identifier
     * @param ontologyId Ontology Name
     * @param distance Distance to the child (1..n) where the distance is the step to the children.
     * @return list of terms.
     */
    List<Term> getTermChildren(Identifier termOBOId, String ontologyId, int distance) throws RestClientException;


    /**
     * This method retrieve a List with the parent terms for an specific term.
     * @param termOBOId Term Identifier
     * @param ontologyId Ontology Name
     * @param distance Distance to the parent (1..n) where the distance is the step to the children.
     * @return list of terms.
     */
    List<Term> getTermParents(Identifier termOBOId, String ontologyId, int distance) throws RestClientException;

    /**
     * If the term is obsolete in the database
     * @param termId Term id
     * @param ontologyId ontology Database
     * @return true if the term is obsolete, false if not obsolete.
     */

    Boolean isObsolete(Identifier termId, String ontologyId) throws RestClientException;

    /**
     * This function try to find the annotations in the file thant contains the a value for an specific annotation
     * value.
     * @param ontologyId Ontology Name
     * @param annotationType The annotation name where the function will look.
     * @param strValue the current value to be search
     * @return list of annotations that fit the value
     */
    List<Term> getTermsByAnnotationData(String ontologyId, String annotationType, String strValue) throws RestClientException;

    /**
     * This function try yto fin the annotations in the ontology by an interval double value.
     * @param ontologyId The name of the ontology
     * @param annotationType the name of the annotation
     * @param fromDblValue the min limit of the interval
     * @param toDblValue the max value of the interval
     * @return list of annotations that fit the value
     */
    List<Term> getTermsByAnnotationData(String ontologyId, String annotationType, double fromDblValue, double toDblValue) throws RestClientException;

    /**
     * This function search for a term in the ols with the current Id term and the ontologyID
     * @param identifier partial ontology ID
     * @param ontologyId ontology to search
     * @return A map with all the terms that contains the present pattern
     * @throws RestClientException if there are problems connecting to the REST service.
     */
    List<Term> searchTermById(String identifier, String ontologyId) throws RestClientException;

    /**
     * Retrieve an specific ontology information for an ID
     * @param ontologyId ontology Identifier
     * @return Ontology result.
     * @throws RestClientException if there are problems connecting to the REST service.
     */
    Ontology getOntology(String ontologyId) throws RestClientException;


    /**
     * Retrieve all synonyms. (Not OBO Synonyms!)
     * @param identifier Term Identifier
     * @param ontology ontology Database
     * @return A Set of all synonyms
     */
    Set<String> getSynonyms(Identifier identifier, String ontology);
}
