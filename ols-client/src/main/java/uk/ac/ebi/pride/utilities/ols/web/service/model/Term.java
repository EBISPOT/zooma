package uk.ac.ebi.pride.utilities.ols.web.service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Yasset Perez-Riverol (ypriverol@gmail.com)
 * Creation date 01/03/2016
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Term implements Comparable, ITerm {

    @JsonProperty("iri")
    private Identifier iri;

    @JsonProperty("label")
    private String label;

    @JsonProperty("description")
    private String[] description;

    @JsonProperty("annotation")
    private Annotation annotation;

    @JsonProperty("synonyms")
    private String[] synonyms;

    @JsonProperty("ontology_name")
    private String ontologyName;

    @JsonProperty("score")
    private String score;

    @JsonProperty("ontology_prefix")
    private String ontologyPrefix;

    @JsonProperty("ontology_iri")
    private String ontologyIri;

    @JsonProperty("is_defining_ontology")
    private boolean definedOntology;

    @JsonProperty("has_children")
    private boolean hasChildren;

    @JsonProperty("is_root")
    private boolean root;

    @JsonProperty("short_form")
    private
    Identifier shortForm;

    @JsonProperty("obo_id")
    private Identifier oboId;

    @JsonProperty("obo_definition_citation")
    private OboDefinitionCitation[] oboDefinitionCitation;

    @JsonProperty("_links")
    private Link link;

    @JsonProperty("obo_xref")
    private OBOXRef[] oboXRefs;

    @JsonProperty("obo_synonym")
    private OBOSynonym[] oboSynonyms;

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
        this.score = score;
        this.ontologyIri = ontologyIri;
        this.definedOntology = definedOntology;
        this.oboDefinitionCitation = oboDefinitionCitation;
    }

    public Term(Identifier iri, String label, String[] description,
                Identifier shortForm, Identifier oboId, String ontologyName, String score, String ontologyIri,
                boolean definedOntology,
                OboDefinitionCitation[] oboDefinitionCitation,
                Annotation annotation) {
        this.iri = iri;
        this.label = label;
        this.description = description;
        this.shortForm = shortForm;
        this.oboId = oboId;
        this.ontologyName = ontologyName;
        this.score = score;
        this.ontologyIri = ontologyIri;
        this.definedOntology = definedOntology;
        this.oboDefinitionCitation = oboDefinitionCitation;
        this.annotation = annotation;
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

    /**
     * Get all synonyms for an specific ontology.
     * @return Map of Synonyms.
     */
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

    @Override
    public String toString() {
        return "Term{" +
                "iri=" + iri +
                ", label='" + label + '\'' +
                ", oboId=" + oboId +
                ", shortForm=" + shortForm +
                ", description=" + Arrays.toString(description) +
                '}';
    }

    @Override
    public String getName() {
        return getLabel();
    }

    @Override
    public Identifier getShortName() {
        return shortForm;
    }

    @Override
    public Identifier getOboId() {
        return oboId;
    }
}
