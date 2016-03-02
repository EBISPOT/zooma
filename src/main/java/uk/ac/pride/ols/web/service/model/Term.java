package uk.ac.pride.ols.web.service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sun.javafx.collections.MappingChange;

import java.util.Map;

/**
 * @author Yasset Perez-Riverol (ypriverol@gmail.com)
 * @date 01/03/2016
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Term {

    @JsonProperty("iri")
    String iri;

    @JsonProperty("label")
    String label;

    @JsonProperty("description")
    String[] description;

    @JsonProperty("annotation")
    Annotation annotation;

    @JsonProperty("sysnonyms")
    String synonyms;

    @JsonProperty("ontology_name")
    String ontologyName;

    @JsonProperty("ontology_prefix")
    String ontologyPrefix;

    @JsonProperty("ontology_iri")
    String ontologyIri;

    @JsonProperty("is_obsolete")
    boolean obsolete;

    @JsonProperty("is_defining_ontology")
    boolean definedOntology;

    @JsonProperty("has_children")
    boolean hasChildren;

    @JsonProperty("is_root")
    boolean root;

    @JsonProperty("short_form")
    String shortForm;

    @JsonProperty("obo_id")
    String termId;

    @JsonProperty("_links")
    Link link;

    public String getIri() {
        return iri;
    }

    public void setIri(String iri) {
        this.iri = iri;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String[] getDescription() {
        return description;
    }

    public void setDescription(String[] description) {
        this.description = description;
    }

    public String getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(String synonyms) {
        this.synonyms = synonyms;
    }

    public String getOntologyName() {
        return ontologyName;
    }

    public void setOntologyName(String ontologyName) {
        this.ontologyName = ontologyName;
    }

    public String getOntologyPrefix() {
        return ontologyPrefix;
    }

    public void setOntologyPrefix(String ontologyPrefix) {
        this.ontologyPrefix = ontologyPrefix;
    }

    public String getOntologyIri() {
        return ontologyIri;
    }

    public void setOntologyIri(String ontologyIri) {
        this.ontologyIri = ontologyIri;
    }

    public boolean isObsolete() {
        return obsolete;
    }

    public void setObsolete(boolean obsolete) {
        this.obsolete = obsolete;
    }

    public boolean isDefinedOntology() {
        return definedOntology;
    }

    public void setDefinedOntology(boolean definedOntology) {
        this.definedOntology = definedOntology;
    }

    public boolean isHasChildren() {
        return hasChildren;
    }

    public void setHasChildren(boolean hasChildren) {
        this.hasChildren = hasChildren;
    }

    public boolean isRoot() {
        return root;
    }

    public void setRoot(boolean root) {
        this.root = root;
    }

    public String getShortForm() {
        return shortForm;
    }

    public void setShortForm(String shortForm) {
        this.shortForm = shortForm;
    }

    public String getTermOBOId() {
        return termId;
    }

    public void setTermId(String termId) {
        this.termId = termId;
    }

    public Link getLink() {
        return link;
    }

    public void setLink(Link link) {
        this.link = link;
    }
}
