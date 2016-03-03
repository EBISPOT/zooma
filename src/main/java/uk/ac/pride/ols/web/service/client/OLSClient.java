package uk.ac.pride.ols.web.service.client;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestClientException;
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
        initRestTemplate();
    }

    public void initRestTemplate(){
//        List<HttpMessageConverter<?>> converters = restTemplate.getMessageConverters();
//        for (HttpMessageConverter<?> converter : converters) {
//            if (converter instanceof MappingJackson2HttpMessageConverter) {
//                MappingJackson2HttpMessageConverter jsonConverter = (MappingJackson2HttpMessageConverter) converter;
//                jsonConverter.setObjectMapper(new ObjectMapper());
//                List<MediaType> mediaTypes = new ArrayList<MediaType>();
//                mediaTypes.add(new MediaType("*", "json", MappingJackson2HttpMessageConverter.DEFAULT_CHARSET));
//                mediaTypes.add(new MediaType("application", "octet-stream", MappingJackson2HttpMessageConverter.DEFAULT_CHARSET));
//                jsonConverter.setSupportedMediaTypes(mediaTypes);
//            }
//        }
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

    public String getTermByOBOId(String termOBOId, String ontologyName) throws RestClientException{

        String url = String.format("%s://%s/api/ontologies/%s/terms?obo_id=%s",
                config.getProtocol(), config.getHostName(),ontologyName,termOBOId);

        TermQuery result = this.restTemplate.getForObject(url, TermQuery.class);

        if(result != null && result.getTerms() != null && result.getTerms().length == 1){
            return result.getTerms()[0].getLabel();
        }

        return null;
    }

    public Map<String, String> getTermMetadata(String termId, String ontologyName) throws RestClientException{
        return null;
    }

    public Map<String, String> getTermXrefs(String termId, String ontologyName) throws RestClientException{
        return null;
    }

    /**
     * This function returns the Map where the key is the namespace of the ontogoly and
     * the value is the name of the ontology
     * @return Ontology Map
     */
    public Map<String, String> getOntologyNames() throws RestClientException{
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

    private OntologyQuery getOntologyQuery(int page) throws RestClientException{
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
    public Map<String, String> getAllTermsFromOntology(String ontologyID) throws RestClientException{
        Map<String, String> termNames = new HashMap<String, String>();
        List<Term> terms = getAllOBOTermsFromOntology(ontologyID);
        for(Term term: terms)
            termNames.put(term.getTermOBOId(), term.getLabel());

        return termNames;

    }

    private List<Term> getAllOBOTermsFromOntology(String ontologyID) throws RestClientException{
        TermQuery currentTermQuery = getTermQuery(0, ontologyID);
        List<Term> terms = new ArrayList<Term>();
        if(currentTermQuery != null && currentTermQuery.getTerms() != null){
            terms.addAll(Arrays.asList(currentTermQuery.getTerms()));
            if(currentTermQuery.getTerms().length < currentTermQuery.getPage().getTotalElements()){
                for(int i = 1; i < currentTermQuery.getPage().getTotalElements()/currentTermQuery.getTerms().length + 1; i++){
                    TermQuery termQuery = getTermQuery(i, ontologyID);
                    if(termQuery != null && termQuery.getTerms() != null)
                        terms.addAll(Arrays.asList(termQuery.getTerms()));
                }
            }
        }
        return terms;
    }

    private TermQuery getTermQuery(int page, String ontologyID) {

        String query = String.format("%s://%s/api/ontologies/%s/terms/?page=%s&size=%s",
                config.getProtocol(), config.getHostName(),ontologyID, page, Constants.TERM_PAGE_SIZE);
        TermQuery termQuery = this.restTemplate.getForObject(query, TermQuery.class);

        return termQuery;
    }

    public Map<String, String> getRootTerms(String ontologyID) {
        List<Term> terms = getAllOBOTermsFromOntology(ontologyID);
        Map<String, String> resultTerms = new HashMap<String, String>();
        for(Term term: terms)
            if(term != null && term.isRoot())
                resultTerms.put(term.getTermOBOId(), term.getLabel());

        return resultTerms;
    }

    public Map<String, String> getAllRootTerms(){
        Map<String, String> ontologies = getOntologyNames();
        Map<String, String> terms = new HashMap<String, String>();
        for(String ontologyID: ontologies.keySet())
            terms.putAll(getRootTerms(ontologyID));
        return terms;
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
        Map<String, String> resultTerms = new HashMap<String, String>();
        if(partialName == null || partialName.isEmpty())
            return resultTerms;

        if(ontologyID == null || ontologyID.isEmpty())
            resultTerms = searchByPartialTerm(partialName, null);
        else
            resultTerms = searchByPartialTerm(partialName, ontologyID);

        if(reverseKeyOrder){
            Map<String, String> newMap = new TreeMap(Collections.reverseOrder());
            newMap.putAll(resultTerms);
            resultTerms = newMap;
        }
        return resultTerms;
    }

    private Map<String, String> searchByPartialTerm(String partialName, String ontology) throws RestClientException{
        Map<String, String> termResults = new HashMap<String, String>();
        SearchQuery currentTermQuery = getSearchQuery(0, partialName, ontology);
        List<SearchResult> terms = new ArrayList<SearchResult>();
        if(currentTermQuery != null && currentTermQuery.getResponse() != null && currentTermQuery.getResponse().getSearchResults() != null){
            terms.addAll(Arrays.asList(currentTermQuery.getResponse().getSearchResults()));
            if(currentTermQuery.getResponse().getSearchResults().length < currentTermQuery.getResponse().getNumFound()){
                for(int i = 1; i < currentTermQuery.getResponse().getNumFound()/currentTermQuery.getResponse().getSearchResults().length + 1; i++){
                    SearchQuery termQuery = getSearchQuery(i, partialName, ontology);
                    if(termQuery != null && termQuery.getResponse() != null && termQuery.getResponse().getSearchResults() != null)
                        terms.addAll(Arrays.asList(termQuery.getResponse().getSearchResults()));
                }
            }
        }
        for(SearchResult result: terms)
             if(result.getObo_id() != null && result.getName() != null)
                 termResults.put(result.getObo_id(), result.getName());
        return termResults;
    }

    private SearchQuery getSearchQuery(int page, String partialName, String ontology) throws RestClientException{
        String query = String.format("%s://%s/api/search?q=*%s*&queryFields=label,synonyms&rows=%s&start=1",
                config.getProtocol(), config.getHostName(),partialName, Constants.SEARCH_PAGE_SIZE, page);
        if(ontology != null && !ontology.isEmpty())
            query = String.format("%s://%s/api/search?q=*%s*&queryFields=label,synonyms&rows=%s&start=1&ontology=%s",
                    config.getProtocol(), config.getHostName(),partialName, Constants.SEARCH_PAGE_SIZE, page, ontology);

        SearchQuery termQuery = this.restTemplate.getForObject(query, SearchQuery.class);

        return termQuery;
    }

    public Map<String, String> getTermChildren(String termOBOId, String ontologyID, int distance) throws RestClientException{
        Map<String, String> terms = new HashMap<String, String>();
        Map<String, String> ontologyTerms = getRootTerms(ontologyID);
        if( ontologyTerms == null || ontologyTerms.isEmpty() )
            return terms;
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

    private List<Term> getTermQuery(Href href) throws RestClientException{
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
    public Boolean isObsolete(String termOBOId, String ontologyID) throws RestClientException{
        String query = String.format("%s://%s/api/ontologies/%s/terms?obo_id=%s",
                config.getProtocol(), config.getHostName(),ontologyID, termOBOId);
        TermQuery termQuery = this.restTemplate.getForObject(query, TermQuery.class);
        if(termQuery != null && termQuery.getTerms() != null && termQuery.getTerms().length == 1 &&
           termQuery.getTerms()[0] != null)
            return termQuery.getTerms()[0].isObsolete();

        return null;
    }

    public List<DataHolder> getTermsByAnnotationData(String ontologyID, String annotationType, String strValue) {
       return Collections.EMPTY_LIST;
    }

    public List<DataHolder> getTermsByAnnotationData(String ontologyID, String annotationType, double fromDblValue, double toDblValue) {
        List<Term> terms = getAllOBOTermsFromOntology(ontologyID);
        List<DataHolder> annotationHolders = new ArrayList<DataHolder>();
        for(Term term: terms){
            if(term != null && term.getAnnotation()!= null && term.getAnnotation().containsCrossReference(annotationType)){
                String termValue = term.getAnnotation().getCrossReferenceValue(annotationType);
                if(NumberUtils.isNumber(termValue) && Double.parseDouble(termValue) >= fromDblValue && Double.parseDouble(termValue) <= toDblValue)
                    annotationHolders.add(new DataHolder(Double.parseDouble(termValue),termValue,annotationType,term.getTermOBOId(),term.getLabel()));
            }
        }
        return annotationHolders;
    }

}
