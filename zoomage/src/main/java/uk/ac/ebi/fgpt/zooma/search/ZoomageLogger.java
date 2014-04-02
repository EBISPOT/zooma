package uk.ac.ebi.fgpt.zooma.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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


    public void printLog(String outfileBasePath, String magetabAccession) {
        HashMap<String, TransitionalAttribute> masterCache = ZoomageUtils.getMasterCache();

        ArrayList<String> rows = new ArrayList<>();

        // Print store each attribute as a string.
        for (TransitionalAttribute attribute : masterCache.values()) {

            String row = "";

            row += (attribute.getAccession());
            row += (logFileDelimiter);

            row += (attribute.getOriginalType());
            row += (logFileDelimiter);

            if (!attribute.getOriginalTermValue().contains(logFileDelimiter))
                row += (attribute.getOriginalTermValue()); // 	This is the text preliminaryStringValue supplied as part of the submitted file.
            else row += (attribute.getOriginalTermValue().replaceAll(logFileDelimiter, " "));
            row += (logFileDelimiter);

            row += (attribute.getOriginalTermSourceREF()); // If your term had a pre-existing annotation, this contains the source of this mapping.
            row += (logFileDelimiter);

            row += (attribute.getOriginalTermAccessionNumber());
            row += (logFileDelimiter);

            String zoomifiedTermValue = (attribute.getZoomifiedTermValue() != null && attribute.getZoomifiedTermValue().equalsIgnoreCase(attribute.getOriginalTermValue())) ? "~" : attribute.getZoomifiedTermValue();

            row += (zoomifiedTermValue);      // 	This is most often identical to the text preliminaryStringValue supplied as part of your search, but occasionally Zooma determines is close enough to a text preliminaryStringValue previously determined to map to a given ontology term.
            row += (logFileDelimiter);

            row += (attribute.getZoomifiedOntologyClassLabel()); // If your term resulted in a Zooma mapping, this contains the label of the class in the ontology that Zooma mapped to
            row += (logFileDelimiter);

            row += (attribute.getZoomifiedTermSourceREF()); // If your term resulted in a Zooma mapping, this contains the source of this mapping. This is usually a dataset in which a similar property preliminaryStringValue was found annotated to the suggested ontology class.;
            row += (logFileDelimiter);

            row += (attribute.getZoomifiedOntAccession()); // If your term resulted in a Zooma mapping, this contains the id of the class in the ontology that Zooma mapped to
            row += (logFileDelimiter);

            row += (attribute.getCategoryOfZoomaMapping());    // 	This indicates how confident ZOOMA was with the mapping. "Automatic" means ZOOMA is highly confident and "Requires curation" means ZOOMA found at least match that might fit but ZOOMA is not confident enough to automatically assert it.
            row += (logFileDelimiter);

            row += (attribute.getNumberOfZoomaResultsBeforeFilter());   //  This indicates the number of results that Zooma found before filters applied
            row += (logFileDelimiter);

            row += (attribute.getNumberOfZoomaResultsAfterFilter());   //  This indicates the number of results that Zooma found based on the input parameters. 0 denotes no results meet criteria, 1 denotes automated curation, >1 denotes needs curation.
            row += (logFileDelimiter);

            row += (attribute.getBasisForExclusion());   //  This indicates the number of results that Zooma found before filters applied
            row += (logFileDelimiter);


            if (attribute.annotationSummary != null) {
                row += (attribute.annotationSummary.getID());
                row += (logFileDelimiter);
                row += (attribute.annotationSummary.getQuality());
                row += (logFileDelimiter);

            } else {
                row += ((String) null);
                row += (logFileDelimiter);
                row += ((String) null);
                row += (logFileDelimiter);
            }

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

                String runnerUpTermLabel = (attribute.getRunnerUpTermLabel() != null && attribute.getRunnerUpTermLabel().equalsIgnoreCase(attribute.getOriginalTermValue())) ? "~" : attribute.getRunnerUpTermValue();

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

            row += (attribute.isProducedZoomaError());
            row += (logFileDelimiter);

            row += (configLogString);

            rows.add(row);

        }

        // sort the attributes to make it easier on the curator
        Collections.sort(rows);

        PrintWriter out = null; //todo: change file ending
        try {
            out = new PrintWriter(new BufferedWriter(new FileWriter(outfileBasePath + magetabAccession + "-zoomifications-log.txt", false)));
        } catch (IOException e) {
            e.printStackTrace();  //todo:
        }

        // Print the headers
        String[] headers = {"Accession", "Type", "Original Term Value", "Original Ont Source", "Original Ont Source Id",
                "Matching Zooma Input", "Zooma Ont Label", "Zoomified Ont Source", "Zoomified Ont Source ID", "Category of Zooma Mapping",
                "# Results before filter", "# Results after filter", "Basis for Exclusion", "Annotation Summary ID", "Annotation Summary Score",
//                "RunnerUp Annotation Summary ID",
                "RunnerUp Annotation Summary Score",
//                "RunnerUp Matching Zooma Input",
                "RunnerUp Zooma Ont Label",
//                "RunnerUp Zoomified Ont Source", "RunnerUp Zoomified Ont Source ID",
                "Zooma Error Status"};

        String headerLine = "";

        for (String header : headers) {
            headerLine += (header + logFileDelimiter);
        }

        headerLine += (getConfigLogHeaders());

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
}
