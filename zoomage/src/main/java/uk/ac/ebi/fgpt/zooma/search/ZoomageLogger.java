package uk.ac.ebi.fgpt.zooma.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.zooma.model.AnnotationSummary;

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
    private StringBuilder configLogString;
    protected ArrayList<String> combinedLogFileRows = new ArrayList<String>();
    private ArrayList<String> combinedCurationLogFileRows = new ArrayList<String>();

    // Print the headers
    private String[] headers = {
            "PROPERTY_TYPE", "PROPERTY_VALUE", "ZOOMA_LABEL|ZOOMA_VALUE", "PROP_VALUE_MATCH", "SEMANTIC_TAG", "CORRESPONDING_ZOOMA_SCORE", "ANNOTATOR", "ANNOTATION_DATE",
            "STUDY", "BIOENTITY", "Original Ont Source", "Original Ont Source Id",
            "Category of Zooma Mapping",
            "# Results before filter", "# Results after filter", "Basis for Exclusion",
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

            StringBuilder row = transitionalAttributeToLogRow(attribute);

            rows.add(row.toString());

        }

        return rows;

    }


    public StringBuilder transitionalAttributeToLogRow(TransitionalAttribute attribute) {
        return transitionalAttributeToLogRow(attribute, configLogString);
    }

    public StringBuilder transitionalAttributeToLogRow(TransitionalAttribute attribute, StringBuilder configLogString) {
        StringBuilder row = new StringBuilder();

        //"PROPERTY_TYPE", "PROPERTY_VALUE", "ZOOMA_LABEL|ZOOMA_VALUE","VALUE_COMPARISON", "SEMANTIC_TAG", "STUDY", "BIOENTITY", "CORRESPONDING_ZOOMA_SCORE", "ANNOTATOR", "ANNOTATION_DATE",

        row.append(standardiseNulls(attribute.getOriginalType()));
        row.append(logFileDelimiter);

        // PROPERTY VALUE

        String originalValue = attribute.getOriginalTermValue();

        row.append(standardiseNulls(originalValue.replaceAll(logFileDelimiter, " ")));
        row.append(logFileDelimiter);

        AnnotationSummary summary = attribute.getAnnotationSummary();
        if (summary == null) summary = attribute.getrunnerUpAnnotation();

        if (summary != null) {

            // CORRESPONDING_LABEL
            String label = ZoomageUtils.getLabel(summary);
            String input = summary.getAnnotatedPropertyValue();
            if ((label != null && input != null) && label.equalsIgnoreCase(input)) {
                row.append(label);
            } else {
                row.append(label + "|" + input);
            }
            row.append(logFileDelimiter);

            // COMPARISON
            String comparison = "";
            if (originalValue.equalsIgnoreCase(label)) {
                comparison = "MATCHES ZOOMA LABEL";
            } else if (originalValue.equalsIgnoreCase(attribute.getZoomifiedTermValue())) {
                comparison = "MATCHES ZOOMA INPUT";
            } else comparison = "mismatch";
            row.append(comparison);

            row.append(logFileDelimiter);

            // SEMANTIC_TAG
            row.append(ZoomageUtils.parseRefsAndAccessions(summary, true).get(1));
            row.append(logFileDelimiter);

            // CORRESPONDING_ZOOMA_SCORE
            row.append(summary.getQuality());
            row.append(logFileDelimiter);

            String curatorsName = "<FIRSTNAME LASTNAME>";
            if (attribute.getAnnotationSummary() != null) curatorsName = "Automated by Zooma";
            //ANNOTATOR (Curator's name)
            row.append(curatorsName);
            row.append(logFileDelimiter);

            //ANNOTATION_DATE
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            row.append(sdf.format(new Date()));
            row.append(logFileDelimiter);
        } else {
            row.append(appendNull(6));
        }

        //
        row.append(standardiseNulls(attribute.getStudy()));
        row.append(logFileDelimiter);

        //
        row.append(standardiseNulls(attribute.getBioentity()));
        row.append(logFileDelimiter);

        //
        row.append(standardiseNulls(attribute.getOriginalTermSourceREF())); // If your term had a pre-existing annotation, this contains the source of this mapping.
        row.append(logFileDelimiter);

        //
        row.append(standardiseNulls(attribute.getOriginalTermAccessionNumber()));
        row.append(logFileDelimiter);

//        //
//        String zoomifiedTermValue = compareStrings(attribute.getZoomifiedTermValue(), attribute.getOriginalTermValue());
//
//        row.append( standardiseNulls(zoomifiedTermValue);      // 	This is most often identical to the text preliminaryStringValue supplied as part of your search, but occasionally Zooma determines is close enough to a text preliminaryStringValue previously determined to map to a given ontology term.
//        row.append( logFileDelimiter);
//
//        //
//        row.append( compareStrings(attribute.getZoomifiedOntologyClassLabel(), attribute.getOriginalTermValue()); // If your term resulted in a Zooma mapping, this contains the label of the class in the ontology that Zooma mapped to
//        row.append( logFileDelimiter);
//
//        //
//        row.append( standardiseNulls(attribute.getZoomifiedTermSourceREF()); // If your term resulted in a Zooma mapping, this contains the source of this mapping. This is usually a dataset in which a similar property preliminaryStringValue was found annotated to the suggested ontology class.;
//        row.append( logFileDelimiter);
//
//        //
//        row.append( standardiseNulls(attribute.getZoomifiedOntAccession()); // If your term resulted in a Zooma mapping, this contains the id of the class in the ontology that Zooma mapped to
//        row.append( logFileDelimiter);

        //
        String mappingCategory = String.valueOf(attribute.getCategoryOfZoomaMapping());
        if (attribute.getBasisForExclusion() != null && !attribute.getBasisForExclusion().isEmpty())
            mappingCategory = "EXCLUDED";

        row.append((mappingCategory));    // 	This indicates how confident ZOOMA was with the mapping. "Automatic" means ZOOMA is highly confident and "Requires curation" means ZOOMA found at least match that might fit but ZOOMA is not confident enough to automatically assert it.
        row.append(logFileDelimiter);

        //

        if (mappingCategory.equals("EXCLUDED")) {
            row.append(appendNull(2));
        } else {
            row.append((attribute.getNumberOfZoomaResultsBeforeFilter()));   //  This indicates the number of results that Zooma found before filters applied
            row.append(logFileDelimiter);

            //
            row.append((attribute.getNumberOfZoomaResultsAfterFilter()));   //  This indicates the number of results that Zooma found based on the input parameters. 0 denotes no results meet criteria, 1 denotes automated curation, >1 denotes needs curation.
            row.append(logFileDelimiter);
        }

        //
        row.append(standardiseNulls(attribute.getBasisForExclusion()));   //  This indicates the number of results that Zooma found before filters applied
        row.append(logFileDelimiter);

//        // //
//        if (attribute.annotationSummary != null) {
//            row.append( standardiseNulls(attribute.annotationSummary.getID());
//            row.append( logFileDelimiter);
//            row.append( (attribute.annotationSummary.getQuality());
//            row.append( logFileDelimiter);
//
//        } else {
//            row.append( appendNull(2);
//        }
//
//        // //
//        if (attribute.runnerUpAnnotation != null) {
//
//            row.append( (attribute.runnerUpAnnotation.getQuality());
//            row.append( logFileDelimiter);
//
//            String runnerUpTermLabel = compareStrings(attribute.getRunnerUpTermLabel(), attribute.getOriginalTermValue());
//
//            row.append( (runnerUpTermLabel); // If your term resulted in a Zooma mapping, this contains the label of the class in the ontology that Zooma mapped to
//            row.append( logFileDelimiter);
//
//        } else {
//            row.append( appendNull(2);
//        }

        //
        String errorMsg = standardiseNulls(attribute.getErrorMessage());
        // strip punctuation from error message or it can mess with the delimiters
        if (errorMsg != null) errorMsg = errorMsg.replaceAll("[()]", " ");
        row.append(errorMsg);
        row.append(logFileDelimiter);

        //
        row.append(configLogString);

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

    private StringBuilder setConfigLogString() {

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

        StringBuilder configString = new StringBuilder();

        for (String config : configs) {
            configString.append(config);
            configString.append(logFileDelimiter);
        }

        configString.append(new Date());

        return configString;
    }

    private StringBuilder getConfigLogHeaders() {
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

        StringBuilder headersString = new StringBuilder();

        for (String header : headers) {
            headersString.append(header);
            headersString.append(logFileDelimiter);
        }

        headersString.append("Date run");

        return headersString;
    }

    private static String compareStrings(String string1, String string2) {
        if (string1 == null || string1.equals("")) return null;
        else return (string1.equalsIgnoreCase(string2)) ? "ExactMatch|" + string1 : string2 + "|" + string1;
    }

    protected Logger getLog() {
        return log;
    }

    private StringBuilder appendNull(int iterations) {
        StringBuilder delimitedNulls = new StringBuilder() ;
        for (int i = 0; i < iterations; i++) {
            delimitedNulls .append(null + logFileDelimiter);
        }
        return delimitedNulls;
    }
}
