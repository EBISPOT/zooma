package uk.ac.ebi.fgpt.zooma.service;

import org.springframework.web.client.RestClientException;
import uk.ac.ebi.fgpt.zooma.Initializable;
import uk.ac.ebi.fgpt.zooma.util.URIUtils;
import uk.ac.ebi.pride.utilities.ols.web.service.client.OLSClient;
import uk.ac.ebi.pride.utilities.ols.web.service.config.OLSWsConfig;
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

    private Properties configuration;

    public Properties getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Properties configuration) {
        this.configuration = configuration;
    }

    //holds all the ontology <namespace, Ontology> mappings
    private Map<String, Ontology> ontologyMappings;

    private Map<URI, URI> originalToReplacementCache = new HashMap<>();
    private Map<URI, Boolean> replaceableCache = new HashMap<>();


    @Override
    protected void doInitialization() throws Exception {

        String olsServer = this.configuration.getProperty("ols.server");
        if (olsServer != null) {
            this.olsClient = new OLSClient(new OLSWsConfig(olsServer));
        } else {
            this.olsClient = new OLSClient(new OLSWsConfig());
        }
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
            List<Term> terms = olsClient.getExactTermsByIriString(iri);

            for (Term term : terms){
                if (term.isDefinedOntology()){
                    return term.getLabel();
                }
            }
        } catch (RestClientException e){
            return  URIUtils.extractFragment(URI.create(iri));
        }

        return  URIUtils.extractFragment(URI.create(iri));
    }


    public boolean isReplaceable(URI semanticTag){
        Boolean replaceable = replaceableCache.get(semanticTag);

        if (replaceable == null) {
            URI replacement = this.tryToReplaceSemanticTag(semanticTag);
            if (replacement != null){
                replaceableCache.put(semanticTag, true);
                originalToReplacementCache.put(semanticTag, replacement);
            } else {
                replaceableCache.put(semanticTag, false);
            }
        }

        return replaceableCache.get(semanticTag);
    }

    public URI replaceSemanticTag(URI semanticTag){
        if (isReplaceable(semanticTag)){
            return originalToReplacementCache.get(semanticTag);
        } else {
            throw new IllegalArgumentException("Term is either not obsolete, or doesn't have a replacement.");
        }
    }

    private URI tryToReplaceSemanticTag(URI semanticTag){
        Term replaceBy = null;
        try {
            replaceBy = olsClient.getReplacedBy(semanticTag.toString());
        } catch (RestClientException e){
            getLog().debug(semanticTag + " something went wrong when we tried to find a replacement in OLS!");
            replaceBy = null;
        }
        if(replaceBy != null && replaceBy.getIri() != null && replaceBy.getIri().getIdentifier() != null) {
            return URI.create(replaceBy.getIri().getIdentifier());
        }
        return null;
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
