package uk.ac.pride.ols.web.service.client;

import org.springframework.web.client.RestTemplate;
import uk.ac.pride.ols.web.service.config.AbstractOLSWsConfig;
import uk.ac.pride.ols.web.service.model.*;
import uk.ac.pride.ols.web.service.utils.Constants;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;


/**
 * @author ypriverol
 */
public class OLSClient implements Client{

    protected RestTemplate restTemplate;
    protected AbstractOLSWsConfig config;

    /**
     * Default constructor for Archive clients
     * @param config
     */
    public OLSClient(AbstractOLSWsConfig config){
        this.config = config;
        this.restTemplate = new RestTemplate();
    }

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public AbstractOLSWsConfig getConfig() {
        return config;
    }

    public void setConfig(AbstractOLSWsConfig config) {
        this.config = config;
    }

    public String getTermByOBOId(String termId, String ontologyName) {

        String url = String.format("%s://%s/api/ontologies/%s/terms?obo_id=%s",
                config.getProtocol(), config.getHostName(),ontologyName,termId);

        TermQuery result = this.restTemplate.getForObject(url, TermQuery.class);

        if(result != null && result.getTerms() != null && result.getTerms().length == 1){
            return result.getTerms()[0].getLabel();
        }

        return null;
    }

    public Map<String, String> getTermMetadata(String termId, String ontologyName) {
        return null;
    }

    public Map<String, String> getTermXrefs(String termId, String ontologyName) {
        return null;
    }

    /**
     * This function returns the Map where the key is the namespace of the ontogoly and
     * the value is the name of the ontology
     * @return Ontology Map
     */
    public Map<String, String> getOntologyNames() {
        Map<String, String> ontologyNames = new HashMap<String, String>();
        OntologyQuery currentOntologyQuery = getOntologyQuery(0);
        List<Ontology> ontologies = new ArrayList<Ontology>();
        ontologies.addAll(Arrays.asList(currentOntologyQuery.getOntolgoies()));
        if(currentOntologyQuery != null){
            if(currentOntologyQuery.getOntolgoies().length < currentOntologyQuery.getPage().getTotalElements()){
                for(int i = 1; i < currentOntologyQuery.getPage().getTotalElements()/currentOntologyQuery.getOntolgoies().length + 1; i++){
                    OntologyQuery ontologyQuery = getOntologyQuery(i);
                    if(ontologyQuery != null && ontologyQuery.getOntolgoies() != null)
                        ontologies.addAll(Arrays.asList(ontologyQuery.getOntolgoies()));
                }
            }
        }
        for(Ontology ontology: ontologies)
            ontologyNames.put(ontology.getId(), ontology.getName());

        return ontologyNames;
    }

    private OntologyQuery getOntologyQuery(int page){
        String query = String.format("%s://%s/api/ontologies?page=%s&size=%s",
                config.getProtocol(), config.getHostName(), page, Constants.ONTOLOGY_PAGE_SIZE);

        OntologyQuery ontologyQuery = this.restTemplate.getForObject(query, OntologyQuery.class);
        return ontologyQuery;

    }

    /**
     * Return a Map with all terms for an ontology where the key is the obo ontology id and
     * the value is the name of the term, also called label in current OLS
     * @param ontologyID Ontology reference
     * @return A map with the Terms
     */
    public Map<String, String> getAllTermsFromOntology(String ontologyID) {
        Map<String, String> termNames = new HashMap<String, String>();
        TermQuery currentTermQuery = getTermQuery(0, ontologyID);
        List<Term> terms = new ArrayList<Term>();
        terms.addAll(Arrays.asList(currentTermQuery.getTerms()));
        if(currentTermQuery != null){
            if(currentTermQuery.getTerms().length < currentTermQuery.getPage().getTotalElements()){
                for(int i = 1; i < currentTermQuery.getPage().getTotalElements()/currentTermQuery.getTerms().length + 1; i++){
                    TermQuery termQuery = getTermQuery(i, ontologyID);
                    if(termQuery != null && termQuery.getTerms() != null)
                        terms.addAll(Arrays.asList(termQuery.getTerms()));
                }
            }
        }
        for(Term term: terms)
            termNames.put(term.getTermOBOId(), term.getLabel());

        return termNames;

    }

    private TermQuery getTermQuery(int page, String ontologyID) {

        String query = String.format("%s://%s/api/ontologies/%s/terms/?page=%s&size=%s",
                config.getProtocol(), config.getHostName(),ontologyID, page, Constants.TERM_PAGE_SIZE);
        TermQuery termQuery = this.restTemplate.getForObject(query, TermQuery.class);

        return termQuery;
    }

    public Map<String, String> getRootTerms(String ontologyName) {
        return null;
    }

    /**
     * This function retrieve all the terms from an specific ontology and perform a search in the client side.
     * In the future would be great to repleace the current functionality with the search capabilities in the ols.
     * @param partialName Substring to lookup in the name term
     * @param ontologyID
     * @param reverseKeyOrder sort the hash in a reverse order
     * @return
     */
    public Map<String, String> getTermsByName(String partialName, String ontologyID, boolean reverseKeyOrder) {
        Map<String,String> ontologyTerms = getAllTermsFromOntology(ontologyID);
        Map<String, String> resultTerms = new HashMap<String, String>();
        //Todo: Replace the current functionality with the search capabilities of the OLS.
        for(Map.Entry entry: ontologyTerms.entrySet())
            if(((String)entry.getValue()).toUpperCase().contains(partialName.toUpperCase()))
                resultTerms.put((String)entry.getKey(), (String)entry.getValue());
        if(reverseKeyOrder){
            Map<String, String> newMap = new TreeMap(Collections.reverseOrder());
            newMap.putAll(resultTerms);
            resultTerms = newMap;
        }
        return resultTerms;
    }

    public Map<String, String> getTermChildren(String termOBOId, String ontologyID, int distance) {
        Map<String, String> terms = new HashMap<String, String>();
        String query = String.format("%s://%s/api/ontologies/%s/terms?obo_id=%s",
                config.getProtocol(), config.getHostName(),ontologyID, termOBOId);
        TermQuery termQuery = this.restTemplate.getForObject(query, TermQuery.class);

        if(termQuery != null && termQuery.getTerms() != null && termQuery.getTerms().length == 1 &&
                termQuery.getTerms()[0] != null && termQuery.getTerms()[0].getLink() != null &&
                termQuery.getTerms()[0].getLink().getChildrenRef() != null)
             terms = getTermChildrenMap(termQuery.getTerms()[0].getLink().getChildrenRef(), distance);
        return terms;
    }

    private Map<String, String> getTermChildrenMap(Href childrenHRef, int distance){
        Map<String, String> children = new HashMap<String, String>();
        if(distance == 0)
            return Collections.EMPTY_MAP;
        List<Term> childTerms = getTermChildren(childrenHRef, distance);
        for(Term term: childTerms)
            children.put(term.getTermOBOId(), term.getLabel());
        return children;
    }

    List<Term> getTermChildren(Href hrefChildren, int distance){
        if(distance == 0)
            return new ArrayList<Term>();
        List<Term> chieldTerms = new ArrayList<Term>();
        chieldTerms.addAll(getTermQuery(hrefChildren));
        distance--;
        List<Term> currentChild = new ArrayList<Term>();
        for(Term chield: chieldTerms)
            currentChild.addAll(getTermChildren(chield.getLink().getChildrenRef(), distance));
        chieldTerms.addAll(currentChild);
        return chieldTerms;

    }

    private List<Term> getTermQuery(Href href){
        if(href == null)
            return new ArrayList<Term>();
        List<Term> terms = new ArrayList<Term>();
        try {
            String query = href.getHref();
            String url = URLDecoder.decode(query.toString(), "UTF-8");
            TermQuery termQuery = this.restTemplate.getForObject(url, TermQuery.class);
            if(termQuery != null && termQuery.getTerms() != null){
                if(terms == null)
                    terms = new ArrayList<Term>();
                terms.addAll(Arrays.asList(termQuery.getTerms()));
            }
            if(termQuery != null && termQuery.getLink() != null && termQuery.getLink().next() != null)
                terms.addAll(getTermQuery(termQuery.getLink().next()));

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        return terms;
    }


    /**
     * This function return true if the term is obsolete, if the term is not found in the ontology the function
     * return null, also if the value is not found.
     * @param termOBOId The OBOId of the Term in the ols ontology
     * @param ontologyID the ontology ID
     * @return
     */
    public Boolean isObsolete(String termOBOId, String ontologyID) {
        String query = String.format("%s://%s/api/ontologies/%s/terms?obo_id=%s",
                config.getProtocol(), config.getHostName(),ontologyID, termOBOId);
        TermQuery termQuery = this.restTemplate.getForObject(query, TermQuery.class);
        if(termQuery != null && termQuery.getTerms() != null && termQuery.getTerms().length == 1 &&
           termQuery.getTerms()[0] != null)
            return termQuery.getTerms()[0].isObsolete();

        return null;
    }

    public List<Annotation> getTermsByAnnotationData(String ontologyName, String annotationType, String strValue) {
        return null;
    }

    public List<Annotation> getTermsByAnnotationData(String ontologyName, String annotationType, double fromDblValue, double toDblValue) {
        return null;
    }
}
