package uk.ac.ebi.pride.utilities.ols.web.service.model;

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
    Identifier iri;

    @JsonProperty("short_form")
    Identifier short_name;

    @JsonProperty("obo_id")
    Identifier obo_id;

    @JsonProperty("label")
    String name;

    @JsonProperty("description")
    String[] description;

    @JsonProperty("ontology_name")
    String ontology_name;

    @JsonProperty("obo_definition_citation")
    OboDefinitionCitation[] oboDefinitionCitation;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Identifier getIri() {
        return iri;
    }

    public void setIri(String iri) {
        this.iri = new Identifier(iri, Identifier.IdentifierType.IRI);
    }

    public Identifier getShort_name() {
        return short_name;
    }

    public void setShort_name(String short_name) {
        this.short_name = new Identifier(short_name, Identifier.IdentifierType.OWL);
    }

    public Identifier getObo_id() {
        return obo_id;
    }

    public void setObo_id(String obo_id) {
        this.obo_id = new Identifier(obo_id, Identifier.IdentifierType.OBO);
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

    public OboDefinitionCitation[] getOboDefinitionCitation() {
        return oboDefinitionCitation;
    }

    public void setOboDefinitionCitation(OboDefinitionCitation[] oboDefinitionCitation) {
        this.oboDefinitionCitation = oboDefinitionCitation;
    }
}
