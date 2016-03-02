package uk.ac.pride.ols.web.service.client;

import org.springframework.web.client.RestTemplate;
import uk.ac.pride.ols.web.service.config.AbstractOLSWsConfig;
import uk.ac.pride.ols.web.service.model.Annotation;
import uk.ac.pride.ols.web.service.model.Ontology;
import uk.ac.pride.ols.web.service.model.OntologyQuery;
import uk.ac.pride.ols.web.service.model.TermQuery;
import uk.ac.pride.ols.web.service.utils.Constants;

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
                config.getProtocol(), config.getHostName(), page, Constants.ontologyPageSize);

        OntologyQuery ontologyQuery = this.restTemplate.getForObject(query, OntologyQuery.class);
        return ontologyQuery;

    }

    public Map<String, String> getAllTermsFromOntology(String ontologyName) {
        return null;
    }

    public Map<String, String> getRootTerms(String ontologyName) {
        return null;
    }

    public Map<String, String> getTermsByName(String partialName, String ontologyName, boolean reverseKeyOrder) {
        return null;
    }

    public Map<String, String> getTermChildren(String termId, String ontologyName, int distance, int[] relationTypes) {
        return null;
    }

    public boolean isObsolete(String termId, String ontologyName) {
        return false;
    }

    public List<Annotation> getTermsByAnnotationData(String ontologyName, String annotationType, String strValue) {
        return null;
    }

    public List<Annotation> getTermsByAnnotationData(String ontologyName, String annotationType, double fromDblValue, double toDblValue) {
        return null;
    }
}
