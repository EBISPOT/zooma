package uk.ac.ebi.fgpt.zooma.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.arrayexpress2.magetab.datamodel.sdrf.node.attribute.CharacteristicsAttribute;
import uk.ac.ebi.arrayexpress2.magetab.datamodel.sdrf.node.attribute.FactorValueAttribute;
import uk.ac.ebi.fgpt.zooma.model.AnnotationSummary;

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


    private String accession = ""; // The accession number to which this transitional attribute applies. eg. magetab accession
    private String type = "";

    private String originalTermValue = ""; // 	This is the text value supplied as part of the submitted file.
    private String originalTermSourceREF = ""; // If your term had a pre-existing annotation, this contains the source of this mapping.
    private String originalTermAccessionNumber;


    private String zoomifiedOntologyClassLabel = ""; // If your term resulted in a Zooma mapping, this contains the label of the class in the ontology that Zooma mapped to
    private String zoomifiedTermSourceREF = ""; // If your term resulted in a Zooma mapping, this contains the source of this mapping. This is usually a dataset in which a similar property value was found annotated to the suggested ontology class.;
    private String zoomifiedTermAccessionNumber = ""; // If your term resulted in a Zooma mapping, this contains the source of this mapping. This is usually a dataset in which a similar property value was found annotated to the suggested ontology class.;

    private String categoryOfZoomaMapping;    // 	This indicates how confident ZOOMA was with the mapping. "Automatic" means ZOOMA is highly confident and "Requires curation" means ZOOMA found at least match that might fit but ZOOMA is not confident enough to automatically assert it.
    private int numberOfZoomaResultsAfterFilter;   //  This indicates the number of results that Zooma found based on the input parameters. 0 denotes no results meet criteria, 1 denotes automated curation, >1 denotes needs curation.
    private int numberOfZoomaResultsBeforeFilter;   //  This indicates the number of results that Zooma found before filters applied
    private String zoomifiedTermValue;      // 	This is most often identical to the text value supplied as part of your search, but occasionally Zooma determines is close enough to a text value previously determined to map to a given ontology term.

    private AnnotationSummary annotationSummary;


    private boolean producedZoomaError = false;

    private Logger log = LoggerFactory.getLogger(getClass());

    public TransitionalAttribute(String magetabAccession, String cleanedAttributeType, String originalAttributeValue, int numberOfZoomaResultsBeforeFilter, AnnotationSummary annotationSummary) {
        // if type is null or blank, then set this type to null, else attribute type
        this.type = (cleanedAttributeType == null || cleanedAttributeType.equals("") ? null : cleanedAttributeType);

        this.originalTermValue = (originalAttributeValue == null || originalAttributeValue.equals("") ? null : originalAttributeValue);

        this.accession = magetabAccession;

        this.numberOfZoomaResultsBeforeFilter = numberOfZoomaResultsBeforeFilter;
        this.numberOfZoomaResultsAfterFilter = 1;
        this.annotationSummary = annotationSummary;
    }

    public String[] getFields() {
        return new String[]{type, originalTermValue, zoomifiedTermValue, zoomifiedOntologyClassLabel, zoomifiedTermSourceREF, zoomifiedTermAccessionNumber, accession};
    }

    public TransitionalAttribute(String delimitedString, String delimiter) {
        String[] exclusions = {"", "", "", "", "", "", ""};
        if (delimitedString.indexOf(delimiter) == -1) {
            getLog().error("Delimiter '" + delimiter + "' was not found in '" + delimitedString + "'");
            System.exit(0);
        }
        String[] exclusionsTemp = delimitedString.split(delimiter);

        for (int i = 0; i < exclusionsTemp.length; i++) {
            exclusions[i] = (exclusionsTemp[i] != null) ? exclusionsTemp[i] : "";
        }

        this.setType(exclusions[0].replace("_", " "));
        this.setOriginalTermValue(exclusions[1]);
        this.setZoomifiedTermValue(exclusions[2]);
        this.setZoomifiedOntologyClassLabel(exclusions[3]);
        this.setZoomifiedTermSourceREF(exclusions[4]);
        this.setZoomifiedTermAccessionNumber(exclusions[5]);
        this.setAccession(exclusions[6]);

    }


    /**
     * Make a TransitionalAttribute object from an original CharacteristicsAttribute that needs to be Zoomified.
     *
     * @param attribute CharacteristicsAttribute
     *                  //     * @param addCommentsToSDRF
     */
    public TransitionalAttribute(String magetabAccession, CharacteristicsAttribute attribute) {
//        this.buildComments = addCommentsToSDRF;
        this.type =
                (
                        attribute.type == null || attribute.type.equals("") ? null :
                                attribute.type);

        this.originalTermValue =
                (
                        attribute.getAttributeValue() == null || attribute.getAttributeValue().equals("") ? null :
                                attribute.getAttributeValue());

        this.originalTermSourceREF =
                (
                        attribute.termSourceREF == null || attribute.termSourceREF.equals("") ? null :
                                attribute.termSourceREF);

        this.originalTermAccessionNumber =
                (
                        attribute.termAccessionNumber == null || attribute.termAccessionNumber.equals("") ? null :
                                attribute.termAccessionNumber);

        this.accession = magetabAccession;
    }

    /**
     * Make a TransitionalAttribute object from a FactorValueAttribute
     *
     * @param attribute FactorValueAttribute
     */
    public TransitionalAttribute(String magetabAccession, FactorValueAttribute attribute) {

        // if type is null or blank, then set this type to null, else attribute type
        this.type = (
                attribute.type == null || attribute.type.equals("") ? null :
                        attribute.type);

        this.originalTermValue =
                (
                        attribute.getAttributeValue() == null || attribute.getAttributeValue().equals("") ? null :
                                attribute.getAttributeValue());

        this.originalTermSourceREF =
                (
                        attribute.termSourceREF == null || attribute.termSourceREF.equals("") ? null :
                                attribute.termSourceREF);

        this.originalTermAccessionNumber =
                (
                        attribute.termAccessionNumber == null || attribute.termAccessionNumber.equals("") ? null :
                                attribute.termAccessionNumber);

//        this.buildComments = addCommentsToSDRF;
        this.accession = magetabAccession;
    }

    public TransitionalAttribute(String magetabAccession, String type, String originalTermValue, int numberOfZoomaResultsBeforeFilter, int numberOfZoomaResultsAfterFilter) {

        // if type is null or blank, then set this type to null, else attribute type
        this.type = (type == null || type.equals("") ? null : type);

        this.originalTermValue = (originalTermValue == null || originalTermValue.equals("") ? null : originalTermValue);

        this.accession = magetabAccession;

        this.numberOfZoomaResultsBeforeFilter = numberOfZoomaResultsBeforeFilter;
        this.numberOfZoomaResultsAfterFilter = numberOfZoomaResultsAfterFilter;
    }

    public void setOriginalTermValue(String originalTermValue) {
        // since this is only invoked when the originalTermValue is being overwritten, capture this event through appending a comment
        this.originalTermValue = originalTermValue;
    }

    public void setZoomifiedTermValue(String zoomifiedvalue) {
        // since this is only invoked when the originalTermValue is being overwritten, capture this event through appending a comment
        this.zoomifiedTermValue = zoomifiedvalue;
//        if (buildComments) appendComment("Value", this.originalTermValue, zoomifiedvalue);
    }

    public void setType(String type) {
        // since this is only invoked when the type is being overwritten, capture this event through appending a comment
//        if (buildComments) appendComment("Type", this.type, type);
        this.type = type;
    }

    public void setZoomifiedTermSourceREF(String zoomifiedTermSourceREF) {
        // since this is only invoked when the termSourceRef is being overwritten, capture this event through appending a comment
        this.zoomifiedTermSourceREF = zoomifiedTermSourceREF;
//        if (buildComments) appendComment("TermSourceREF", this.originalTermSourceREF, zoomifiedTermSourceREF);
    }

    public void setOriginalTermSourceREF(String originalTermSourceREF) {
        this.originalTermSourceREF = originalTermSourceREF;
    }


    public void setZoomifiedTermAccessionNumber(String zoomifiedTermAccessionNumber) {
        // since this is only invoked when the TermAccessionNumber is being overwritten, capture this event through appending a comment
        this.zoomifiedTermAccessionNumber = zoomifiedTermAccessionNumber;
//        if (buildComments) appendComment("TermAccessionNumber", this.originalTermAccessionNumber, zoomifiedTermAccessionNumber);
    }

    public void setOriginalTermAccessionNumber(String originalTermAccessionNumber) {
        this.originalTermAccessionNumber = originalTermAccessionNumber;
    }

    public void setZoomifiedOntologyClassLabel(String zoomifiedOntologyClassLabel) {
        this.zoomifiedOntologyClassLabel = zoomifiedOntologyClassLabel;
    }

    public String getZoomifiedOntologyClassLabel() {
        return zoomifiedOntologyClassLabel;
    }


    public String getType() {
        return type;
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


    public String getZoomifiedTermValue() {
        return zoomifiedTermValue;
    }

    public String getZoomifiedTermSourceREF() {
        return zoomifiedTermSourceREF;
    }

    public String getZoomifiedTermAccessionNumber() {
        return zoomifiedTermAccessionNumber;
    }

    public String getAccession() {
        return accession;
    }

    public void setAccession(String accession) {
        this.accession = accession;
    }

    public String getCategoryOfZoomaMapping() {
        return categoryOfZoomaMapping;
    }

    public void setCategoryOfZoomaMapping(String categoryOfZoomaMapping) {
        this.categoryOfZoomaMapping = categoryOfZoomaMapping;
    }

    public int getNumberOfZoomaResultsAfterFilter() {
        return numberOfZoomaResultsAfterFilter;
    }

    public void setNumberOfZoomaResultsAfterFilter(int numberOfZoomaResultsAfterFilter) {
        this.numberOfZoomaResultsAfterFilter = numberOfZoomaResultsAfterFilter;
    }

    public AnnotationSummary getAnnotationSummary() {
        return annotationSummary;
    }

    public void setAnnotationSummary(AnnotationSummary annotationSummary) {
        this.annotationSummary = annotationSummary;
    }


//    public static String printCompareAttributes(TransitionalAttribute attribute, TransitionalAttribute exclusionProfile) {
//
//        String[] headers = {"type", "originalTermValue", "zoomifiedTermValue", "zoomifiedOntologyClassLabel", "zoomifiedTermSourceREF", "zoomifiedTermAccessionNumber", "accession"};
//
//        String result = "\n " + "   |   " + "header" + "" + "   |   " + "exclusions" + "   |   " + "attribute";
//        result += "\n " + "   |   " + "---------------------------------------------------";
//        String[] exclusionFields = exclusionProfile.getFields();
//        String[] attributeFields = attribute.getFields();
//
//        for (int i = 0; i < exclusionFields.length; i++) {
//            result += "\n" + i + "   |   " + headers[i] + "   |   " + exclusionFields[i] + "   |   " + attributeFields[i];
//        }
//
//        return result;
//    }
//
//    public static String printAttribute(TransitionalAttribute attribute) {
//        String[] headers = {"type", "originalTermValue", "zoomifiedTermValue", "zoomifiedOntologyClassLabel", "zoomifiedTermSourceREF", "zoomifiedTermAccessionNumber", "accession"};
//
//        String result = "\n ";
//        String[] attributeFields = attribute.getFields();
//
//        for (int i = 0; i < attributeFields.length; i++) {
//            result += "\n" + i + "   |   " + headers[i] + "   |   " + attributeFields[i];
//        }
//
//        return result;
//    }

    public boolean isProducedZoomaError() {
        return producedZoomaError;
    }

    public void setProducedZoomaError(boolean producedZoomaError) {
        this.producedZoomaError = producedZoomaError;
    }

    protected Logger getLog() {
        return log;
    }
}
