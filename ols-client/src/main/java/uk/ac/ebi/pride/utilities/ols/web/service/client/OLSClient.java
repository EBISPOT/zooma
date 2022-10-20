package uk.ac.ebi.pride.utilities.ols.web.service.client;

import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.pride.utilities.ols.web.service.config.AbstractOLSWsConfig;
import uk.ac.ebi.pride.utilities.ols.web.service.utils.Constants;
import uk.ac.ebi.pride.utilities.ols.web.service.model.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;


/**
 * This class allows to query the Ontology Lockup service to retrirve information about
 * CVTerms.
 *
 * @author ypriverol
 */
public class OLSClient implements Client {

    private  RestTemplate restTemplate;
    private  AbstractOLSWsConfig config;

    private String queryField;
    private String fieldList;
    private int searchPageSize;
    private int searchPageNum;

    private static int TIME_OUT = 5000;

    private static final String DEFAULT_QUERY_FIELD = new QueryFields.QueryFieldBuilder()
            .setLabel()
            .setSynonym()
            .build()
            .toString();
    private static final String DEFAULT_FIELD_LIST = new FieldList.FieldListBuilder()
            .setLabel()
            .setIri()
            .setScore()
            .setOntologyName()
            .setOboId()
            .setOntologyIri()
            .setIsDefiningOntology()
            .setShortForm()
            .setOntologyPrefix()
            .setDescription()
            .setType()
            .build()
            .toString();

    public String getQueryField() {
        if (queryField == null){
            queryField = DEFAULT_QUERY_FIELD;
        }
        return queryField;
    }

    public void setQueryField(String queryField) {
        this.queryField = queryField;
    }

    public String getFieldList() {
        if (fieldList == null){
            fieldList = DEFAULT_FIELD_LIST;
        }
        return fieldList;
    }

    // Zooma
    public int getSearchPageSize() {
        return searchPageSize;
    }

    // Zooma
    public void setSearchPageSize(int searchPageSize) {
        this.searchPageSize = searchPageSize;
    }

    // Zooma
    public int getSearchPageNum() {
        return searchPageNum;
    }

    // Zooma
    public void setSearchPageNum(int searchPageNum) {
        this.searchPageNum = searchPageNum;
    }

    public void setFieldList(String fieldList) {
        this.fieldList = fieldList;
    }

    org.slf4j.Logger logger = LoggerFactory.getLogger(OLSClient.class);


    /**
     * Default constructor for Archive clients
     *
     * @param config configuration to use.
     */
    public OLSClient(AbstractOLSWsConfig config) {
        this.config = config;
        this.restTemplate = new RestTemplate();
        this.restTemplate = new RestTemplate();
        this.searchPageSize = Constants.SEARCH_PAGE_SIZE;
        this.searchPageNum = -1;
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


    /**
     * This function returns the current ontologies in OLS.
     *
     * @return List
     * @throws RestClientException if there are problems connecting to the REST service.
     */
    // Zooma
    public List<Ontology> getOntologies() throws RestClientException {
        OntologyQuery currentOntologyQuery = getOntologyQuery(0);
        List<Ontology> ontologies = new ArrayList<>();
        ontologies.addAll(Arrays.asList(currentOntologyQuery.getOntolgoies()));
        if (currentOntologyQuery != null) {
            if (currentOntologyQuery.getOntolgoies().length < currentOntologyQuery.getPage().getTotalElements()) {
                for (int i = 1; i < currentOntologyQuery.getPage().getTotalElements() / currentOntologyQuery.getOntolgoies().length + 1; i++) {
                    OntologyQuery ontologyQuery = getOntologyQuery(i);
                    if (ontologyQuery != null && ontologyQuery.getOntolgoies() != null)
                        ontologies.addAll(Arrays.asList(ontologyQuery.getOntolgoies()));
                }
            }
        }
        return ontologies;
    }

    // Zooma
    public Ontology getOntologyFromId(URI id){
        List<Ontology> ontologyList = getOntologies();
        for (Ontology ontology : ontologyList){
            logger.debug(ontology.getConfig().getId());
            if (ontology.getConfig().getId().equals(id.toString())){
                return ontology;
            }
        }
        return null;
    }

    // Zooma
    private OntologyQuery getOntologyQuery(int page) throws RestClientException {
        String query = String.format("page=%s&size=%s",
                page, Constants.ONTOLOGY_PAGE_SIZE);
        logger.debug(query);
        URI uri = encodeURL("/api/ontologies", query);
        return this.restTemplate.getForObject(uri, OntologyQuery.class);
    }

    // Zooma
    public List<Term> getTermsByName(String partialName, String ontologyID, boolean reverseKeyOrder) {
        return getTermsByName(partialName, ontologyID, reverseKeyOrder, null);
    }

    // Zooma
    public List<Term> getTermsByNameFromParent(String partialName, String ontologyID, boolean reverseKeyOrder, String childrenOf) {
        return  getTermsByName(partialName, ontologyID, reverseKeyOrder, childrenOf);
    }

    /**
     * This function retrieve all the terms from an specific ontology and perform a search in the client side.
     * In the future would be great to replace the current functionality with the search capabilities in the ols.
     *
     * @param partialName     Substring to lookup in the name term
     * @param ontologyID the ontology ID.
     * @param reverseKeyOrder sort the hash in a reverse order
     * @return list of Terms.
     */

    // Zooma
    private List<Term> getTermsByName(String partialName, String ontologyID, boolean reverseKeyOrder, String childrenOf) {
        List<Term> resultTerms;
        if (partialName == null || partialName.isEmpty())
            return Collections.emptyList();

        resultTerms = searchByPartialTerm(partialName, ontologyID, childrenOf);

        if (reverseKeyOrder) {
            Set<Term> newMap = new TreeSet<>(Collections.reverseOrder());
            newMap.addAll(resultTerms);
            resultTerms = new ArrayList<>(newMap);
        }
        return resultTerms;
    }

    /**
     * Searches for exact term matches that belong to a specific parent.
     * You can restrict a search to children of a given term.
     *
     * @param exactName the term we are looking for
     * @param ontologyId the ontology that the term belongs to
     * @param childrenOf a list of IRI for the terms that you want to search under, comma separated
     * @return a list of Terms found
     */
    // Zooma
    public List<Term> getExactTermsByNameFromParent(String exactName, String ontologyId, String childrenOf) {
        return  getExactTermsByName(exactName, ontologyId, childrenOf);
    }

    // Zooma
    public List<Term> getExactTermsByName(String exactName, String ontologyId) {
        return getExactTermsByName(exactName, ontologyId, null);
    }

    // Zooma
    private List<Term> getExactTermsByName(String exactName, String ontologyId, String childrenOf) {

        if (exactName == null || exactName.isEmpty()){
            return null;
        }

        return  searchByExactTerm(exactName, ontologyId, childrenOf);

    }


    // Zooma
    public List<Term> getExactTermsByIriString(String iri) {
        String customQueryField = new QueryFields.QueryFieldBuilder()
                .setIri()
                .build()
                .toString();
        this.setQueryField(customQueryField);
        List<Term> terms = getExactTermsByName(iri, null);
        //restore olsClient search to it's default query field and field list
        this.setQueryField(DEFAULT_QUERY_FIELD);
        return terms;
    }


    private List<Term> searchByPartialTerm(String partialName, String ontology, String childrenOf) throws RestClientException {
        return searchByTerm(partialName, ontology, false, childrenOf, false);
    }

    private List<Term> searchByExactTerm(String exactName, String ontologyId, String childrenOf) throws RestClientException {
        return searchByTerm(exactName, ontologyId, true, childrenOf, false);
    }

    /**
     * Searches for terms in the OLS
     *
     * @param termToSearch the name of the term (partial or exact) that we want to find
     * @param ontology  optional ontology to search the term in, null if not specified
     * @param exact true if we want an exact string match
     * @param childrenOf will restrict a search to children of a given term.
     *                   Supply a list of IRI for the terms that you want to search under, comma separated
     * @param obsolete  true if you want to look into obsolete terms
     * @return a list of Terms found
     * @throws RestClientException Rest Exception
     */
    private List<Term> searchByTerm(String termToSearch, String ontology, boolean exact, String childrenOf, boolean obsolete) throws RestClientException {
        List<Term> termResults = new ArrayList<>();
        List<SearchResult> terms = new ArrayList<>();

        int pageSize = getSearchPageSize();
        if(pageSize <= 0){
            pageSize = Constants.SEARCH_PAGE_SIZE;
        }

        SearchQuery currentTermQuery = getSearchQuery(0, termToSearch, ontology, exact, childrenOf, obsolete, pageSize);

        int pageNum = getSearchPageNum();
        if (pageNum < 0){
            pageNum = new Integer(currentTermQuery.getResponse().getNumFound() / pageSize);
        }

        if (currentTermQuery != null && currentTermQuery.getResponse() != null && currentTermQuery.getResponse().getSearchResults() != null) {
            terms.addAll(Arrays.asList(currentTermQuery.getResponse().getSearchResults()));
            if (currentTermQuery.getResponse().getSearchResults().length < currentTermQuery.getResponse().getNumFound()) {
                int start = 0;
                for(int i = 0; i < pageNum; i++){
                    start = start + pageSize;
                    SearchQuery termQuery = getSearchQuery(start, termToSearch, ontology, exact, childrenOf, obsolete, pageSize);
                    if (termQuery != null && termQuery.getResponse() != null && termQuery.getResponse().getSearchResults() != null){
                        if(termQuery.getResponse().getSearchResults().length == 0) {
                            break;
                        }
                        terms.addAll(Arrays.asList(termQuery.getResponse().getSearchResults()));
                    }
                }
            }
        }
        for (SearchResult term : terms)
            if (term.getName() != null) {
                termResults.add(new Term(term.getIri(), term.getName(), term.getDescription(),
                        term.getShortName(),
                        term.getOboId(),
                        term.getOntologyName(),
                        term.getScore(),
                        term.getOntologyIri(),
                        term.getIsDefiningOntology(),
                        term.getOboDefinitionCitation()));
            }

        return termResults;
    }

    /**
     * Retrieves a specific term given its iri as a String and the ontology it belongs to
     *
     * @param id the term id, whether it is obo, short or iri i.e. http://www.ebi.ac.uk/efo/EFO_0000635, EFO_0000635 or EFO:0000635
     * @param ontology the ontology the term belongs to, i.e. efo
     * @return the Term we are looking for. Can also be an ObsoleteTerm
     * @throws RestClientException Rest Exception
     */
    public Term retrieveTerm(String id, String ontology) throws RestClientException {
        RetrieveTermQuery currentTermQuery = getRetrieveQuery(id, ontology);

        List<SearchResult> terms = new ArrayList<>();
        if (currentTermQuery != null && currentTermQuery.getResponse() != null && currentTermQuery.getResponse().getSearchResults() != null) {
            terms.addAll(Arrays.asList(currentTermQuery.getResponse().getSearchResults()));
        }
        Term term = null;
        for (SearchResult term1 : terms)
            if (term1.getName() != null) {
                if (term1.isObsolete()) {
                    term = new ObsoleteTerm(term1.getIri(), term1.getName(), term1.getDescription(),
                            term1.getShortName(),
                            term1.getOboId(),
                            term1.getOntologyName(),
                            term1.getScore(),
                            term1.getOntologyIri(),
                            term1.getIsDefiningOntology(),
                            term1.getOboDefinitionCitation(),
                            term1.getAnnotation(),
                            true, term1.getTermReplacedBy());
                } else {
                    term = new Term(term1.getIri(), term1.getName(), term1.getDescription(),
                            term1.getShortName(),
                            term1.getOboId(),
                            term1.getOntologyName(),
                            term1.getScore(),
                            term1.getOntologyIri(),
                            term1.getIsDefiningOntology(),
                            term1.getOboDefinitionCitation(),
                            term1.getAnnotation());
                }
            }

            if(ontology != null && !ontology.isEmpty() && term != null && term.getOntologyName() != null){
                if(!term.getOntologyName().toLowerCase().equals(ontology)){
                    return null;
                }
            }
        return term;
    }

    /**
     * Retrieves a specific term given its id as a String
     * If it is obsolete then it will be an ObsoleteTerm
     *
     * @param id the term id, whether it is obo, shor or iri i.e. http://www.ebi.ac.uk/efo/EFO_0000635, EFO_0000635 or EFO:0000635
     * @return the an ObsoleteTerm we are looking for or null if not obsolete
     * @throws RestClientException Rest Exception
     */
    // Zooma
    public ObsoleteTerm retrieveObsoleteTerm(String id) throws RestClientException {
        RetrieveTermQuery currentTermQuery = getRetrieveQuery(id);

        List<SearchResult> terms = new ArrayList<>();
        if (currentTermQuery != null && currentTermQuery.getResponse() != null && currentTermQuery.getResponse().getSearchResults() != null) {
            terms.addAll(Arrays.asList(currentTermQuery.getResponse().getSearchResults()));
        }
        ObsoleteTerm term = null;
        for (SearchResult term1 : terms) {
            if (term1.getName() != null) {
                if (term1.isObsolete()) {
                    if (term1.getIsDefiningOntology()) {
                        return new ObsoleteTerm(term1.getIri(), term1.getName(), term1.getDescription(),
                                term1.getShortName(),
                                term1.getOboId(),
                                term1.getOntologyName(),
                                term1.getScore(),
                                term1.getOntologyIri(),
                                term1.getIsDefiningOntology(),
                                term1.getOboDefinitionCitation(),
                                term1.getAnnotation(),
                                true, term1.getTermReplacedBy());
                    }
                }
            }
        }
        return null;
    }

    /**
     * If the given term is obsolete and has a reference to it's replacement
     return the replacement

     * @param termId the String of the id of the term we are looking for a replacement for
     * @return the term to be replaced with, null if nothing found
     */
    // Zooma
    public Term getReplacedBy(String termId){
        ObsoleteTerm term = retrieveObsoleteTerm(termId);
        String termReplacedBy = null;
        if(term != null) {
            termReplacedBy = term.getTermReplacedBy();
        }
        if(termReplacedBy == null || termReplacedBy.isEmpty()){
            return null;
        }
        return retrieveTerm(termReplacedBy.trim(), term.getOntologyName());
    }

    /**
     * Raw query to OLS Service
     * @param page number of the page
     * @param name term to be query
     * @param ontology ontology name
     * @param exactMatch if is exact match
     * @param childrenOf including children
     * @param obsolete including obsolete terms
     * @param size size of the query
     * @return Return results
     * @throws RestClientException RestClient Exception
     */
    public SearchQuery getSearchQuery(int page, String name, String ontology, boolean exactMatch, String childrenOf, boolean obsolete, int size) throws RestClientException {
        String query;

        query = String.format("q=%s&" +
                        this.getQueryField()
                        + "&rows=%s&start=%s&"
                        + this.getFieldList() ,
                name, size, page);

        query += "&type=class";

        if (ontology != null && !ontology.isEmpty())
            query += "&ontology=" + ontology;

        if(exactMatch){
            query += "&exact=true";
        }

        if (childrenOf != null && !childrenOf.isEmpty())
            query += "&childrenOf=" + childrenOf;

        if (obsolete)
            query += "&obsoletes=true";

        logger.debug(query);
        URI uri = encodeURL("/api/search", query);
        return this.restTemplate.getForObject(uri, SearchQuery.class);
    }


    private RetrieveTermQuery getRetrieveQuery(String id, String ontology) throws RestClientException {

        if(ontology == null || ontology.isEmpty()){
            throw new IllegalArgumentException("The term's ontology must not be null or empty!");
        }

        String iri = null;
        if(!id.contains("http") && !id.contains("/")){
             iri = resolveIri(id);
        }

        if (iri == null){
            iri = id;
        }

        String query = String.format("iri=%s",
                iri);

        logger.debug(query);
        URI uri = encodeURL("/api/ontologies/" + ontology + "/terms", query);
        return this.restTemplate.getForObject(uri, RetrieveTermQuery.class);
    }

    private RetrieveTermQuery getRetrieveQuery(String id) throws RestClientException {
        String query;

        query = String.format("id=%s",
                id);

        logger.debug(query);
        URI uri = encodeURL("/api/terms", query);
        return this.restTemplate.getForObject(uri, RetrieveTermQuery.class);
    }

    private String resolveIri(String id) throws RestClientException {
        RetrieveTermQuery currentTermQuery = getRetrieveQuery(id);
        List<SearchResult> terms = new ArrayList<>();
        if (currentTermQuery != null && currentTermQuery.getResponse() != null && currentTermQuery.getResponse().getSearchResults() != null) {
            terms.addAll(Arrays.asList(currentTermQuery.getResponse().getSearchResults()));
        }

        for (SearchResult term : terms) {
            if (term.getName() != null) {
                return term.getIri().getIdentifier();
            }
        }
        return null;
    }

    // Zooma
    public Ontology getOntology(String ontologyId) throws RestClientException {
        URI uri = encodeURL("/api/ontologies/" + ontologyId, null);
        Ontology ontology = this.restTemplate.getForObject(uri, Ontology.class);
        if (ontology != null) {
            return ontology;
        }
        return null;
    }

    private URI encodeURL(String path, String query){
        URI uri;
        try {
            String hostname = config.getHostName().split("/")[0]; //e.g. www.ebi.ac.uk
            String hostnamePath = config.getHostName().split("/")[1]; //e.g. ols
            uri = new URI(config.getProtocol(), hostname, "/" + hostnamePath + path, query, null);
            return uri;
        } catch (URISyntaxException e){
            throw new RestClientException("The query could not be encoded");
        }
    }
}