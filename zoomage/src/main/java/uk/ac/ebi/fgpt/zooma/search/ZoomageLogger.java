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
    private final boolean addCommentsToSDRF;
    private final String zoomaPath;
    private final String limpopoPath;
    private String configLogString;
    protected ArrayList<String> combinedLogFileRows = new ArrayList<String>();
    private ArrayList<String> combinedCurationLogFileRows = new ArrayList<String>();

    // Print the headers
    private String[] headers = {"PROPERTY_TYPE", "PROPERTY_VALUE", "STUDY", "BIOENTITY", "Original Ont Source", "Original Ont Source Id",
            "Matching Zooma Input", "Zooma Ont Label", "Zoomified Ont Source", "Zoomified Ont Source ID", "Category of Zooma Mapping",
            "# Results before filter", "# Results after filter", "Basis for Exclusion", "Annotation Summary ID", "Annotation Summary Score",
//                "RunnerUp Annotation Summary ID",
            "RunnerUp Annotation Summary Score",
//                "RunnerUp Matching Zooma Input",
            "RunnerUp Zooma Ont Label",
//                "RunnerUp Zoomified Ont Source", "RunnerUp Zoomified Ont Source ID",
            "Zooma Error Message"};


    public ZoomageLogger(float cutoffScore, int minStringLength, float cutoffPercentage, boolean olsShortIds,
                         String logFileDelimiter, boolean overwriteValues,
                         boolean overwriteAnnotations, boolean stripLegacyAnnotations, boolean addCommentsToSDRF,
                         String zoomaPath, String limpopoPath) {

        this.cutoffScore = cutoffScore;
        this.minStringLength = minStringLength;
        this.cutoffPercentage = cutoffPercentage;
        this.olsShortIds = olsShortIds;
        this.logFileDelimiter = logFileDelimiter;
        this.overwriteValues = overwriteValues;
        this.overwriteAnnotations = overwriteAnnotations;
        this.stripLegacyAnnotations = stripLegacyAnnotations;

        this.addCommentsToSDRF = addCommentsToSDRF;
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

//    public ArrayList<String> formatCacheAsBulkCuration(HashMap<String, TransitionalAttribute> masterCache) {
//
//        ArrayList<String> curationRows = new ArrayList<>();
//
//        // Print store each attribute as a string.
//        for (TransitionalAttribute attribute : masterCache.values()) {
//
//            if (attribute.getCategoryOfZoomaMapping() == ZoomaResultsProfile.MappingCategory.REQUIRES_CURATION) {
//                String row = transitionalAttributeAsCurationRow(attribute);
//                curationRows.add(row);
//            }
//
//        }
//
//        return curationRows;
//
//    }

//    private String transitionalAttributeAsCurationRow(TransitionalAttribute attribute) {
//        //STUDY,BIOENTITY,PROPERTY_TYPE,PROPERTY_VALUE,SEMANTIC_TAG,ANNOTATOR,ANNOTATION_DATE
//        String row = "";
//
//        // STUDY
//        row += attribute.getStudy();
//        row += ",";
//
//        // BIOENTITY
//        row += attribute.getBioentity();
//        row += ",";
//
//        //PROPERTY_TYPE
//        row += attribute.getOriginalType();
//        row += ",";
//
//        //PROPERTY_VALUE
//        row += attribute.getorigi();
//        row += ",";
//
//        //SEMANTIC_TAG
//        row += attribute.getRunnerUpOntAccession();
//        row += ",";
//
//        //ANNOTATOR (Curator's name)
//        row += "FIRST LAST";
//        row += ",";
//
//        //ANNOTATION_DATE
//
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        row += sdf.format(new Date());
//
//        return row;  //To change body of created methods use File | Settings | File Templates.
//    }

    public String transitionalAttributeToLogRow(TransitionalAttribute attribute) {
        return transitionalAttributeToLogRow(attribute, configLogString);
    }

    public String transitionalAttributeToLogRow(TransitionalAttribute attribute, String configLogString) {
        String row = "";

        //0
        row += standardiseNulls(attribute.getOriginalType());
        row += (logFileDelimiter);

        //1
        if (!attribute.getOriginalTermValue().contains(logFileDelimiter))
            row += standardiseNulls(attribute.getOriginalTermValue()); // 	This is the text preliminaryStringValue supplied as part of the submitted file.
        else row += standardiseNulls(attribute.getOriginalTermValue().replaceAll(logFileDelimiter, " "));
        row += (logFileDelimiter);

        //2
        row += standardiseNulls(attribute.getStudy());
        row += (logFileDelimiter);

        //3
        row += standardiseNulls(attribute.getOriginalTermSourceREF()); // If your term had a pre-existing annotation, this contains the source of this mapping.
        row += (logFileDelimiter);

        //4
        row += standardiseNulls(attribute.getOriginalTermAccessionNumber());
        row += (logFileDelimiter);

        //5
        String zoomifiedTermValue = compareStrings(attribute.getZoomifiedTermValue(), attribute.getOriginalTermValue());

        row += standardiseNulls(zoomifiedTermValue);      // 	This is most often identical to the text preliminaryStringValue supplied as part of your search, but occasionally Zooma determines is close enough to a text preliminaryStringValue previously determined to map to a given ontology term.
        row += (logFileDelimiter);

        //6
        row += compareStrings(attribute.getZoomifiedOntologyClassLabel(), attribute.getOriginalTermValue()); // If your term resulted in a Zooma mapping, this contains the label of the class in the ontology that Zooma mapped to
        row += (logFileDelimiter);

        //7
        row += standardiseNulls(attribute.getZoomifiedTermSourceREF()); // If your term resulted in a Zooma mapping, this contains the source of this mapping. This is usually a dataset in which a similar property preliminaryStringValue was found annotated to the suggested ontology class.;
        row += (logFileDelimiter);

        //8
        row += standardiseNulls(attribute.getZoomifiedOntAccession()); // If your term resulted in a Zooma mapping, this contains the id of the class in the ontology that Zooma mapped to
        row += (logFileDelimiter);

        //9

        String mappingCategory = String.valueOf(attribute.getCategoryOfZoomaMapping());
        if (attribute.getBasisForExclusion() != null && !attribute.getBasisForExclusion().isEmpty())
            mappingCategory = "EXCLUDED";

        row += (mappingCategory);    // 	This indicates how confident ZOOMA was with the mapping. "Automatic" means ZOOMA is highly confident and "Requires curation" means ZOOMA found at least match that might fit but ZOOMA is not confident enough to automatically assert it.
        row += (logFileDelimiter);

        //10
        row += (attribute.getNumberOfZoomaResultsBeforeFilter());   //  This indicates the number of results that Zooma found before filters applied
        row += (logFileDelimiter);

        //11
        row += (attribute.getNumberOfZoomaResultsAfterFilter());   //  This indicates the number of results that Zooma found based on the input parameters. 0 denotes no results meet criteria, 1 denotes automated curation, >1 denotes needs curation.
        row += (logFileDelimiter);

        //12
        row += standardiseNulls(attribute.getBasisForExclusion());   //  This indicates the number of results that Zooma found before filters applied
        row += (logFileDelimiter);

        //13-14
        if (attribute.annotationSummary != null) {
            row += standardiseNulls(attribute.annotationSummary.getID());
            row += (logFileDelimiter);
            row += (attribute.annotationSummary.getQuality());
            row += (logFileDelimiter);

        } else {
            row += ((String) null);
            row += (logFileDelimiter);
            row += ((String) null);
            row += (logFileDelimiter);
        }

        //15-16
        if (attribute.runnerUpAnnotation != null) {

//                row += (attribute.runnerUpAnnotation.getID());
//                row += (logFileDelimiter);
//
            row += (attribute.runnerUpAnnotation.getQuality());
            row += (logFileDelimiter);
//
//                String runnerUpTermValue = (attribute.getZoomifiedTermValue() != null && attribute.getZoomifiedTermValue().equalsIgnoreCase(attribute.getZoomifiedTermValue())) ? "~" : attribute.getZoomifiedTermValue();
//
//                row += (runnerUpTermValue);      // 	This is most often identical to the text preliminaryStringValue supplied as part of your search, but occasionally Zooma determines is close enough to a text preliminaryStringValue previously determined to map to a given ontology term.
//                row += (logFileDelimiter);

            String runnerUpTermLabel = compareStrings(attribute.getRunnerUpTermLabel(), attribute.getOriginalTermValue());

            row += (runnerUpTermLabel); // If your term resulted in a Zooma mapping, this contains the label of the class in the ontology that Zooma mapped to
            row += (logFileDelimiter);


//                row += (attribute.getRunnerUpTermSourceRef()); // If your term resulted in a Zooma mapping, this contains the source of this mapping. This is usually a dataset in which a similar property preliminaryStringValue was found annotated to the suggested ontology class.
//                row += (logFileDelimiter);
//
//                row += (attribute.getRunnerUpOntAccession()); // If your term resulted in a Zooma mapping, this contains the source of this mapping. This is usually a dataset in which a similar property preliminaryStringValue was found annotated to the suggested ontology class.;
//                row += (logFileDelimiter);


        } else {
            row += ((String) null);
            row += (logFileDelimiter);
            row += ((String) null);
            row += (logFileDelimiter);
//                row += ((String) null);
//                row += (logFileDelimiter);
//                row += ((String) null);
//                row += (logFileDelimiter);
//                row += ((String) null);
//                row += (logFileDelimiter);
//                row += ((String) null);
//                row += (logFileDelimiter);
        }

        //17
        row += standardiseNulls(attribute.getErrorMessage());
        row += (logFileDelimiter);

        //18
        row += (configLogString);

        return row;
    }

    private static String standardiseNulls(String string) {
        if (string == null || string.equals("")) return null;
        else return string;
    }

    public void printCurationRowsToFile(String outfileBasePath, String study) {

        String headerLine = "";

        for (String header : headers) {
            headerLine += (header + logFileDelimiter);
        }

        headerLine += (getConfigLogHeaders());

        printLogRowsToFile(outfileBasePath, study, headerLine, combinedCurationLogFileRows);
    }

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
        } catch (IOException e) {
            e.printStackTrace();  //todo:
        }


        out.println(headerLine);

        for (String line : rows) {
            out.println(line);
        }

        out.flush();
        out.close();
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
                String.valueOf(addCommentsToSDRF),
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
                "addCommentsToSDRF"
        };

        String headersString = "";

        for (String header : headers) {
            headersString += header;
            headersString += logFileDelimiter;
        }

        headersString += "Date run";

        return headersString;
    }

//    public void addLogFileRowsForSingleAccession(ArrayList<String> logFileRowsForSingleAccession) {
//        combinedLogFileRows.addAll(logFileRowsForSingleAccession);
//    }

    private static String compareStrings(String string1, String string2) {
        if (string1 == null || string1.equals("")) return null;
        else return (string1.equalsIgnoreCase(string2)) ? string1 + "|ExactMatch" : string1 + "|" + string2;
    }


    public void addCurationRowsForSingleAccession(ArrayList<String> curationRowsForSingleAccession) {
        combinedCurationLogFileRows.addAll(curationRowsForSingleAccession);
    }
}
