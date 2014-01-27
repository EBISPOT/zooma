package uk.ac.ebi.fgpt.zooma.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.arrayexpress2.magetab.datamodel.sdrf.node.attribute.CharacteristicsAttribute;
import uk.ac.ebi.arrayexpress2.magetab.datamodel.sdrf.node.attribute.FactorValueAttribute;

import java.util.ArrayList;

/**
 * Convenience class for minimal and *temporary* representation of an attribute of an annotation.
 * This representation takes a CharacteristicsAttribute object
 * or a FactorValue object and temporarily stores the corresponding four primary
 * components of the attribute (type, originalValue, termSourceREF, originalTermAccessionNumber).
 * The benefit it provides is that only a single version of each method in the ZoomaRESTClient is needed.
 * Comments are appended to the attribute every time one of its components is updated.
 *
 * @author jmcmurry
 * @date 05/04/2013
 */
public class TransitionalAttribute {

    //    "#ORIGINAL TYPE","ORIGINAL VALUE","ZOOMA VALUE","ONT LABEL","TERM SOURCE REF","TERM ACCESSION","MAGETAB ACCESSION"
    private String type = "";
    private String originalValue = "";
    private String zoomifiedOntologyLabel = "";
    private String zoomifiedTermSourceREF = "";
    private String zoomifiedTermAccessionNumber = "";
    private String magetabAccession = "";


    public String[] getFields() {
        return new String[]{type, originalValue, zoomifiedValue, zoomifiedOntologyLabel, zoomifiedTermSourceREF, zoomifiedTermAccessionNumber, magetabAccession};
    }


    private String originalTermSourceREF;
    private String originalTermAccessionNumber;
//    private boolean buildComments; // for eventual incorporation in SDRF file.


    private Logger log = LoggerFactory.getLogger(getClass());
    private String zoomifiedValue;

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
        this.setOriginalValue(exclusions[1]);
        this.setZoomifiedValue(exclusions[2]);
        this.setZoomifiedOntologyLabel(exclusions[3]);
        this.setZoomifiedTermSourceREF(exclusions[4]);
        this.setZoomifiedTermAccessionNumber(exclusions[5]);
        this.setMagetabAccession(exclusions[6]);

    }


    protected Logger getLog() {
        return log;
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

        this.originalValue =
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

        this.magetabAccession = magetabAccession;
    }

    /**
     * Make a TransitionalAttribute object from a FactorValueAttribute
     *
     * @param attribute FactorValueAttribute
     */
    public TransitionalAttribute(String magetabAccession, FactorValueAttribute attribute) {

        if (attribute.type.equalsIgnoreCase("organism part")) {
            System.out.println("stop here");
        }

        // if type is null or blank, then set this type to null, else attribute type
        this.type = (
                attribute.type == null || attribute.type.equals("") ? null :
                        attribute.type);

        this.originalValue =
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
        this.magetabAccession = magetabAccession;
    }

    public void setOriginalValue(String originalValue) {
        // since this is only invoked when the originalValue is being overwritten, capture this event through appending a comment
        this.originalValue = originalValue;
    }

    public void setZoomifiedValue(String zoomifiedvalue) {
        // since this is only invoked when the originalValue is being overwritten, capture this event through appending a comment
        this.zoomifiedValue = zoomifiedvalue;
//        if (buildComments) appendComment("Value", this.originalValue, zoomifiedvalue);
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

    public void setZoomifiedOntologyLabel(String zoomifiedOntologyLabel) {
        this.zoomifiedOntologyLabel = zoomifiedOntologyLabel;
    }

    public String getZoomifiedOntologyLabel() {
        return zoomifiedOntologyLabel;
    }


    public String getType() {
        return type;
    }

    public String getOriginalTermAccessionNumber() {
        return originalTermAccessionNumber;
    }

    public String getOriginalValue() {
        return originalValue;
    }

    public String getOriginalTermSourceREF() {
        return originalTermSourceREF;
    }


    public String getZoomifiedValue() {
        return zoomifiedValue;
    }

    public String getZoomifiedTermSourceREF() {
        return zoomifiedTermSourceREF;
    }

    public String getZoomifiedTermAccessionNumber() {
        return zoomifiedTermAccessionNumber;
    }

    public String getMagetabAccession() {
        return magetabAccession;
    }

    public void setMagetabAccession(String magetabAccession) {
        this.magetabAccession = magetabAccession;
    }


//    public static String printCompareAttributes(TransitionalAttribute attribute, TransitionalAttribute exclusionProfile) {
//
//        String[] headers = {"type", "originalValue", "zoomifiedValue", "zoomifiedOntologyLabel", "zoomifiedTermSourceREF", "zoomifiedTermAccessionNumber", "magetabAccession"};
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
//        String[] headers = {"type", "originalValue", "zoomifiedValue", "zoomifiedOntologyLabel", "zoomifiedTermSourceREF", "zoomifiedTermAccessionNumber", "magetabAccession"};
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
}
