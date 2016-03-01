package uk.ac.pride.ols.web.service.client;

import org.springframework.web.client.RestTemplate;
import uk.ac.pride.ols.web.service.config.AbstractOLSWsConfig;
import uk.ac.pride.ols.web.service.model.Annotation;
import uk.ac.pride.ols.web.service.model.TermQuery;

import java.util.List;
import java.util.Map;


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

    public String getTermById(String termId, String ontologyName) {

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

    public Map<String, String> getOntologyNames() {
        return null;
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
