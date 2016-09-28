package uk.ac.ebi.pride.utilities.ols.web.service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Yasset Perez-Riverol (ypriverol@gmail.com)
 * Creation date 02/03/2016
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Config {

    @JsonProperty("id")
    String id;

    @JsonProperty("versionIri")
    String versionIri;

    @JsonProperty("title")
    String name;

    @JsonProperty("namespace")
    String namespace;

    @JsonProperty("preferredPrefix")
    String preferredPrefix;

    @JsonProperty("description")
    String description;

    @JsonProperty("homepage")
    String homePage;

    @JsonProperty("version")
    String version;

    @JsonProperty("mailingList")
    String mailingList;

    @JsonProperty("creators")
    String[] creators;

    @JsonProperty("annotations")
    Annotation annotations;

    @JsonProperty("fileLocation")
    String fileLocation;

    @JsonProperty("reasonerType")
    String reasonerType;

    @JsonProperty("oboSlims")
    boolean oboLims;

    @JsonProperty("labelProperty")
    String labelProperty;

    @JsonProperty("definitionProperties")
    String[] definitionProperties;

    @JsonProperty("synonymProperties")
    String[] synonymProperties;

    @JsonProperty("hierarchicalProperties")
    String[] hierarchicalProperties;

    @JsonProperty("baseUris")
    String[] baseUris;

    @JsonProperty("hiddenProperties")
    String[] hiddenProperties;

    @JsonProperty("internalMetadataProperties")
    String[] internalMetadataProperties;

    @JsonProperty("skos")
    boolean skos;

    public boolean isSkos() {
        return skos;
    }

    public void setSkos(boolean skos) {
        this.skos = skos;
    }

    public String getVersionIri() {
        return versionIri;
    }

    public void setVersionIri(String versionIri) {
        this.versionIri = versionIri;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getPreferredPrefix() {
        return preferredPrefix;
    }

    public void setPreferredPrefix(String preferredPrefix) {
        this.preferredPrefix = preferredPrefix;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHomePage() {
        return homePage;
    }

    public void setHomePage(String homePage) {
        this.homePage = homePage;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getMailingList() {
        return mailingList;
    }

    public void setMailingList(String mailingList) {
        this.mailingList = mailingList;
    }

    public String[] getCreators() {
        return creators;
    }

    public void setCreators(String[] creators) {
        this.creators = creators;
    }

    public Annotation getAnnotations() {
        return annotations;
    }

    public void setAnnotations(Annotation annotations) {
        this.annotations = annotations;
    }

    public String getFileLocation() {
        return fileLocation;
    }

    public void setFileLocation(String fileLocation) {
        this.fileLocation = fileLocation;
    }

    public String getReasonerType() {
        return reasonerType;
    }

    public void setReasonerType(String reasonerType) {
        this.reasonerType = reasonerType;
    }

    public boolean isOboLims() {
        return oboLims;
    }

    public void setOboLims(boolean oboLims) {
        this.oboLims = oboLims;
    }

    public String getLabelProperty() {
        return labelProperty;
    }

    public void setLabelProperty(String labelProperty) {
        this.labelProperty = labelProperty;
    }

    public String[] getDefinitionProperties() {
        return definitionProperties;
    }

    public void setDefinitionProperties(String[] definitionProperties) {
        this.definitionProperties = definitionProperties;
    }

    public String[] getSynonymProperties() {
        return synonymProperties;
    }

    public void setSynonymProperties(String[] synonymProperties) {
        this.synonymProperties = synonymProperties;
    }

    public String[] getHierarchicalProperties() {
        return hierarchicalProperties;
    }

    public void setHierarchicalProperties(String[] hierarchicalProperties) {
        this.hierarchicalProperties = hierarchicalProperties;
    }

    public String[] getBaseUris() {
        return baseUris;
    }

    public void setBaseUris(String[] baseUris) {
        this.baseUris = baseUris;
    }

    public String[] getHiddenProperties() {
        return hiddenProperties;
    }

    public void setHiddenProperties(String[] hiddenProperties) {
        this.hiddenProperties = hiddenProperties;
    }

    public String[] getInternalMetadataProperties() {
        return internalMetadataProperties;
    }

    public void setInternalMetadataProperties(String[] internalMetadataProperties) {
        this.internalMetadataProperties = internalMetadataProperties;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
