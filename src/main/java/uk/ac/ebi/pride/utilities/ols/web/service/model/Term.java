package uk.ac.ebi.pride.utilities.ols.web.service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Yasset Perez-Riverol (ypriverol@gmail.com)
 * Creation date 01/03/2016
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Term implements Comparable{

    @JsonProperty("iri")
    Identifier iri;

    @JsonProperty("label")
    String label;

    @JsonProperty("description")
    String[] description;

    @JsonProperty("annotation")
    Annotation annotation;

    @JsonProperty("synonyms")
    String[] synonyms;

    @JsonProperty("ontology_name")
    String ontologyName;

    @JsonProperty("score")
    String score;

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

    @JsonProperty("obo_definition_citation")
    OboDefinitionCitation[] oboDefinitionCitation;

    @JsonProperty("_links")
    Link link;

    @JsonProperty("obo_xref")
    OBOXRef[] oboXRefs;

    @JsonProperty("obo_synonym")
    OBOSynonym[] oboSynonyms;

    public Term() {
    }

    public Term(Identifier iri, String label, String[] description,
                Identifier shortForm, Identifier oboId, String ontologyName, String score, String ontologyIri, boolean definedOntology, OboDefinitionCitation[] oboDefinitionCitation) {
        this.iri = iri;
        this.label = label;
        this.description = description;
        this.shortForm = shortForm;
        this.oboId = oboId;
        this.ontologyName = ontologyName;
        this.oboDefinitionCitation = oboDefinitionCitation;
        this.score = score;
        this.ontologyIri = ontologyIri;
        this.definedOntology = definedOntology;
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

    public String[] getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(String[] synonyms) {
        this.synonyms = synonyms;
    }

    public String getOntologyName() {
        return ontologyName;
    }

    public void setOntologyName(String ontologyName) {
        this.ontologyName = ontologyName;
    }

    public String getScore() { return  score; }

    public void setScore(String score) { this.score = score; }

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

    public Identifier getGlobalId(){
        return (oboId != null)?oboId:shortForm;
    }

    public OBOXRef[] getOboXRefs() {
        return oboXRefs;
    }

    public void setOboXRefs(OBOXRef[] oboXRefs) {
        this.oboXRefs = oboXRefs;
    }

    public OboDefinitionCitation[] getOboDefinitionCitation() {
        return oboDefinitionCitation;
    }

    public void setOboDefinitionCitation(OboDefinitionCitation[] oboDefinitionCitation) {
        this.oboDefinitionCitation = oboDefinitionCitation;
    }

    public boolean containsXref(String annotationType) {
        if(oboXRefs != null && oboXRefs.length > 0){
            for(OBOXRef oboRef: oboXRefs)
                if(oboRef != null && oboRef.getId() != null)
                    if(oboRef.getDatabase().toUpperCase().contains(annotationType.toUpperCase()))
                        return true;
        }
        return false;
    }

    public String getXRefValue(String annotationType) {
        if(oboXRefs != null && oboXRefs.length > 0){
            for(OBOXRef oboRef: oboXRefs)
                if(oboRef != null && oboRef.getDatabase() != null)
                    if(oboRef.getDatabase().toUpperCase().contains(annotationType.toUpperCase()))
                        return oboRef.getDescription();
        }
        return null;
    }

    public Map<String, String> getOboSynonyms(){
        Map<String, String> synonyms = new HashMap<>();
        if(oboSynonyms != null){
            for(OBOSynonym synonym: oboSynonyms)
                if(synonym.getName() != null)
                    synonyms.put(synonym.getName(), synonym.getType());
        }
        return synonyms;
    }

    @Override
    public int compareTo(Object o) {
        Term newTerm = (Term) o;
        if(oboId != null && oboId.getIdentifier() != null
                && newTerm != null && newTerm.getTermOBOId() != null && newTerm.getTermOBOId().getIdentifier() != null)
            return oboId.getIdentifier().compareTo(newTerm.getTermOBOId().getIdentifier());
        else if(shortForm != null && shortForm.getIdentifier() != null
                && newTerm != null && newTerm.getShortForm() != null && newTerm.getTermOBOId().getIdentifier() != null)
            return oboId.getIdentifier().compareTo(newTerm.getTermOBOId().getIdentifier());
        else  if(iri != null && iri.getIdentifier() != null
                && newTerm != null && newTerm.getIri() != null && newTerm.getIri().getIdentifier() != null)
            return iri.getIdentifier().compareTo(newTerm.getIri().getIdentifier());
        return 0;

    }


}
