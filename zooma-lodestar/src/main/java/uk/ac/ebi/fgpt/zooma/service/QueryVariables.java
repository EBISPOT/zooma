package uk.ac.ebi.fgpt.zooma.service;

/**
 * @author Simon Jupp
 * @date 18/05/2012 Functional Genomics Group EMBL-EBI
 */
public enum QueryVariables {

    ANNOTATION_ID("annotationid"),
    BIOLOGICAL_ENTITY("bioentityid"),
    BIOLOGICAL_ENTITY_TYPE("bioentityType"),
    BIOLOGICAL_ENTITY_NAME("bioentitylabel"),
    PROPERTY_VALUE_ID("propertyvalueid"),
    PROPERTY_NAME("propertyname"),
    PROPERTY_VALUE("propertyvalue"),
    SEMANTIC_TAG("semantictag"),
    SEMANTIC_TAG_LABEL("label"),
    SYNONYM_PROPERTY("synonymproperty"),
    SEMANTIC_TAG_SYNONYM("synonym"),
    STUDY_ID("study"),
    STUDY_TYPE("studyType"),
    STUDY_LABEL("studylabel"),
    DATABASEID("databaseid"),
    SOURCETYPE("sourcetype"),
    SOURCENAME("sourcename"),
    GENERATOR("generator"),
    GENERATED("generated"),
    ANNOTATOR("annotator"),
    ANNOTATED("annotated"),
    EVIDENCE("evidence"),
    RESOURCE("resource"),
    RESOURCE_TYPE("resourceType"),
    KEYWORD ("keyword");

    private final String variable;

    private QueryVariables(final String variable) {
        this.variable = variable;
    }

    @Override
    public String toString() {
        return variable;
    }
}
