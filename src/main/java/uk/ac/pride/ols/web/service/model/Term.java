package uk.ac.pride.ols.web.service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Yasset Perez-Riverol (ypriverol@gmail.com)
 * @date 01/03/2016
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Term {

    @JsonProperty("iri")
    Identifier iri;

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
    Identifier shortForm;

    @JsonProperty("obo_id")
    Identifier oboId;

    @JsonProperty("_links")
    Link link;

    public Term() {
    }

    public Term(Identifier iri, String label, String[] description, Identifier shortForm, Identifier oboId) {
        this.iri = iri;
        this.label = label;
        this.description = description;
        this.shortForm = shortForm;
        this.oboId = oboId;
    }

    public Identifier getIri() {
        return iri;
    }

    public void setIri(String iri) {
        this.iri = new Identifier(iri, Identifier.IdentifierType.IRI);
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

    public Identifier getShortForm() {
        return shortForm;
    }

    public void setShortForm(String shortForm) {
        this.shortForm = new Identifier(shortForm, Identifier.IdentifierType.OWL);
    }

    public Identifier getTermOBOId() {
        return oboId;
    }

    public void setOboId(String oboId) {
        this.oboId = new Identifier(oboId, Identifier.IdentifierType.OBO);
    }

    public Link getLink() {
        return link;
    }

    public void setLink(Link link) {
        this.link = link;
    }

    public Annotation getAnnotation() {
        return annotation;
    }

    public void setAnnotation(Annotation annotation) {
        this.annotation = annotation;
    }


}
