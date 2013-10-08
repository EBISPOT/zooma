package uk.ac.ebi.fgpt.zooma.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.arrayexpress2.magetab.datamodel.sdrf.node.attribute.CharacteristicsAttribute;
import uk.ac.ebi.arrayexpress2.magetab.datamodel.sdrf.node.attribute.FactorValueAttribute;

import java.util.ArrayList;

/**
 * Convenience class for minimal and temporary representation of an attribute of an annotation.
 * This representation takes a CharacteristicsAttribute object
 * or a FactorValue object and temporarily stores the corresponding four primary
 * components of the attribute (type, value, termSourceREF, termAccessionNumber).
 * The benefit it provides is that only a single version of each method in the ZoomaRESTClient is needed.
 * Comments are appended to the attribute any time one of its components is updated.
 *
 * @author jmcmurry
 * @date 05/04/2013
 */
public class TransitionalAttribute {

    private String type;
    private String value;
    private String termSourceREF;
    private String termAccessionNumber;

    private ArrayList<String> comments = new ArrayList<String>();

    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    /**
     * Make a TransitionalAttribute object from an original CharacteristicsAttribute that needs to be Zoomified.
     *
     * @param attribute CharacteristicsAttribute
     */
    public TransitionalAttribute(CharacteristicsAttribute attribute) {
        this.type =
                (
                        attribute.type == null || attribute.type.equals("") ? null :
                                attribute.type);

        this.value =
                (
                        attribute.getAttributeValue() == null || attribute.getAttributeValue().equals("") ? null :
                                attribute.getAttributeValue());

        this.termSourceREF =
                (
                        attribute.termSourceREF == null || attribute.termSourceREF.equals("") ? null :
                                attribute.termSourceREF);

        this.termAccessionNumber =
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

        this.value =
                (
                        attribute.getAttributeValue() == null || attribute.getAttributeValue().equals("") ? null :
                                attribute.getAttributeValue());

        this.termSourceREF =
                (
                        attribute.termSourceREF == null || attribute.termSourceREF.equals("") ? null :
                                attribute.termSourceREF);

        this.termAccessionNumber =
                (
                        attribute.termAccessionNumber == null || attribute.termAccessionNumber.equals("") ? null :
                                attribute.termAccessionNumber);
    }

    public void setValue(String value) {
        // since this is only invoked when the value is being overwritten, capture this event through appending a comment
        appendComment("Value", this.value, value);
        this.value = value;
    }

    public void setType(String type) {
        // since this is only invoked when the type is being overwritten, capture this event through appending a comment
        appendComment("Type", this.type, type);
        this.type = type;
    }

    public void setTermSourceREF(String termSourceREF) {
        // since this is only invoked when the termSourceRef is being overwritten, capture this event through appending a comment
        appendComment("TermSourceREF", this.termSourceREF, termSourceREF);
        this.termSourceREF = termSourceREF;
    }


    public void setTermAccessionNumber(String termAccessionNumber) {
        // since this is only invoked when the TermAccessionNumber is being overwritten, capture this event through appending a comment
        appendComment("TermAccessionNumber", this.termAccessionNumber, termAccessionNumber);
        this.termAccessionNumber = termAccessionNumber;
    }

    private void appendComment(String varName, String oldString, String newString) {
//        // if there's no new string, just return
//        if (newString == null || newString.equals("")) return;
//
//        // else, initialize comment
//        String comment = "";
//
//        // if there's no original annotation, phrase the comment accordingly
//        if (oldString == null || oldString.equals("")) comment = (varName + " set to " + newString + ".");
//
//            // otherwise if zoomification overwrites an existing annotation, phrase the comment accordingly
//        else if (!oldString.equals(newString)) comment = (varName + " " + this.type + " changed to " + type + ".");
//
//        // finally, append the comment
//        comments.add(comment);
    }

    public String getType() {
        return type;
    }

    public String getTermAccessionNumber() {
        return termAccessionNumber;
    }

    public String getValue() {
        return value;
    }

    public String getTermSourceREF() {
        return termSourceREF;
    }

    public ArrayList<String> getComments() {
        return comments;
    }

}
