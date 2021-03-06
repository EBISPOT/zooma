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

    //default parameters of search page number and search page size
    private int defaultPageNum;
    private int defaultPageSize;


    @Override
    protected void doInitialization() throws Exception {

        String olsServer = this.configuration.getProperty("ols.server");
        if (olsServer != null) {
            this.olsClient = new OLSClient(new OLSWsConfig(olsServer));
        } else {
            this.olsClient = new OLSClient(new OLSWsConfig());
        }

        this.defaultPageNum = this.olsClient.getSearchPageNum();
        this.defaultPageSize = this.olsClient.getSearchPageSize();

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

    public List<Term> getExactTermsByName(String value){
        specificSearchParams(0, 20);
        return filterDefiningOntology(getExactTermsByName(value, ""));
    }

    public List<Term> getTermsByName(String value){
        specificSearchParams(0, 2);
        return filterDefiningOntology(getTermsByName(value, ""));
    }

    public List<Term> getExactTermsByName(String value, ArrayList<String> sources){
        specificSearchParams(0, 20);
        List<Term> terms = new ArrayList<>();
        for (String source : sources) {
            terms.addAll(getExactTermsByName(value, source));
        }
        return filterDefiningOntology(terms);
    }

    public List<Term> getTermsByName(String value, ArrayList<String> sources){
        specificSearchParams(0, 2);
        List<Term> terms = new ArrayList<>();
        for (String source : sources) {
            terms.addAll(getTermsByName(value, source));
        }
        return filterDefiningOntology(terms);
    }

    private List<Term> getExactTermsByName(String value, String source){
        try {
            specificSearchParams(0, 20);
            return olsClient.getExactTermsByName(value, source);
        } catch (RestClientException e){
            return new ArrayList<>();
        }
    }

    private List<Term> getTermsByName(String value, String source){
        try {
            specificSearchParams(0, 2);
            return olsClient.getTermsByName(value, source, false);
        } catch (RestClientException e){
            return new ArrayList<>();
        }
    }


    public List<Term> getExactTermsByNameFromParent(String value, String childrenOf){
        specificSearchParams(0, 20);
        return filterDefiningOntology(getExactTermsByNameFromParent(value, "", childrenOf));
    }


    public List<Term> getTermsByNameFromParent(String value, String childrenOf){
        specificSearchParams(0, 2);
        return filterDefiningOntology(getTermsByNameFromParent(value, "", childrenOf));
    }

    public List<Term> getExactTermsByNameFromParent(String value, ArrayList<String> sources, String childrenOf){
        specificSearchParams(0, 20);
        List<Term> terms = new ArrayList<>();
        for (String source : sources) {
            terms.addAll(getExactTermsByNameFromParent(value, source, childrenOf));
        }
        return filterDefiningOntology(terms);
    }

    public List<Term> getTermsByNameFromParent(String value, ArrayList<String> sources, String childrenOf){
        specificSearchParams(0, 2);
        List<Term> terms = new ArrayList<>();
        for (String source : sources) {
            terms.addAll(getTermsByNameFromParent(value, source, childrenOf));
        }
        return filterDefiningOntology(terms);
    }

    private List<Term> getExactTermsByNameFromParent(String value, String source, String childrenOf){
        try {
            specificSearchParams(0, 20);
            return olsClient.getExactTermsByNameFromParent(value, source, childrenOf);
        } catch (RestClientException e){
            return new ArrayList<>();
        }
    }

    private List<Term> getTermsByNameFromParent(String value, String source, String childrenOf){
        try {
            specificSearchParams(0, 2);
            return olsClient.getTermsByNameFromParent(value, source, false, childrenOf);
        } catch (RestClientException e){
            return new ArrayList<>();
        }
    }

    public String getExactLabelByIri(String iri){
        try {
            defaultSearchParams();
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
        defaultSearchParams();
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
            defaultSearchParams();
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
            defaultSearchParams();
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
            defaultSearchParams();
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

    public boolean inOLS(URI uri) {
        defaultSearchParams();
         List<Term> terms = olsClient.getExactTermsByIriString(uri.toString());
         if (terms.isEmpty()){
             return false;
         }
         return true;
    }

    /**
     * Restrics OLS client search to a page number and a page size
     * @param pageNum
     * @param pageSize
     */
    private void specificSearchParams(int pageNum, int pageSize){
        this.olsClient.setSearchPageNum(pageNum);
        this.olsClient.setSearchPageSize(pageSize);
    }

    /**
     * Sets OLS client search page number and search page size to the defaults it had when initialized
     */
    private void defaultSearchParams(){
        this.olsClient.setSearchPageNum(this.defaultPageNum);
        this.olsClient.setSearchPageSize(this.defaultPageSize);
    }
}
