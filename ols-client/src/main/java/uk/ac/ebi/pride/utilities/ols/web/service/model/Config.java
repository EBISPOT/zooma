package uk.ac.ebi.pride.utilities.ols.web.service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;

/**
 * @author Yasset Perez-Riverol (ypriverol@gmail.com)
 * Creation date 02/03/2016
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Config {

    @JsonProperty("id")
    private String id;

    @JsonProperty("versionIri")
    private String versionIri;

    @JsonProperty("title")
    private String name;

    @JsonProperty("namespace")
    private String namespace;

    @JsonProperty("preferredPrefix")
    private String preferredPrefix;

    @JsonProperty("description")
    private String description;

    @JsonProperty("homepage")
    private String homePage;

    @JsonProperty("version")
    private String version;

    @JsonProperty("mailingList")
    private String mailingList;

    @JsonProperty("creators")
    private String[] creators;

    @JsonProperty("annotations")
    private Annotation annotations;

    @JsonProperty("fileLocation")
    private String fileLocation;

    @JsonProperty("reasonerType")
    private
    String reasonerType;

    @JsonProperty("oboSlims")
    private
    boolean oboLims;

    @JsonProperty("labelProperty")
    private
    String labelProperty;

    @JsonProperty("definitionProperties")
    private
    String[] definitionProperties;

    @JsonProperty("synonymProperties")
    private
    String[] synonymProperties;

    @JsonProperty("hierarchicalProperties")
    private
    String[] hierarchicalProperties;

    @JsonProperty("baseUris")
    private
    String[] baseUris;

    @JsonProperty("hiddenProperties")
    private
    String[] hiddenProperties;

    @JsonProperty("internalMetadataProperties")
    private
    String[] internalMetadataProperties;

    @JsonProperty("skos")
    private
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

    @Override
    public String toString() {
        return "Config{" +
                "id='" + id + '\'' +
                ", versionIri='" + versionIri + '\'' +
                ", name='" + name + '\'' +
                ", namespace='" + namespace + '\'' +
                ", preferredPrefix='" + preferredPrefix + '\'' +
                ", description='" + description + '\'' +
                ", homePage='" + homePage + '\'' +
                ", version='" + version + '\'' +
                ", mailingList='" + mailingList + '\'' +
                ", creators=" + Arrays.toString(creators) +
                ", annotations=" + annotations +
                ", fileLocation='" + fileLocation + '\'' +
                ", reasonerType='" + reasonerType + '\'' +
                ", oboLims=" + oboLims +
                ", labelProperty='" + labelProperty + '\'' +
                ", definitionProperties=" + Arrays.toString(definitionProperties) +
                ", synonymProperties=" + Arrays.toString(synonymProperties) +
                ", hierarchicalProperties=" + Arrays.toString(hierarchicalProperties) +
                ", baseUris=" + Arrays.toString(baseUris) +
                ", hiddenProperties=" + Arrays.toString(hiddenProperties) +
                ", internalMetadataProperties=" + Arrays.toString(internalMetadataProperties) +
                ", skos=" + skos +
                '}';
    }
}
