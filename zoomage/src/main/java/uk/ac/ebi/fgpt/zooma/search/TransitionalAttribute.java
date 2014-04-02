package uk.ac.ebi.fgpt.zooma.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.arrayexpress2.magetab.datamodel.sdrf.node.attribute.CharacteristicsAttribute;
import uk.ac.ebi.arrayexpress2.magetab.datamodel.sdrf.node.attribute.FactorValueAttribute;
import uk.ac.ebi.fgpt.zooma.model.AnnotationSummary;

import java.util.ArrayList;

/**
 * Convenience class for minimal and *temporary* representation of an attribute of an annotation.
 * This representation takes a CharacteristicsAttribute object
 * or a FactorValue object and temporarily stores the corresponding four primary
 * components of the attribute (type, originalTermValue, termSourceREF, originalTermAccessionNumber).
 * The benefit it provides is that only a single version of each method in the ZoomaRESTClient is needed.
 * Comments are appended to the attribute every time one of its components is updated.
 *
 * @author jmcmurry
 * @date 05/04/2013
 */
public class TransitionalAttribute {

    private Logger log = LoggerFactory.getLogger(getClass());

    private String accession = ""; // The accession number to which this transitional attribute applies. eg. magetab accession
    private String originalType = "";
    private String normalisedType = "";

    private String originalTermValue = ""; // 	This is the text preliminaryStringValue supplied as part of the submitted file.
    private String originalTermSourceREF = ""; // If your term had a pre-existing annotation, this contains the source of this mapping.
    private String originalTermAccessionNumber = "";

    private String zoomifiedOntologyClassLabel = ""; // If your term resulted in a Zooma mapping, this contains the label of the class in the ontology that Zooma mapped to
    private String zoomifiedTermSourceREF = ""; // If your term resulted in a Zooma mapping, this contains the source of this mapping. This is usually a dataset in which a similar property preliminaryStringValue was found annotated to the suggested ontology class.;
    private String zoomifiedTermValue = "";      // 	This is most often identical to the text preliminaryStringValue supplied as part of your search, but occasionally Zooma determines is close enough to a text preliminaryStringValue previously determined to map to a given ontology term.
    private String zoomifiedOntAccession = ""; // If your term resulted in a Zooma mapping, this contains the source of this mapping. This is usually a dataset in which a similar property preliminaryStringValue was found annotated to the suggested ontology class.;


    private String categoryOfZoomaMapping = "";    // 	This indicates how confident ZOOMA was with the mapping. "Automatic" means ZOOMA is highly confident and "Requires curation" means ZOOMA found at least match that might fit but ZOOMA is not confident enough to automatically assert it.
    private int numberOfZoomaResultsAfterFilter;   //  This indicates the number of results that Zooma found based on the input parameters. 0 denotes no results meet criteria, 1 denotes automated curation, >1 denotes needs curation.
    private int numberOfZoomaResultsBeforeFilter;   //  This indicates the number of results that Zooma found before filters applied
    private String basisForExclusion = "";

    protected AnnotationSummary annotationSummary;

    private String errorMessage = "";

    protected AnnotationSummary runnerUpAnnotation;
    private String runnerUpTermValue;
    private String runnerUpTermSourceRef;
    private String runnerUpOntAccession;
    private String runnerUpTermLabel;

    public TransitionalAttribute(String magetabAccession, String originalType, String originalAttributeValue, int numberOfZoomaResultsBeforeFilter, AnnotationSummary annotationSummary) {
        // if type is null or blank, then set this type to null, else attribute type
        this.originalType = (originalType == null || originalType.equals("") ? null : originalType);
//        this.normalisedType = norm
        this.normalisedType = ZoomageUtils.normaliseType(originalType);

        this.originalTermValue = (originalAttributeValue == null || originalAttributeValue.equals("") ? null : originalAttributeValue);

        this.accession = magetabAccession;
        this.numberOfZoomaResultsBeforeFilter = numberOfZoomaResultsBeforeFilter;

        setAnnotationSummary(annotationSummary);

    }

    public String[] getFields() {
        return new String[]{originalType, originalTermValue, getZoomifiedTermValue(), getZoomifiedOntologyClassLabel(), getZoomifiedTermSourceREF(), getZoomifiedOntAccession(), accession};
    }

    /**
     * Make a TransitionalAttribute object from an original CharacteristicsAttribute that needs to be Zoomified.
     *
     * @param attribute CharacteristicsAttribute
     */
    public TransitionalAttribute(String magetabAccession, CharacteristicsAttribute attribute) {

        this.originalType = (attribute.type == null || attribute.type.equals("") ? null : attribute.type);

        this.normalisedType = ZoomageUtils.normaliseType(originalType);

        this.originalTermValue = (attribute.getAttributeValue() == null || attribute.getAttributeValue().equals("") ? null : attribute.getAttributeValue());

        this.originalTermSourceREF = (attribute.termSourceREF == null || attribute.termSourceREF.equals("") ? null : attribute.termSourceREF);

        this.originalTermAccessionNumber = (attribute.termAccessionNumber == null || attribute.termAccessionNumber.equals("") ? null : attribute.termAccessionNumber);

        this.accession = magetabAccession;
    }

    /**
     * Make a TransitionalAttribute object from a FactorValueAttribute
     *
     * @param attribute FactorValueAttribute
     */
    public TransitionalAttribute(String magetabAccession, FactorValueAttribute attribute) {

        // if type is null or blank, then set this type to null, else attribute type
        this.originalType = (attribute.type == null || attribute.type.equals("") ? null : attribute.type);

        this.normalisedType = ZoomageUtils.normaliseType(originalType);

        this.originalTermValue = (attribute.getAttributeValue() == null || attribute.getAttributeValue().equals("") ? null : attribute.getAttributeValue());

        this.originalTermSourceREF =
                (attribute.termSourceREF == null || attribute.termSourceREF.equals("") ? null :
                        attribute.termSourceREF);

        this.originalTermAccessionNumber =
                (attribute.termAccessionNumber == null || attribute.termAccessionNumber.equals("") ? null :
                        attribute.termAccessionNumber);

        this.accession = magetabAccession;
    }

    public TransitionalAttribute(String magetabAccession, String type, String originalTermValue, int numberOfZoomaResultsBeforeFilter, int numberOfZoomaResultsAfterFilter) {

        // if type is null or blank, then set this type to null, else attribute type
        this.originalType = (type == null || type.equals("") ? null : type);

        this.normalisedType = ZoomageUtils.normaliseType(originalType);

        this.originalTermValue = (originalTermValue == null || originalTermValue.equals("") ? null : originalTermValue);

        this.accession = magetabAccession;

        this.numberOfZoomaResultsBeforeFilter = numberOfZoomaResultsBeforeFilter;
        this.numberOfZoomaResultsAfterFilter = numberOfZoomaResultsAfterFilter;
    }


    public String getOriginalType() {
        return originalType;
    }

    public String getOriginalTermAccessionNumber() {
        return originalTermAccessionNumber;
    }

    public String getOriginalTermValue() {
        return originalTermValue;
    }

    public String getOriginalTermSourceREF() {
        return originalTermSourceREF;
    }

    public String getZoomifiedOntologyClassLabel() {
        return zoomifiedOntologyClassLabel;
    }

    public String getZoomifiedTermValue() {
        return zoomifiedTermValue;
    }

    public String getZoomifiedTermSourceREF() {
        return zoomifiedTermSourceREF;
    }

    public String getZoomifiedOntAccession() {
        return zoomifiedOntAccession;
    }


    public String getAccession() {
        return accession;
    }

    public void setAccession(String accession) {
        this.accession = accession;
    }

    public String getCategoryOfZoomaMapping() {
        if (categoryOfZoomaMapping != null && !categoryOfZoomaMapping.isEmpty())
            return categoryOfZoomaMapping;
        else if (!errorMessage.equals("")) return "Error";
        else if (!basisForExclusion.equals("")) return "Excluded";
        else if (numberOfZoomaResultsBeforeFilter == 0) return "No results at all";
        else if (numberOfZoomaResultsAfterFilter == 0) return "No results meet criteria";
        else if (numberOfZoomaResultsAfterFilter == 1) return "Automatic";
        else if (numberOfZoomaResultsAfterFilter > 1) return "Requires Curation";
        else return "other";
    }

    private void setCategoryOfZoomaMapping(String categoryOfZoomaMapping) {
        this.categoryOfZoomaMapping = categoryOfZoomaMapping;
    }

    public int getNumberOfZoomaResultsAfterFilter() {
        return numberOfZoomaResultsAfterFilter;
    }

    public AnnotationSummary getAnnotationSummary() {
        return annotationSummary;
    }

    public void setAnnotationSummary(AnnotationSummary annotationSummary) {
        this.annotationSummary = annotationSummary;

        if (annotationSummary != null) {
            this.zoomifiedTermValue = annotationSummary.getAnnotatedPropertyValue();
            this.zoomifiedTermSourceREF = ZoomageUtils.getCompoundTermSourceRef(annotationSummary);
            this.zoomifiedOntAccession = ZoomageUtils.getCompoundTermSourceAccession(annotationSummary);
            this.zoomifiedOntologyClassLabel = ZoomageUtils.getLabel(annotationSummary);
        }
    }

    public boolean isProducedZoomaError() {
        return !errorMessage.isEmpty();
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        this.setCategoryOfZoomaMapping("ERROR. " + errorMessage);
    }

    public int getNumberOfZoomaResultsBeforeFilter() {
        return numberOfZoomaResultsBeforeFilter;
    }

    protected Logger getLog() {
        return log;
    }

    public String getNormalisedType() {
        return normalisedType;
    }

    public String getInput() {
        return normalisedType + ":" + originalTermValue;
    }

    /**
     * Delegates acquisition of a ZoomaAnnotationSummary based on type and preliminaryStringValue of the attribute passed in.
     * Using the resulting best summary, update the attribute accordingly.
     *
     * @return zoomified attribute.
     */
    public void storeZoomifications(AnnotationSummary zoomaAnnotationSummary, int numberOfZoomaResultsBeforeFilter, int numberOfZoomaResultsAfterFilter) {
        this.numberOfZoomaResultsBeforeFilter = numberOfZoomaResultsBeforeFilter;
        this.numberOfZoomaResultsAfterFilter = numberOfZoomaResultsAfterFilter;
        getLog().debug("Storing zoomifications in transitional attribute");

        // if there are zooma results, store them in the transitional attribute
        if (zoomaAnnotationSummary != null) {
            setAnnotationSummary(zoomaAnnotationSummary);
        }

    }

    public void setRunnerUpAnnotation(AnnotationSummary runnerUpAnnotation) {
        this.runnerUpAnnotation = runnerUpAnnotation;
        if (runnerUpAnnotation != null) {
            this.runnerUpTermValue = runnerUpAnnotation.getAnnotatedPropertyValue();
            this.runnerUpTermSourceRef = ZoomageUtils.getCompoundTermSourceRef(runnerUpAnnotation);
            this.runnerUpOntAccession = ZoomageUtils.getCompoundTermSourceAccession(runnerUpAnnotation);
            this.runnerUpTermLabel = ZoomageUtils.getLabel(runnerUpAnnotation);
        }
    }


    public String getBasisForExclusion() {
        return basisForExclusion;
    }

    public boolean isExcluded() {
        return basisForExclusion.equals("");
    }

    public void setBasisForExclusion(String basisForExclusion) {
        this.basisForExclusion = basisForExclusion;
    }

    public boolean excludeBasedOn(ExclusionProfileAttribute exclusionProfileAttribute) {
//        #ORIGINAL TYPE,ORIGINAL VALUE,ZOOMA VALUE,ONT LABEL,TERM SOURCE REF,TERM ACCESSION,MAGETAB ACCESSION

        // initialise exclusion flag
        boolean allExclusionCriteriaMet = false;

        // if the criterion applies
        if (!exclusionProfileAttribute.getOriginalType().equals("")) {
            // and if criterion is met
            if (getNormalisedType().equalsIgnoreCase(exclusionProfileAttribute.getOriginalType())) {
                allExclusionCriteriaMet = true;
                basisForExclusion += "Type=" + getNormalisedType() + ". ";
            }
            // if criteria applies and is NOT met, stop
            else {
                allExclusionCriteriaMet = false;
                basisForExclusion = "";
                return allExclusionCriteriaMet;
            }
        }

        // if the criterion applies
        if (!exclusionProfileAttribute.getOriginalTermValue().equals("")) {
            // and if criterion is met
            if (getOriginalTermValue().equalsIgnoreCase(exclusionProfileAttribute.getOriginalTermValue())) {
                allExclusionCriteriaMet = true;
                basisForExclusion += "Value=" + getOriginalTermValue() + ". ";
            }
            // if criteria applies and is NOT met, stop
            else {
                allExclusionCriteriaMet = false;
                basisForExclusion = "";
                return allExclusionCriteriaMet;
            }
        }

        // if the criterion applies
        if (!exclusionProfileAttribute.getZoomifiedOntologyClassLabel().equals("")) {
            // and if criterion is met
            if (getZoomifiedOntologyClassLabel().equalsIgnoreCase(exclusionProfileAttribute.getZoomifiedOntologyClassLabel())) {
                allExclusionCriteriaMet = true;
                basisForExclusion += "Zooma Ont Label=" + getZoomifiedOntologyClassLabel() + ". ";
            }
            // if criteria applies and is NOT met, stop
            else {
                allExclusionCriteriaMet = false;
                basisForExclusion = "";
                return allExclusionCriteriaMet;
            }
        }


        // if the criterion applies
        if (!exclusionProfileAttribute.getZoomifiedTermSourceREF().equals("")) {
            // and if criterion is met
            if (getZoomifiedTermSourceREF().equalsIgnoreCase(exclusionProfileAttribute.getZoomifiedTermSourceREF())) {
                allExclusionCriteriaMet = true;
                basisForExclusion += "Zooma Ont Ref=" + getZoomifiedTermSourceREF() + ". ";
            }
            // if criteria applies and is NOT met, stop
            else {
                allExclusionCriteriaMet = false;
                basisForExclusion = "";
                return allExclusionCriteriaMet;
            }
        }

        // if the criterion applies
        if (!exclusionProfileAttribute.getZoomifiedOntAccession().equals("")) {
            // and if criterion is met
            if (getZoomifiedOntAccession().equalsIgnoreCase(exclusionProfileAttribute.getZoomifiedOntAccession())) {
                allExclusionCriteriaMet = true;
                basisForExclusion += "Zooma Ont ID=" + getZoomifiedOntAccession() + ". ";
            }
            // if criteria applies and is NOT met, stop
            else {
                allExclusionCriteriaMet = false;
                basisForExclusion = "";
                return allExclusionCriteriaMet;
            }
        }

        // if the criterion applies
        if (!exclusionProfileAttribute.getAccession().equals("")) {
            // and if criterion is met
            if (getAccession().equalsIgnoreCase(exclusionProfileAttribute.getAccession())) {
                allExclusionCriteriaMet = true;
                basisForExclusion += "Magetab Accession=" + getAccession() + ". ";
            }
            // if criteria applies and is NOT met, stop
            else {
                allExclusionCriteriaMet = false;
                basisForExclusion = "";
                return allExclusionCriteriaMet;
            }
        }

        if (!allExclusionCriteriaMet) {
            basisForExclusion = "";
        }

        return allExclusionCriteriaMet;

    }

    public String getRunnerUpTermLabel() {
        return runnerUpTermLabel;
    }


    public String getRunnerUpTermValue() {
        return runnerUpTermValue;
    }

    public String getRunnerUpTermSourceRef() {
        return runnerUpTermSourceRef;
    }

    public String getRunnerUpOntAccession() {
        return runnerUpOntAccession;
    }
}
