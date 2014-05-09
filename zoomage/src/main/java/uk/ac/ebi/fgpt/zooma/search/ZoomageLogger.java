package uk.ac.ebi.fgpt.zooma.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

/**
 * User: jmcmurry
 * Date: 26/03/2014
 * Time: 17:12
 */
public class ZoomageLogger {

    private static final Logger log = LoggerFactory.getLogger(ZoomageLogger.class);

    private float cutoffScore;
    private int minStringLength;
    private float cutoffPercentage;
    private boolean olsShortIds;
    private String logFileDelimiter;
    private boolean overwriteValues;
    private boolean overwriteAnnotations;

    private boolean stripLegacyAnnotations;
    private final String zoomaPath;
    private final String limpopoPath;
    private String configLogString;
    protected ArrayList<String> combinedLogFileRows = new ArrayList<String>();
    private ArrayList<String> combinedCurationLogFileRows = new ArrayList<String>();

    // Print the headers
    private String[] headers = {
            "PROPERTY_TYPE", "PROPERTY_VALUE", "CORRESPONDING_LABEL", "VALUE|LABEL", "SEMANTIC_TAG","CORRESPONDING_ZOOMA_SCORE", "ANNOTATOR", "ANNOTATION_DATE",
            "STUDY", "BIOENTITY",  "Original Ont Source", "Original Ont Source Id",
            "Matching Zooma Input", "Zooma Ont Label", "Zoomified Ont Source", "Zoomified Ont Source ID", "Category of Zooma Mapping",
            "# Results before filter", "# Results after filter", "Basis for Exclusion", "ID of Automatic Annotation", "Summary Score of Automatic Annotation",
            "RunnerUp Annotation Summary Score",
            "RunnerUp Zooma Ont Label",
            "Zooma Error Message"};


    public ZoomageLogger(float cutoffScore, int minStringLength, float cutoffPercentage, boolean olsShortIds,
                         String logFileDelimiter, boolean overwriteValues,
                         boolean overwriteAnnotations, boolean stripLegacyAnnotations,
                         String zoomaPath, String limpopoPath) {

        this.cutoffScore = cutoffScore;
        this.minStringLength = minStringLength;
        this.cutoffPercentage = cutoffPercentage;
        this.olsShortIds = olsShortIds;
        this.logFileDelimiter = logFileDelimiter;
        this.overwriteValues = overwriteValues;
        this.overwriteAnnotations = overwriteAnnotations;
        this.stripLegacyAnnotations = stripLegacyAnnotations;

        this.zoomaPath = zoomaPath;
        this.limpopoPath = limpopoPath;

        this.configLogString = setConfigLogString();

    }

    public ArrayList<String> formatErrorAsLogFileRow(String accession) {

        HashMap<String, TransitionalAttribute> cacheErrorMap = new HashMap<>();

        TransitionalAttribute errorItem = new TransitionalAttribute(accession, "", null, null, 0, 0);
        errorItem.setErrorMessage("Accession " + accession + " could not be processed");

        cacheErrorMap.put(null, errorItem);
        return formatCacheAsLogFileRows(cacheErrorMap);
    }


    public ArrayList<String> formatCacheAsLogFileRows(HashMap<String, TransitionalAttribute> masterCache) {

        ArrayList<String> rows = new ArrayList<>();

        // Print store each attribute as a string.
        for (TransitionalAttribute attribute : masterCache.values()) {

            String row = transitionalAttributeToLogRow(attribute);

            rows.add(row);

        }

        return rows;

    }


    public String transitionalAttributeToLogRow(TransitionalAttribute attribute) {
        return transitionalAttributeToLogRow(attribute, configLogString);
    }

    public String transitionalAttributeToLogRow(TransitionalAttribute attribute, String configLogString) {
        String row = "";

        //"PROPERTY_TYPE", "PROPERTY_VALUE", "CORRESPONDING_LABEL","COMPARISON", "SEMANTIC_TAG", "STUDY", "BIOENTITY", "CORRESPONDING_ZOOMA_SCORE", "ANNOTATOR", "ANNOTATION_DATE",

        row += standardiseNulls(attribute.getOriginalType());
        row += logFileDelimiter;

        // PROPERTY VALUE

        String originalValue = attribute.getOriginalTermValue();

        row += standardiseNulls(originalValue.replaceAll(logFileDelimiter, " "));
        row += logFileDelimiter;

        String semanticTag = standardiseNulls(attribute.getRunnerUpOntAccession());

        if (semanticTag != null) {

            // CORRESPONDING_LABEL
            row += attribute.getRunnerUpTermLabel();
            row += logFileDelimiter;

            // COMPARISON
            String comparison = "";
            if (originalValue.equalsIgnoreCase(attribute.getRunnerUpTermLabel())) {
                comparison = "EXACT MATCH";
            } else comparison = "mismatch";
            row += comparison;

            row += logFileDelimiter;

            // SEMANTIC_TAG
            row += attribute.getRunnerUpOntAccession();
            row += logFileDelimiter;

            // CORRESPONDING_ZOOMA_SCORE
            row += attribute.runnerUpAnnotation.getQuality();
            row += logFileDelimiter;

            //ANNOTATOR (Curator's name)
            row += "FIRSTNAME LASTNAME";
            row += logFileDelimiter;

            //ANNOTATION_DATE
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            row += sdf.format(new Date());
            row += logFileDelimiter;
        } else {
            row += appendNull(5);
        }

        //
        row += standardiseNulls(attribute.getStudy());
        row += logFileDelimiter;

        //
        row += standardiseNulls(attribute.getBioentity());
        row += logFileDelimiter;

        //
        row += standardiseNulls(attribute.getOriginalTermSourceREF()); // If your term had a pre-existing annotation, this contains the source of this mapping.
        row += logFileDelimiter;

        //
        row += standardiseNulls(attribute.getOriginalTermAccessionNumber());
        row += logFileDelimiter;

        //
        String zoomifiedTermValue = compareStrings(attribute.getZoomifiedTermValue(), attribute.getOriginalTermValue());

        row += standardiseNulls(zoomifiedTermValue);      // 	This is most often identical to the text preliminaryStringValue supplied as part of your search, but occasionally Zooma determines is close enough to a text preliminaryStringValue previously determined to map to a given ontology term.
        row += logFileDelimiter;

        //
        row += compareStrings(attribute.getZoomifiedOntologyClassLabel(), attribute.getOriginalTermValue()); // If your term resulted in a Zooma mapping, this contains the label of the class in the ontology that Zooma mapped to
        row += logFileDelimiter;

        //
        row += standardiseNulls(attribute.getZoomifiedTermSourceREF()); // If your term resulted in a Zooma mapping, this contains the source of this mapping. This is usually a dataset in which a similar property preliminaryStringValue was found annotated to the suggested ontology class.;
        row += logFileDelimiter;

        //
        row += standardiseNulls(attribute.getZoomifiedOntAccession()); // If your term resulted in a Zooma mapping, this contains the id of the class in the ontology that Zooma mapped to
        row += logFileDelimiter;

        //
        String mappingCategory = String.valueOf(attribute.getCategoryOfZoomaMapping());
        if (attribute.getBasisForExclusion() != null && !attribute.getBasisForExclusion().isEmpty())
            mappingCategory = "EXCLUDED";

        row += (mappingCategory);    // 	This indicates how confident ZOOMA was with the mapping. "Automatic" means ZOOMA is highly confident and "Requires curation" means ZOOMA found at least match that might fit but ZOOMA is not confident enough to automatically assert it.
        row += logFileDelimiter;

        //

        if (mappingCategory.equals("EXCLUDED")) {
            row += appendNull(2);
        } else {
            row += (attribute.getNumberOfZoomaResultsBeforeFilter());   //  This indicates the number of results that Zooma found before filters applied
            row += logFileDelimiter;

            //
            row += (attribute.getNumberOfZoomaResultsAfterFilter());   //  This indicates the number of results that Zooma found based on the input parameters. 0 denotes no results meet criteria, 1 denotes automated curation, >1 denotes needs curation.
            row += logFileDelimiter;
        }

        //
        row += standardiseNulls(attribute.getBasisForExclusion());   //  This indicates the number of results that Zooma found before filters applied
        row += logFileDelimiter;

        // //
        if (attribute.annotationSummary != null) {
            row += standardiseNulls(attribute.annotationSummary.getID());
            row += logFileDelimiter;
            row += (attribute.annotationSummary.getQuality());
            row += logFileDelimiter;

        } else {
            row += appendNull(2);
        }

        // //
        if (attribute.runnerUpAnnotation != null) {

            row += (attribute.runnerUpAnnotation.getQuality());
            row += logFileDelimiter;

            String runnerUpTermLabel = compareStrings(attribute.getRunnerUpTermLabel(), attribute.getOriginalTermValue());

            row += (runnerUpTermLabel); // If your term resulted in a Zooma mapping, this contains the label of the class in the ontology that Zooma mapped to
            row += logFileDelimiter;

        } else {
            row += appendNull(2);
        }

        //
        row += standardiseNulls(attribute.getErrorMessage());
        row += logFileDelimiter;

        //
        row += (configLogString);

        return row;
    }

    private static String standardiseNulls(String string) {
        if (string == null || string.equals("")) return null;
        else return string;
    }

//    public void printCurationRowsToFile(String outfileBasePath, String study) {
//
//        String headerLine = "";
//
//        for (String header : headers) {
//            headerLine += (header + logFileDelimiter);
//        }
//
//        headerLine += (getConfigLogHeaders());
//
//        printLogRowsToFile(outfileBasePath, study, headerLine, combinedCurationLogFileRows);
//    }

    public void printLogRowsToFile(String outfileBasePath, String study) {

        String headerLine = "";

        for (String header : headers) {
            headerLine += (header + logFileDelimiter);
        }

        headerLine += (getConfigLogHeaders());

        printLogRowsToFile(outfileBasePath, study, headerLine, combinedLogFileRows);

    }

    public void printLogRowsToFile(String outfileBasePath, String study, String headerLine, ArrayList<String> rows) {
        // sort the attributes to make it easier on the curator
        Collections.sort(rows);

        System.out.println("Printing " + rows.size() + " rows to file: " + outfileBasePath + study + "-zoomifications-log.tsv");

        PrintWriter out = null; //todo: change file ending
        try {
            out = new PrintWriter(new BufferedWriter(new FileWriter(outfileBasePath + study + "-zoomifications-log.tsv", false)));

            if (headerLine != null) out.println(headerLine);
            else getLog().error("Header line is missing.");

            for (String line : rows) {
                out.println(line);
            }

            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();  //todo:
        }
    }

    private String setConfigLogString() {

        String[] configs = {
                limpopoPath,
                zoomaPath,
                String.valueOf(cutoffScore),
                String.valueOf(cutoffPercentage),
                String.valueOf(minStringLength),
                String.valueOf(olsShortIds),
                String.valueOf(overwriteValues),
                String.valueOf(overwriteAnnotations),
                String.valueOf(stripLegacyAnnotations),
        };

        String configString = "";

        for (String config : configs) {
            configString += config;
            configString += logFileDelimiter;
        }

        configString += new Date();

        return configString;
    }

    private String getConfigLogHeaders() {
        String[] headers = {
                "limpopoPath",
                "zoomaPath",
                "zoomaCutoffScore",
                "zoomaCutoffPercentage",
                "minStringLength",
                "olsShortIds",
                "overwriteValues",
                "overwriteAnnotations",
                "stripLegacyAnnotations",
        };

        String headersString = "";

        for (String header : headers) {
            headersString += header;
            headersString += logFileDelimiter;
        }

        headersString += "Date run";

        return headersString;
    }

    private static String compareStrings(String string1, String string2) {
        if (string1 == null || string1.equals("")) return null;
        else return (string1.equalsIgnoreCase(string2)) ? string1 + "|ExactMatch" : string1 + "|" + string2;
    }

    protected Logger getLog() {
        return log;
    }

    private String appendNull(int iterations) {
        String delimitedNulls = "";
        for (int i = 0; i < iterations; i++) {
            delimitedNulls += null + logFileDelimiter;
        }
        return delimitedNulls;
    }
}
