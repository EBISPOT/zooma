package uk.ac.pride.ols.web.service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Yasset Perez-Riverol (ypriverol@gmail.com)
 * @date 03/03/2016
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchResult {

    @JsonProperty("id")
    String id;

    @JsonProperty("iri")
    String iri;

    @JsonProperty("short_form")
    String short_name;

    @JsonProperty("obo_id")
    String obo_id;

    @JsonProperty("label")
    String name;

    @JsonProperty("description")
    String[] description;

    @JsonProperty("ontology_name")
    String ontology_name;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIri() {
        return iri;
    }

    public void setIri(String iri) {
        this.iri = iri;
    }

    public String getShort_name() {
        return short_name;
    }

    public void setShort_name(String short_name) {
        this.short_name = short_name;
    }

    public String getObo_id() {
        return obo_id;
    }

    public void setObo_id(String obo_id) {
        this.obo_id = obo_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getDescription() {
        return description;
    }

    public void setDescription(String[] description) {
        this.description = description;
    }

    public String getOntology_name() {
        return ontology_name;
    }

    public void setOntology_name(String ontology_name) {
        this.ontology_name = ontology_name;
    }
}
