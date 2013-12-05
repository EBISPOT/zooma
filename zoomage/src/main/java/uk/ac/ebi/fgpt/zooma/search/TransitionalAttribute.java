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

    private String type;
    private String originalValue;
    private String zoomifiedvalue;
    private String originalTermSourceREF;
    private String zoomifiedTermSourceREF;
    private String originalTermAccessionNumber;
    private String zoomifiedTermAccessionNumber;
//    private boolean buildComments; // for eventual incorporation in SDRF file.


    private Logger log = LoggerFactory.getLogger(getClass());
    private String zoomifiedValue;

    protected Logger getLog() {
        return log;
    }

    /**
     * Make a TransitionalAttribute object from an original CharacteristicsAttribute that needs to be Zoomified.
     *
     * @param attribute         CharacteristicsAttribute
//     * @param addCommentsToSDRF
     */
    public TransitionalAttribute(CharacteristicsAttribute attribute) {
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
    }

    /**
     * Make a TransitionalAttribute object from a FactorValueAttribute
     *
     * @param attribute FactorValueAttribute
     */
    public TransitionalAttribute(FactorValueAttribute attribute) {

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
    }

    public void setOriginalValue(String originalValue) {
        // since this is only invoked when the originalValue is being overwritten, capture this event through appending a comment
        this.originalValue = originalValue;
    }

    public void setZoomifiedValue(String zoomifiedvalue) {
        // since this is only invoked when the originalValue is being overwritten, capture this event through appending a comment
        this.zoomifiedvalue = zoomifiedvalue;
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
}
