package uk.ac.ebi.fgpt.zooma.service;

import org.springframework.web.client.RestClientException;
import uk.ac.ebi.fgpt.zooma.Initializable;
import uk.ac.ebi.pride.utilities.ols.web.service.client.OLSClient;
import uk.ac.ebi.pride.utilities.ols.web.service.config.OLSWsConfigProd;
import uk.ac.ebi.pride.utilities.ols.web.service.model.Ontology;
import uk.ac.ebi.pride.utilities.ols.web.service.model.Term;

import java.net.URI;
import java.util.*;

/**
 *
 * Uses the PRIDE ols-client, modified to the SPOT needs (https://github.com/EBISPOT/ols-client)
 *
 * Created by olgavrou on 20/05/2016.
 */
public class OLSSearchService extends Initializable {

    private OLSClient olsClient;

    //holds all the ontology <namespace, Ontology> mappings
    private Map<String, Ontology> ontologyMappings;

    @Override
    protected void doInitialization() throws Exception {

        this.olsClient = new OLSClient(new OLSWsConfigProd());
        this.ontologyMappings = new HashMap<>();
        populateOntologyMappings();

    }

    private void populateOntologyMappings() {
        List<Ontology> ontologies = this.getAllOntologies();
        if (ontologies != null && !ontologies.isEmpty()) {
            for (Ontology ontology : ontologies) {
                this.ontologyMappings.put(ontology.getConfig().getId(), ontology);
            }
        }
    }

    @Override
    protected void doTermination() throws Exception {

    }

    public List<Term> getTermsByName(String value){
        return increaseScoreForDefiningOntologyTerm(getTermsByName(value, ""));
    }

    public List<Term> getTermsByName(String value, ArrayList<String> sources){

        List<Term> terms = new ArrayList<>();
        for (String source : sources) {
            terms.addAll(getTermsByName(value, source));
        }
        return increaseScoreForDefiningOntologyTerm(terms);
    }

    private List<Term> getTermsByName(String value, String source){
        try {
            return olsClient.getExactTermsByName(value, source);
        } catch (RestClientException e){
            return new ArrayList<>();
        }
    }
    //
    public List<Term> getTermsByNameFromParent(String value, String childrenOf){
        return increaseScoreForDefiningOntologyTerm(getTermsByNameFromParent(value, "", childrenOf));
    }

    public List<Term> getTermsByNameFromParent(String value, ArrayList<String> sources, String childrenOf){
        List<Term> terms = new ArrayList<>();
        for (String source : sources) {
            terms.addAll(getTermsByNameFromParent(value, source, childrenOf));
        }
        return increaseScoreForDefiningOntologyTerm(terms);
    }

    private List<Term> getTermsByNameFromParent(String value, String source, String childrenOf){
        try {
            return olsClient.getExactTermsByNameFromParent(value, source, childrenOf);
        } catch (RestClientException e){
            return new ArrayList<>();
        }
    }

    /*
     * If the Term returned is from a defining ontology the score is increased by one.
     * This is done due to the reason that if duplicate iri's are returned from different ontologies
     * in OLS, Zooma will keep only one of them, and we want to keep the one from the defining ontology.
     */
    private List<Term> increaseScoreForDefiningOntologyTerm(List<Term> terms){

        if (terms == null || terms.isEmpty()){
            return new ArrayList<>();
        }

        List<Term> survivalTerms = new ArrayList<>();
        for (Term term : terms){
            if (term.isDefinedOntology()){
                if (term.getScore() != null) {
                    term.setScore(String.valueOf(Float.valueOf(term.getScore()) + 1));
                    survivalTerms.add(term);
                }
            } else {
                if (term.getScore() != null) {
                    survivalTerms.add(term);
                }
            }
        }

        return survivalTerms;
    }

    public List<Ontology> getAllOntologies(){
        try {
            if (this.ontologyMappings != null && !this.ontologyMappings.isEmpty()) {
                return (new ArrayList<>(ontologyMappings.values()));
            }
            return olsClient.getOntologies();
        } catch (RestClientException e){
            return new ArrayList<>();
        }
    }

    /*
     * Returns the ontology for a given ontology name
     */
    public Ontology getOntology(String name){

        if (ontologyMappings.containsValue(name)){
            for (String key : ontologyMappings.keySet()){
                if (ontologyMappings.get(key).equals(name)){
                    return ontologyMappings.get(key);
                }
            }
        }
        try {
            Ontology ontology = olsClient.getOntology(name);
            ontologyMappings.put(ontology.getId(), ontology);
            return ontology;
        } catch (RestClientException e ){
            return null;
        }
    }

    public String getOntologyNamespaceFromId(String uri){

        if (ontologyMappings.containsKey(uri)){
            return ontologyMappings.get(uri).getNamespace();
        }

        try {
            Ontology ontology = olsClient.getOntologyFromId(URI.create(uri));
            if (ontology != null){
                ontologyMappings.put(ontology.getId(), ontology);
                return ontology.getConfig().getNamespace();
            }
        } catch (RestClientException e){
            return null;
        }
        return null;
    }

}
