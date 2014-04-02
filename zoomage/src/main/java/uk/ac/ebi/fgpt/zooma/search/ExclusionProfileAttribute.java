package uk.ac.ebi.fgpt.zooma.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: jmcmurry
 * Date: 30/03/2014
 * Time: 09:54
 * Class to store the information about which kinds of zoomifications to exclude.
 * In order to be excluded, all criteria must be met. So if a profile specifies both a type and a value, all attributes
 * with that type and that value would be annotated (if an automatic mapping is available). Transitional attributes
 * with just the type but not the value, or vice versa, would be skipped (left in their original state).
 * The actual string matching and exclusion determination is done in the TransitionalAttribute class.
 */
public class ExclusionProfileAttribute {

    private Logger log = LoggerFactory.getLogger(getClass());

    private String accession = ""; // The accession number to which this transitional attribute applies. eg. magetab accession
    private String originalType = "";

    private String originalTermValue = ""; // 	This is the text preliminaryStringValue supplied as part of the submitted file.
//    private String originalTermSourceREF = ""; // If your term had a pre-existing annotation, this contains the source of this mapping.
//    private String originalTermAccessionNumber = ""; // If your term had a pre-existing annotation, this contains the id of this mapping.

    private String zoomifiedOntologyClassLabel = ""; // If your term resulted in a Zooma mapping, this contains the label of the class in the ontology that Zooma mapped to
    private String zoomifiedTermSourceREF = ""; // If your term resulted in a Zooma mapping, this contains the source of this mapping. This is usually a dataset in which a similar property preliminaryStringValue was found annotated to the suggested ontology class.;
    private String zoomifiedTermValue;      // 	This is most often identical to the text preliminaryStringValue supplied as part of your search, but occasionally Zooma determines is close enough to a text preliminaryStringValue previously determined to map to a given ontology term.
    private String zoomifiedOntAccession = ""; // If your term resulted in a Zooma mapping, this contains the source of this mapping. This is usually a dataset in which a similar property preliminaryStringValue was found annotated to the suggested ontology class.;


    public ExclusionProfileAttribute(String delimitedString, String delimiter) {

        String[] exclusions = {"", "", "", "", "", "", ""};

        if (delimitedString.indexOf(delimiter) == -1) {
            throw new IllegalArgumentException("Delimiter '" + delimiter + "' was not found in '" + delimitedString + "'");
        }

        String[] exclusionsTemp = delimitedString.split(delimiter);

        for (int i = 0; i < exclusionsTemp.length; i++) {
            exclusions[i] = (exclusionsTemp[i] != null) ? exclusionsTemp[i] : "";
        }

        this.originalType = (exclusions[0]);
        this.originalTermValue = (exclusions[1]);
        this.zoomifiedTermValue = (exclusions[2]);
        this.zoomifiedOntologyClassLabel = (exclusions[3]);
        this.zoomifiedTermSourceREF = (exclusions[4]);
        this.zoomifiedOntAccession = (exclusions[5]);
        this.accession = (exclusions[6]);

    }


    public Logger getLog() {
        return log;
    }

    public String getOriginalTermValue() {
        return originalTermValue;
    }

    public String getZoomifiedOntologyClassLabel() {
        return zoomifiedOntologyClassLabel;
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

    public String getOriginalType() {
        return originalType;
    }

    public String getZoomifiedTermValue() {
        return zoomifiedTermValue;
    }
}
