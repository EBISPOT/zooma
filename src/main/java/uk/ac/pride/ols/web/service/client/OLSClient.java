package uk.ac.pride.ols.web.service.client;



import org.apache.commons.lang.math.NumberUtils;
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
     * This function retrieve the term by the accession of the term in the ontogoly and the id of the ontology
     * if the term is not found it, the NULL is returned.
     *
     * @param termId Term ID in the ontology
     * @param ontologyId The ontology name
     * @return return the name of the Ontology term
     */
    public Term getTermById(Identifier termId, String ontologyId) throws RestClientException {
        if(termId != null && termId.getIdentifier() != null){
            if(termId.getType() == Identifier.IdentifierType.OBO)
                return getTermByOBOId(termId.getIdentifier(), ontologyId);
            else if(termId.getType() == Identifier.IdentifierType.OWL)
                return getTermByShortName(termId.getIdentifier(), ontologyId);
            else if(termId.getType() == Identifier.IdentifierType.IRI)
                return getTermByIRIId(termId.getIdentifier(), ontologyId);
        }
        return null;
    }

    /**
     * Return a Term for an OBO Identifier and the ontology Identifier.
     * @param termOBOId OBO Identifier in OLS
     * @param ontologyId ontology Identifier
     * @return Term
     * @throws RestClientException
     */
    public Term getTermByOBOId(String termOBOId, String ontologyId) throws RestClientException{

        String url = String.format("%s://%s/api/ontologies/%s/terms?obo_id=%s",
                config.getProtocol(), config.getHostName(),ontologyId,termOBOId);

        TermQuery result = this.restTemplate.getForObject(url, TermQuery.class);

        if(result != null && result.getTerms() != null && result.getTerms().length == 1){
            return result.getTerms()[0];
        }

        return null;
    }

    /**
     * Return a Term for a short name Identifier and the ontology Identifier.
     * @param shortTerm short term Identifier in OLS
     * @param ontologyId ontology Identifier
     * @return Term
     * @throws RestClientException
     */
    public Term getTermByShortName(String shortTerm, String ontologyId) throws RestClientException{

        String url = String.format("%s://%s/api/ontologies/%s/terms?short_term=%s",
                config.getProtocol(), config.getHostName(),ontologyId,shortTerm);

        TermQuery result = this.restTemplate.getForObject(url, TermQuery.class);

        if(result != null && result.getTerms() != null && result.getTerms().length == 1){
            return result.getTerms()[0];
        }

        return null;
    }

    /**
     * Return a Term for a short name Identifier and the ontology Identifier.
     * @param iriId short term Identifier in OLS
     * @param ontologyId ontology Identifier
     * @return Term
     * @throws RestClientException
     */
    public Term getTermByIRIId(String iriId, String ontologyId) throws RestClientException{

        String url = String.format("%s://%s/api/ontologies/%s/terms/%s",
                config.getProtocol(), config.getHostName(),ontologyId,iriId);

        TermQuery result = this.restTemplate.getForObject(url, TermQuery.class);

        if(result != null && result.getTerms() != null && result.getTerms().length == 1){
            return result.getTerms()[0];
        }

        return null;
    }

    public List<String> getTermDescription(Identifier termId, String ontologyId) throws RestClientException {
        Term term = getTermById(termId, ontologyId);
        List<String> description = new ArrayList<String>();
        if(term != null && term.getDescription() != null)
            for(String subDescription: term.getDescription())
                if(subDescription != null && !subDescription.isEmpty())
                    description.add(subDescription);
        return description;
    }

    public List<String> getTermComments(Identifier termId, String ontologyId) throws RestClientException {
        return Collections.EMPTY_LIST;
    }

    /**
     * Retrirve all the annotations for an specific term.
     * @param termId Term ID in the ontology
     * @param ontologyId The ontology name
     * @return
     * @throws RestClientException
     */
    public Map<String, List<String>> getAnnotations(Identifier termId, String ontologyId) throws RestClientException {
        Term term = getTermById(termId, ontologyId);
        if(term != null && term.getAnnotation() != null)
            return term.getAnnotation().getAnnotation();
        return null;
    }

    /**
     * This function returns the current ontologies in OLS.
     * @return List
     * @throws RestClientException
     */
    public List<Ontology> getOntologies() throws RestClientException {
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
        return ontologies;
    }

    public List<Term> getTermChildren(Identifier termOBOId, String ontologyId, int distance) throws RestClientException {
        List<Term> terms = new ArrayList<Term>();
        List<Term> ontologyTerms = getRootTerms(ontologyId);
        if( ontologyTerms == null || ontologyTerms.isEmpty() )
            return terms;
        String query = String.format("%s://%s/api/ontologies/%s/terms?obo_id=%s",
                config.getProtocol(), config.getHostName(),ontologyId, termOBOId.getIdentifier());
        TermQuery termQuery = this.restTemplate.getForObject(query, TermQuery.class);

        if(termQuery != null && termQuery.getTerms() != null && termQuery.getTerms().length == 1 &&
                termQuery.getTerms()[0] != null && termQuery.getTerms()[0].getLink() != null &&
                termQuery.getTerms()[0].getLink().getChildrenRef() != null)
            terms = getTermChildrenMap(termQuery.getTerms()[0].getLink().getChildrenRef(), distance);
        return terms;
    }

    public Boolean isObsolete(Identifier termId, String ontologyId) throws RestClientException {
        Term term = getTermById(termId, ontologyId);
        if(term != null)
            return term.isObsolete();
        return null;
    }

    public String searchTermById(String identifier, Identifier.IdentifierType type, String ontologyID) throws RestClientException {
        return null;
    }

    public List<String> getTermDescription(String termId, String ontologyId) throws RestClientException{
        Term term = getTermQueryByOBOId(termId, ontologyId);
        if(term != null)
            return Arrays.asList(term.getDescription());
        return null;
    }

    public List<String> getTermCommnets(String termOBOId, String ontologyId) throws RestClientException{
        return Collections.EMPTY_LIST;

    }

    /**
     * This function returns a Term for an obo ID. If a different ID is provided the function will return
     * NULL value. If the user is interested to use a general identifer it should use the generic
     * getTermById using an Identifier.
     * @param termOBOId obo ontology ID
     * @param ontologyId ontology name
     * @return Term
     */
    public Term getTermQueryByOBOId(String termOBOId, String ontologyId) {
        String url = String.format("%s://%s/api/ontologies/%s/terms?obo_id=%s",
                config.getProtocol(), config.getHostName(),ontologyId,termOBOId);

        TermQuery result = this.restTemplate.getForObject(url, TermQuery.class);
        if(result != null && result.getTerms() != null && result.getTerms().length == 1 && result.getTerms()[0] != null)
            return result.getTerms()[0];
        return null;
    }

    public Map<String, String> getTermXrefs(String termId, String ontologyId) throws RestClientException{
        return null;
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
    public List<Term> getAllTermsFromOntology(String ontologyID) throws RestClientException{
        return getAllOBOTermsFromOntology(ontologyID);
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

    /**
     * Return all Root Terms for an specific ontology including.
     * @param ontologyID ontology Id to be search
     * @return List of Term
     */
    public List<Term> getRootTerms(String ontologyID) {
        List<Term> terms = getAllOBOTermsFromOntology(ontologyID);
        List<Term> resultTerms = new ArrayList<Term>();
        for(Term term: terms)
            if(term != null && term.isRoot())
                resultTerms.add(term);
        return resultTerms;
    }

    /**
     * This function retrieve all the terms from an specific ontology and perform a search in the client side.
     * In the future would be great to repleace the current functionality with the search capabilities in the ols.
     * @param partialName Substring to lookup in the name term
     * @param ontologyID
     * @param reverseKeyOrder sort the hash in a reverse order
     * @return
     */
    public List<Term> getTermsByName(String partialName, String ontologyID, boolean reverseKeyOrder) {
        List<Term> resultTerms = new ArrayList<Term>();
        if(partialName == null || partialName.isEmpty())
            return Collections.EMPTY_LIST;

        if(ontologyID == null || ontologyID.isEmpty())
            resultTerms = searchByPartialTerm(partialName, null);
        else
            resultTerms = searchByPartialTerm(partialName, ontologyID);

        if(reverseKeyOrder){
            Set<Term> newMap = new TreeSet<Term>(Collections.reverseOrder());
            newMap.addAll(resultTerms);
            resultTerms = new ArrayList<Term>(newMap);
        }
        return resultTerms;
    }

    private List<Term> searchByPartialTerm(String partialName, String ontology) throws RestClientException{
        List<Term> termResults = new ArrayList<Term>();
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
        for(int i = 0; i < terms.size(); i++)
             if(terms.get(i).getObo_id() != null && terms.get(i).getName() != null){
                 SearchResult termResult = terms.get(i);
                 termResults.add(new Term(termResult.getIri(), termResult.getName(), termResult.getDescription(), termResult.getShort_name(), termResult.getObo_id()));
             }

        return termResults;
    }

    private SearchQuery getSearchQuery(int page, String partialName, String ontology) throws RestClientException{
        String query = String.format("%s://%s/api/search?q=*%s*&queryFields=label,synonyms&rows=%s&start=%s",
                config.getProtocol(), config.getHostName(),partialName, Constants.SEARCH_PAGE_SIZE, page);
        if(ontology != null && !ontology.isEmpty())
            query = String.format("%s://%s/api/search?q=*%s*&queryFields=label,synonyms&rows=%s&start=%s&ontology=%s",
                    config.getProtocol(), config.getHostName(),partialName, Constants.SEARCH_PAGE_SIZE, page, ontology);

        SearchQuery termQuery = this.restTemplate.getForObject(query, SearchQuery.class);

        return termQuery;
    }

    private List<Term> getTermChildrenMap(Href childrenHRef, int distance){
        List<Term> children = new ArrayList<Term>();
        if(distance == 0)
            return Collections.EMPTY_LIST;
        List<Term> childTerms = getTermChildren(childrenHRef, distance);
        for(Term term: childTerms)
            children.add(term);
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
                    annotationHolders.add(new DataHolder(Double.parseDouble(termValue),termValue,annotationType,term.getTermOBOId().getIdentifier(),term.getLabel()));
            }
        }
        return annotationHolders;
    }

    public String searchTermByOBOId(String partialOBOId, String ontologyID) throws RestClientException {
//        Map<String, String> termResults = new HashMap<String, String>();
//        SearchQuery currentTermQuery = getSearchOBOIDQuery(0, partialOBOId, ontologyID);
//            List<SearchResult> terms = new ArrayList<SearchResult>();
//            if(currentTermQuery != null && currentTermQuery.getResponse() != null && currentTermQuery.getResponse().getSearchResults() != null){
//                terms.addAll(Arrays.asList(currentTermQuery.getResponse().getSearchResults()));
//                if(currentTermQuery.getResponse().getSearchResults().length < currentTermQuery.getResponse().getNumFound()){
//                    for(int i = 1; i < currentTermQuery.getResponse().getNumFound()/currentTermQuery.getResponse().getSearchResults().length + 1; i++){
//                        SearchQuery termQuery = getSearchOBOIDQuery(i, partialOBOId, ontologyID);
//                        if(termQuery != null && termQuery.getResponse() != null && termQuery.getResponse().getSearchResults() != null)
//                            terms.addAll(Arrays.asList(termQuery.getResponse().getSearchResults()));
//                    }
//                }
//            }
//            for(SearchResult result: terms)
//                if(result.getObo_id() != null && result.getName() != null)
//                    termResults.put(result.getObo_id(), result.getName());
//            return termResults.;
        return null;
        }

    private SearchQuery getSearchOBOIDQuery(int page, String partialName, String ontology) throws RestClientException{
        String query = String.format("%s://%s/api/search?q=*%s*&queryFields=label,synonyms&rows=%s&start=1",
                config.getProtocol(), config.getHostName(),partialName, Constants.SEARCH_PAGE_SIZE, page);
        if(ontology != null && !ontology.isEmpty())
            query = String.format("%s://%s/api/search?q=*%s*&queryFields=label,synonyms&rows=%s&start=1&ontology=%s",
                    config.getProtocol(), config.getHostName(),partialName, Constants.SEARCH_PAGE_SIZE, page, ontology);

        SearchQuery termQuery = this.restTemplate.getForObject(query, SearchQuery.class);

        return termQuery;
    }

}
