package uk.ac.ebi.fgpt.zooma.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.zooma.model.AnnotationSummary;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: jmcmurry
 * Date: 19/12/2013
 * Time: 14:01
 * To change this template use File | Settings | File Templates.
 */
public class ZoomageTextUtils {

    private static final Logger log = LoggerFactory.getLogger(ZoomageTextUtils.class);
    private static HashMap<String, AnnotationSummary> resultsCache = new HashMap<String, AnnotationSummary>();


    /**
     * Zooma supports the edge case in which there is a compound term with corresponding (compound) accessions.
     * eg: heart and lung
     * Ultimately, the MAGETAB parser should support this edge case too, but until it does, this method
     * will concatenate the URIs into a single string.
     *
     * @param zoomaAnnotationSummary
     * @param delim
     * @return ArrayList with two elements, the first is the reference, and the second the accession.
     */
    public static ArrayList<String> concatenateCompoundURIs(AnnotationSummary zoomaAnnotationSummary, boolean olsShortIds, String delim) {

        // set termSourceREF and termAccessionNumber
        Collection<URI> semanticTags = zoomaAnnotationSummary.getSemanticTags();
        ArrayList<String> refAndAccession = new ArrayList<String>();

        if (semanticTags.size() == 0) {
            // since we've already checked to see that there is a Zooma annotation, this should never happen.
            getLog().error("No term source ref detected for " + zoomaAnnotationSummary.getAnnotatedPropertyValue());
            refAndAccession.add(null);
            refAndAccession.add(null);
            return refAndAccession;
        }

        Iterator iterator = semanticTags.iterator();


        // 99% of the time, there will be one URI
        if (semanticTags.size() == 1) {
            URI uri = (URI) iterator.next();

            String accession = parseAccession(uri, olsShortIds);
            String ref = accession.substring(0, accession.indexOf(":"));

            refAndAccession.add(ref);
            refAndAccession.add(accession);
        }

        // for the edge cases: compound URIs
        else if (semanticTags.size() > 1) {

            String compoundTermSourceRef = "";
            String compoundAccession = "";

            //todo: MAGETAB parser to handle this ultimately
            getLog().warn("Compound URI detected; MAGETAB parser not yet able to handle this edge case.");

            // for each URI build up corresponding delimited strings for refs and accessions
            for (URI semanticTag : semanticTags) {
                String uri = String.valueOf(semanticTag);
                int delimiterIndex = Math.max(uri.lastIndexOf("/"), uri.lastIndexOf("#")) + 1;
                String accession = uri.substring(delimiterIndex);
                String shortNamespace = accession.substring(0, accession.indexOf("_"));
                compoundTermSourceRef += shortNamespace + delim;

                if (olsShortIds) {
                    String olsShortId = uri.substring(delimiterIndex).replace("_", ":");
                    compoundAccession += olsShortId + delim;
                } else {
                    compoundAccession += uri + delim;
                }
            }

            compoundTermSourceRef = removeTrailingDelimiter(compoundTermSourceRef, delim);

            refAndAccession.add(compoundTermSourceRef);
            refAndAccession.add(compoundAccession);
        }

        return refAndAccession;
    }

    public static String parseAccession(URI semanticTag, boolean olsShortIds) {

        String tag = String.valueOf(semanticTag);

        if (olsShortIds) {
            int delimiterIndex = parseDelimIndex(semanticTag);
            tag = tag.substring(delimiterIndex).replace("_", ":");
        }

        return tag;
    }

    public static int parseDelimIndex(URI semanticTag) {
        String uri = String.valueOf(semanticTag);
        return Math.max(uri.lastIndexOf("/"), uri.lastIndexOf("#")) + 1;
    }

    /**
     * Get best Zooma AnnotationSummary for specified attribute type and value
     *
     * @param attributeType (eg: organism)
     * @return cleanedAttributeType
     */
    public static String normaliseType(String attributeType) {

        String cleanedAttributeType = attributeType;

        // handle camel cased property types and split into words separated by spaces
        String[] attributeTypeWords = attributeType.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");

        // if there was camel casing
        if (attributeTypeWords.length > 1) {

            // reset this value
            cleanedAttributeType = "";

            // stitch the words back together with spaces
            for (String word : attributeTypeWords) {
                cleanedAttributeType += word + " ";
            }
        }

        // further clean this value of extra spaces
        return cleanedAttributeType.trim().toLowerCase().replaceAll("//s+", " ");

    }

    public static String removeTrailingDelimiter(String originalString, String delimiter) {
        String stringSansTrailingDelim = "";

        int predictedLastIndexOfDelim = originalString.length() - delimiter.length();

        if (originalString.lastIndexOf(delimiter) == predictedLastIndexOfDelim) {
            stringSansTrailingDelim = originalString.substring(0, predictedLastIndexOfDelim);
            return stringSansTrailingDelim;
        } else return originalString;
    }


    protected static Logger getLog() {
        return log;
    }
}
