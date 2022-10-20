package uk.ac.ebi.pride.utilities.ols.web.service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * SearchResult contains summary term information after the query to the ols service.
 *
 * @author Yasset Perez-Riverol (ypriverol@gmail.com)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchResult implements ITerm {

    @JsonProperty("id")
    private String id;

    @JsonProperty("iri")
    private Identifier iri;

    @JsonProperty("short_form")
    private Identifier shortName;

    @JsonProperty("obo_id")
    private Identifier oboId;

    @JsonProperty("label")
    private String name;

    @JsonProperty("description")
    private String[] description;

    @JsonProperty("ontology_name")
    private String ontologyName;

    @JsonProperty("score")
    private String score;

    @JsonProperty("ontology_iri")
    private String ontologyIri;

    @JsonProperty("is_defining_ontology")
    private boolean definedOntology;

    @JsonProperty("is_obsolete")
    private boolean obsolete;

    @JsonProperty("annotation")
    private Annotation annotation;

    @JsonProperty("term_replaced_by")
    private String termReplacedBy;

    @JsonProperty("obo_definition_citation")
    private OboDefinitionCitation[] oboDefinitionCitation;

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

    public Identifier getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = new Identifier(shortName, Identifier.IdentifierType.OWL);
    }

    public Identifier getOboId() {
        return oboId;
    }

    public void setOboId(String oboId) {
        this.oboId = new Identifier(oboId, Identifier.IdentifierType.OBO);
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

    public String getOntologyName() {
        return ontologyName;
    }

    public void setOntologyName(String ontologyName) {
        this.ontologyName = ontologyName;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getOntologyIri() {
        return ontologyIri;
    }

    public void setOntologyIri(String ontologyIri) {
        this.ontologyIri = ontologyIri;
    }

    public boolean getIsDefiningOntology() {
        return definedOntology;
    }

    public boolean isObsolete() {
        return obsolete;
    }

    public void setObsolete(boolean obsolete) {
        this.obsolete = obsolete;
    }

    public Annotation getAnnotation() {
        return annotation;
    }

    public String getTermReplacedBy() {
        return termReplacedBy;
    }

    public void setTermReplacedBy(String termReplacedBy) {
        this.termReplacedBy = termReplacedBy;
    }

    public void setAnnotation(Annotation annotation) {
        this.annotation = annotation;
    }

    public void setIsDefiningOntology(boolean definedOntology) {
        this.definedOntology = definedOntology;
    }

    public OboDefinitionCitation[] getOboDefinitionCitation() {
        return oboDefinitionCitation;
    }

    public void setOboDefinitionCitation(OboDefinitionCitation[] oboDefinitionCitation) {
        this.oboDefinitionCitation = oboDefinitionCitation;
    }

    public Identifier getGlobalId(){
        return (oboId != null)?oboId:shortName;
    }
}
