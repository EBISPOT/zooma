package uk.ac.ebi.fgpt.zooma.service;

import javafx.util.Pair;
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

    //holds all the ontology <id, namespace> mappings
    private Map<String, String> ontologyMappings;

    @Override
    protected void doInitialization() throws Exception {

        this.olsClient = new OLSClient(new OLSWsConfigProd());
        this.ontologyMappings = new HashMap<>();

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
        return olsClient.getExactTermsByName(value, source);
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
        return olsClient.getExactTermsByNameFromParent(value, source, childrenOf);
    }

    /*
     * If the Term returned is from a defining ontology the score is increased by one.
     * This is done due to the reason that if duplicate iri's are returned from different ontologies
     * in OLS, Zooma will keep only one of them, and we want to keep the one from the defining ontology.
     */
    private List<Term> increaseScoreForDefiningOntologyTerm(List<Term> terms){

        for (Term term : terms){
            if (term.isDefinedOntology()){
                term.setScore(String.valueOf(Float.valueOf(term.getScore()) + 1));
            }
        }

        return terms;
    }

    public Map<String, String> getAllOntologies(){

        List<Ontology> ontologyList = olsClient.getOntologies();
        for (Ontology ontology : ontologyList){
            ontologyMappings.put(ontology.getConfig().getId(), ontology.getConfig().getNamespace());
        }

        return ontologyMappings;
    }

    /*
     * Returns a pair of the ontology id <-> ontology nampespace, for a given ontology name
     */
    public Pair<String, String> getOntology(String name){

        if (ontologyMappings.containsValue(name)){
            for (String key : ontologyMappings.keySet()){
                if (ontologyMappings.get(key).equals(name)){
                    return new Pair<String, String>(key, name);
                }
            }
        }
        Ontology ontology = olsClient.getOntology(name);
        return new Pair(ontology.getConfig().getId(), ontology.getConfig().getNamespace());
    }

    public String getOntologyNamespaceFromId(String uri){

        if (ontologyMappings.containsKey(uri)){
            return ontologyMappings.get(uri);
        }

        Ontology ontology = olsClient.getOntologyFromId(URI.create(uri));
        if (ontology != null){
            return ontology.getConfig().getNamespace();
        }
        return null;
    }

}
