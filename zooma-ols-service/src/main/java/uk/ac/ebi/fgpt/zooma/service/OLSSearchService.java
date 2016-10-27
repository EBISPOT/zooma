package uk.ac.ebi.fgpt.zooma.service;

import org.springframework.web.client.RestClientException;
import uk.ac.ebi.fgpt.zooma.Initializable;
import uk.ac.ebi.pride.utilities.ols.web.service.client.OLSClient;
import uk.ac.ebi.pride.utilities.ols.web.service.config.OLSWsConfigProd;
import uk.ac.ebi.pride.utilities.ols.web.service.model.*;

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
        return filterDefiningOntology(getTermsByName(value, ""));
    }

    public List<Term> getTermsByName(String value, ArrayList<String> sources){

        List<Term> terms = new ArrayList<>();
        for (String source : sources) {
            terms.addAll(getTermsByName(value, source));
        }
        return filterDefiningOntology(terms);
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
        return filterDefiningOntology(getTermsByNameFromParent(value, "", childrenOf));
    }

    public List<Term> getTermsByNameFromParent(String value, ArrayList<String> sources, String childrenOf){
        List<Term> terms = new ArrayList<>();
        for (String source : sources) {
            terms.addAll(getTermsByNameFromParent(value, source, childrenOf));
        }
        return filterDefiningOntology(terms);
    }

    private List<Term> getTermsByNameFromParent(String value, String source, String childrenOf){
        try {
            return olsClient.getExactTermsByNameFromParent(value, source, childrenOf);
        } catch (RestClientException e){
            return new ArrayList<>();
        }
    }

    public String getLabelByIri(String iri){
        try {
            String customQueryField = new QueryFields.QueryFieldBuilder()
                    .setIri()
                    .build()
                    .toString();
            olsClient.setQueryField(customQueryField);

            String fieldList = new FieldList.FieldListBuilder()
                    .setLabel()
                    .setIri()
                    .setIsDefiningOntology()
                    .build()
                    .toString();
            olsClient.setFieldList(fieldList);

            List<Term> terms = olsClient.getExactTermsByName(iri, null);
            for (Term term : terms){
                if (term.isDefinedOntology()){
                    return term.getLabel();
                }
            }
        } catch (RestClientException e){
            return "";
        } finally {
            //restore olsClient search to it's default query field and field list
            olsClient.setQueryField(olsClient.DEFAULT_QUERY_FIELD);
            olsClient.setFieldList(olsClient.DEFAULT_FIELD_LIST);
        }
        return "";
    }

    /*
     * If at least one result has "is_defining_ontology = true" then return only those ones.
     * If no terms have "is_defining_ontology = true", then return them all.
     */
    private List<Term> filterDefiningOntology(List<Term> terms){

        if (terms == null || terms.isEmpty()){
            return new ArrayList<>();
        }

        List<Term> termsWithDefiningOntologyTrue = new ArrayList<>();
        List<Term> termsWithDefiningOntologyFalse = new ArrayList<>();

        for (Term term : terms){
            if (term.isDefinedOntology()){
                if (term.getScore() != null) {
                    term.setScore(String.valueOf(Float.valueOf(term.getScore()) + 1));
                    termsWithDefiningOntologyTrue.add(term);
                }
            } else {
                if (term.getScore() != null) {
                    termsWithDefiningOntologyFalse.add(term);
                }
            }
        }

        if (termsWithDefiningOntologyTrue.isEmpty()){
            return termsWithDefiningOntologyFalse;
        } else {
            return termsWithDefiningOntologyTrue;
        }

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
