package uk.ac.ebi.pride.utilities.ols.web.service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Yasset Perez-Riverol (ypriverol@gmail.com)
 * Creation date 02/03/2016
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class Ontology {

    @JsonProperty("loaded")
    private String loadedDate;

    @JsonProperty("updated")
    private String updatedDate;

    @JsonProperty("status")
    private String status;

    @JsonProperty("message")
    private String message;

    @JsonProperty("version")
    private String version;

    @JsonProperty("numberOfTerms")
    private int numberOfTerms;

    @JsonProperty("numberOfProperties")
    private int numberOfProperties;

    @JsonProperty("numberOfIndividuals")
    private int numberOfIndividuals;

    @JsonProperty("config")
    private Config config;

    @JsonProperty("_links")
    private Link link;


    public String getLoadedDate() {
        return loadedDate;
    }

    public void setLoadedDate(String loadedDate) {
        this.loadedDate = loadedDate;
    }

    public String getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(String updatedDate) {
        this.updatedDate = updatedDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getNumberOfTerms() {
        return numberOfTerms;
    }

    public void setNumberOfTerms(int numberOfTerms) {
        this.numberOfTerms = numberOfTerms;
    }

    public int getNumberOfProperties() {
        return numberOfProperties;
    }

    public void setNumberOfProperties(int numberOfProperties) {
        this.numberOfProperties = numberOfProperties;
    }

    public int getNumberOfIndividuals() {
        return numberOfIndividuals;
    }

    public void setNumberOfIndividuals(int numberOfIndividuals) {
        this.numberOfIndividuals = numberOfIndividuals;
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public Link getLink() {
        return link;
    }

    public void setLink(Link link) {
        this.link = link;
    }

    public String getName(){
        if(config != null)
            return config.getName();
        return null;
    }

    public void setName(String name){
        if(config != null)
            config.setName(name);
    }

    public String getNamespace(){
        if(config != null)
            return config.getNamespace();
        return null;
    }

    public String getId(){
        if(config != null)
            return config.getId();
        return null;
    }

    public String getDescription(){
        if(config != null){
            return  config.getDescription();
        }
        return null;
    }

    public Map<String, String> getAnnotations() {
        Map<String, String> annotations = new HashMap<>();
        if(config != null && config.getAnnotations() != null){
            for(String annotation: config.getAnnotations().annotation.keySet()){
                StringBuilder globalAnnotation = new StringBuilder();
                for(String subAnnotation: config.getAnnotations().getAnnotation().get(annotation)){
                    globalAnnotation.append(subAnnotation).append("\n");
                }
                annotations.put(annotation, globalAnnotation.toString());
            }
        }
        return annotations;
    }

    @Override
    public String toString() {
        return "Ontology{" +
                "loadedDate='" + loadedDate + '\'' +
                ", updatedDate='" + updatedDate + '\'' +
                ", status='" + status + '\'' +
                ", message='" + message + '\'' +
                ", version='" + version + '\'' +
                ", numberOfTerms=" + numberOfTerms +
                ", numberOfProperties=" + numberOfProperties +
                ", numberOfIndividuals=" + numberOfIndividuals +
                ", config=" + config +
                ", link=" + link +
                '}';
    }
}
