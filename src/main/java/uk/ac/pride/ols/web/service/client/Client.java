package uk.ac.pride.ols.web.service.client;

import uk.ac.pride.ols.web.service.model.Annotation;
import uk.ac.pride.ols.web.service.model.AnnotationHolder;

import java.util.List;
import java.util.Map;

/**
 * @author Yasset Perez-Riverol (ypriverol@gmail.com)
 * @date 01/03/2016
 */
public interface Client {

    /**
     * This function retrieve the term by the accession of the term in the ontogoly and the id of the ontology
     * if the term is not found it, the NULL is returned.
     *
     * @param termId Term ID in the ontology
     * @param ontologyName The ontology name
     * @return return the name of the Ontology term
     */
    String getTermByOBOId(String termId, String ontologyName);

    /**
     * This function retrieve the metadata for an specific term, specially the description
     * if the term is not present in the ontology the NULL value is retrieved. The metadata contains for example
     * the definition and the comment related with the Term.
     *
     * @param termId Term ID in the ontology
     * @param ontologyName The ontology name
     * @return return the metadata (description) of the Ontology term
     */
    Map<String,String> getTermMetadata(String termId, String ontologyName);

    /**
     * The geTXRefTerm in the ontology is used to retrieve this value
     * @param termId Term ID in the ontology
     * @param ontologyName The ontology name
     * @return return a HashMap with all the references to the specific Term.
     */
    Map<String, String> getTermXrefs(String termId, String ontologyName);

    /**
     * This function retrieve the list of Ontologies in the OLS. The Key correspond to the Ontology Id
     * and the value correspond to the name of the ontology.
     * @return Map<String,String> contains all the ontologies in the resource.
     */
    Map<String, String> getOntologyNames();


    /**
     * Retrieve all the terms in an specific ontology. The Map contains in the key the term identifier and in the value
     * contains the term name.
     * @param ontologyName
     * @return
     */
    Map<String, String> getAllTermsFromOntology(String ontologyName);

    /**
     * This function use an ontology name to retrieve all the root terms for the ontology
     * @param ontologyName The ontology name
     * @return Map<String, String> where the key is the id of the term and the value is the name..
     */
    Map<String,String> getRootTerms(String ontologyName);

    /**
     * This function retrieve all the terms that contains in the name the partialName.
     * @param partialName Substring to lookup in the name term
     * @param ontologyName Ontology term
     * @param reverseKeyOrder sort the hash in a reverse order
     * @return Has<String, String> contains in the key the id of the term and in the value the name of the term.
     */
    Map<String,String> getTermsByName(String partialName, String ontologyName, boolean reverseKeyOrder);

    /**
     * This method retrieve a HashMap with the child terms for an specific term. In the Hash<String,String>
     * the key correspond to the Term Identifier and the value correspond to the name in the ontology.
     *
     * @param termOBOId Term Identifier
     * @param ontologyId Ontology Name
     * @param distance Distance to the child (1..n) where the distance is the step to the chields.
     * @return Map<String, String> A Term Id and the corresponding name
     */
    Map<String,String> getTermChildren(String termOBOId, String ontologyId, int distance);

    /**
     * If the term is obsolete in the database
     * @param termId Term id
     * @param ontologyName ontology Database
     * @return true if the term is obsolete, false if not obsolete.
     */

    Boolean isObsolete(String termId, String ontologyName);

    /**
     * This function try to find the annotations in the file thant contains the a value for an specific annotation
     * value.
     * @param ontologyName Ontology Name
     * @param annotationType The annotation name where the function will look.
     * @param strValue the current value to be search
     * @return A list of annotations that fit the value
     */
    List<AnnotationHolder> getTermsByAnnotationData(String ontologyName, String annotationType, String strValue);

    /**
     * This function try yto fin the annotations in the ontology by an interval double value.
     * @param ontologyName The name of the ontology
     * @param annotationType the name of the annotation
     * @param fromDblValue the min limit of the interval
     * @param toDblValue the max value of the interval
     * @return the annotation list
     */
    List<AnnotationHolder> getTermsByAnnotationData(String ontologyName, String annotationType, double fromDblValue, double toDblValue);
}
