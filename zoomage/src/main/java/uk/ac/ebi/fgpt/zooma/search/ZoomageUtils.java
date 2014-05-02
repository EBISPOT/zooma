package uk.ac.ebi.fgpt.zooma.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.zooma.model.AnnotationSummary;
import uk.ac.ebi.fgpt.zooma.model.Property;
import uk.ac.ebi.fgpt.zooma.model.SimpleTypedProperty;
import uk.ac.ebi.fgpt.zooma.util.ZoomaUtils;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * User: jmcmurry
 * Date: 19/12/2013
 * Time: 14:01
 */
public class ZoomageUtils {

    private static final Logger log = LoggerFactory.getLogger(ZoomageUtils.class);
    private static boolean olsShortIds;
    private static String compoundAnnotationDelimiter;
    private static ZOOMASearchClient zoomaClient;
    private static float cutoffScoreForAutomaticCuration;
    private static float cutoffPercentageForAutomaticCuration;
    private static int minStringLength;  // todo: move this to the zooma search rest httpClient
    protected static HashMap<String, boolean[]> cacheOfExclusionsApplied;
    private static HashMap<String, TransitionalAttribute> masterCache;
    private static ArrayList<ExclusionProfileAttribute> exclusionProfiles;

    private static final ZoomageUtils INSTANCE = new ZoomageUtils();

    // Private constructor prevents instantiation from other classes
    private ZoomageUtils() {
    }

    public static ZoomageUtils getInstance() {
        return INSTANCE;
    }

    public static void initialise(String zoomaPath, float cutoffScoreForAutomaticCuration, float cutoffPercentageForAutomaticCuration, int minStringLength, String exclusionProfilesResource, String exclusionProfilesDelimiter, boolean olsShortIds, String compoundAnnotationDelimiter) {
        try {
            zoomaClient = new ZOOMASearchClient(URI.create(zoomaPath).toURL());
        } catch (MalformedURLException e) {
            throw new RuntimeException("Failed to create ZOOMASearchClient", e);
        }

        ZoomageUtils.minStringLength = minStringLength;
        ZoomageUtils.cutoffScoreForAutomaticCuration = cutoffScoreForAutomaticCuration;
        ZoomageUtils.cutoffPercentageForAutomaticCuration = cutoffPercentageForAutomaticCuration;
        ZoomageUtils.exclusionProfiles = parseExclusionProfiles(exclusionProfilesResource, exclusionProfilesDelimiter);
        ZoomageUtils.olsShortIds = olsShortIds;
        ZoomageUtils.compoundAnnotationDelimiter = compoundAnnotationDelimiter;

        masterCache = new HashMap<String, TransitionalAttribute>();
        exclusionProfiles = parseExclusionProfiles(exclusionProfilesResource, exclusionProfilesDelimiter);
        cacheOfExclusionsApplied = new HashMap<String, boolean[]>();
    }

    public static void clearMasterCache() {
        masterCache = new HashMap<String, TransitionalAttribute>();
    }


    /**
     * Zooma supports the edge case in which there is a compound term with corresponding (compound) accessions.
     * eg: heart and lung
     * Ultimately, the MAGETAB parser should support this edge case too, but until it does, this method
     * will concatenate the URIs into a single string.
     *
     * @param zoomaAnnotationSummary
     * @return ArrayList with two elements, the first is the reference, and the second the accession.
     */
    public static ArrayList<String> concatenateCompoundURIs(AnnotationSummary zoomaAnnotationSummary) {

        if (zoomaAnnotationSummary == null) getLog().debug("Zooma annotation summary is null.");

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

            String termSourceAccession = parseAccession(uri);
            String termSourceRef = null;

            try {
                termSourceRef = termSourceAccession.substring(0, termSourceAccession.indexOf(":"));
            } catch (StringIndexOutOfBoundsException e) {
                log.error("Term source Reference could not be parsed for " + termSourceAccession + ": " + zoomaAnnotationSummary.toString());
            }

            refAndAccession.add(termSourceRef);
            refAndAccession.add(termSourceAccession);
        }

        // for the edge cases: compound URIs
        else if (semanticTags.size() > 1) {

            String compoundTermSourceRef = "";
            String compoundAccession = "";

            //todo: MAGETAB parser to handle this ultimately
            getLog().warn("Compound URI detected; MAGETAB parser not yet able to handle this edge case.");

            // for each URI build up corresponding delimited strings for refs and accessions
            for (URI semanticTag : semanticTags) {
                String accession = parseAccession(semanticTag);
                String uri = String.valueOf(semanticTag);
                int delimiterIndex = parseDelimIndex(semanticTag);
                String shortNamespace = accession.substring(0, accession.indexOf("_"));
                compoundTermSourceRef += shortNamespace + compoundAnnotationDelimiter;

                if (olsShortIds) {
                    String olsShortId = uri.substring(delimiterIndex).replace("_", ":");
                    compoundAccession += olsShortId + compoundAnnotationDelimiter;
                } else {
                    compoundAccession += uri + compoundAnnotationDelimiter;
                }
            }

            compoundTermSourceRef = removeTrailingDelimiter(compoundTermSourceRef, compoundAnnotationDelimiter);

            refAndAccession.add(compoundTermSourceRef);
            refAndAccession.add(compoundAccession);
        }

        return refAndAccession;
    }

    public static String getCompoundTermSourceRef(AnnotationSummary annotationSummary) {
        return concatenateCompoundURIs(annotationSummary).get(0);
    }

    public static String getCompoundTermSourceAccession(AnnotationSummary annotationSummary) {
        return concatenateCompoundURIs(annotationSummary).get(1);
    }

    public static String parseAccession(URI semanticTag) {

        String tag = String.valueOf(semanticTag);

        String accession = null;

        if (olsShortIds) {
            try {
                int delimiterIndex = parseDelimIndex(semanticTag);
                accession = tag.substring(delimiterIndex).replace("_", ":");
            } catch (StringIndexOutOfBoundsException e) {
                getLog().error("Accession could not be parsed from " + tag);
            }
        }

        return accession;
    }

    public static int parseDelimIndex(URI semanticTag) {
        String uri = String.valueOf(semanticTag);
        return Math.max(uri.lastIndexOf("/"), uri.lastIndexOf("#")) + 1;
    }

    /**
     * Get best Zooma AnnotationSummary for specified attribute type and preliminaryStringValue
     *
     * @param attributeType (eg: organism)
     * @return cleanedAttributeType
     */
    public static String normaliseType(String attributeType) {

        if (attributeType == null) {
            getLog().warn("Attribute type is null.");
            return null;
        }

        String cleanedAttributeType = attributeType;

        // handle camel cased property types and split into words separated by spaces
        String[] attributeTypeWords = attributeType.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");

        // if there was camel casing
        if (attributeTypeWords.length > 1) {

            // reset this preliminaryStringValue
            cleanedAttributeType = "";

            // stitch the words back together with spaces
            for (String word : attributeTypeWords) {
                cleanedAttributeType += word + " ";
            }
        }

        // further clean this preliminaryStringValue of extra spaces
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

//    private static Logger log = LoggerFactory.getLogger(TextUtils.class);

    // determines whether the two strings are acceptably fuzzy matched based on the edit distance between them
    // is case-insensitive
    public static boolean isFuzzyMatch(String entity1, String entity2, int maxNumDiffs, double maxPctDiffs) {

        if (entity1 == null || entity2 == null || entity1.equals("") || (entity2.equals(""))) return false;

        entity1 = entity1.toLowerCase().trim();
        entity2 = entity2.toLowerCase().trim();

        if (entity1.charAt(0) != entity2.charAt(0)) return false;

        int diffs = new DamerauLevenshtein(entity1, entity2).getNumDiffs();

        if (diffs == 0) return true;
        if (diffs / entity1.length() < .5)
            log.debug("Comparing \"" + entity1 + "\" with \"" + entity2 + "\": " + diffs + " differences out of " + +entity1.length() + " chars in original string.");

        boolean isFuzzyMatch = (diffs < maxNumDiffs) && ((double) diffs / entity1.length() <= maxPctDiffs);

        if (!isFuzzyMatch && diffs < 6 && entity1.length() > 20)
            log.debug("isFuzzyMatch result:" + isFuzzyMatch + "\t\tentity: \t" + entity1 + "\t\ttext: " + entity2 + "\tdiffs:" + diffs + "\tpctDiffs:" + (double) diffs / entity1.length());

        return isFuzzyMatch;
    }

    public static void setCompoundAnnotationDelimiter(String compoundAnnotationDelimiter) {
        ZoomageUtils.compoundAnnotationDelimiter = compoundAnnotationDelimiter;
    }

    public static void setOlsShortIds(boolean _olsShortIds) {
        ZoomageUtils.olsShortIds = _olsShortIds;
    }

    public static String getLabel(AnnotationSummary annotationSummary) {
        URI uri = annotationSummary.getSemanticTags().iterator().next();
        String ontLabel = null;
        try {
            ontLabel = zoomaClient.getLabel(uri);
        } catch (IOException e) {
            e.printStackTrace();  //todo:
        }
        return ontLabel;
    }

    public static TransitionalAttribute getZoomaResults(TransitionalAttribute baselineAttribute) {

        String input = baselineAttribute.getNormalisedType() + ":" + baselineAttribute.getOriginalTermValue();

        TransitionalAttribute zoomifiedAttribute = null;

        if (masterCache.containsKey(input)) {

            zoomifiedAttribute = masterCache.get(input);

        } else {
            ZoomaResultsProfile zoomaResultsProfile = null;

            try {
                zoomaResultsProfile = new ZoomaResultsProfile(baselineAttribute.getOriginalType(), baselineAttribute.getOriginalTermValue(), cutoffScoreForAutomaticCuration, cutoffPercentageForAutomaticCuration, zoomaClient);
                zoomifiedAttribute = applyZoomificationsToTransitionalAttribute(baselineAttribute, zoomaResultsProfile);
                putInMasterCacheWithoutOverwriting(input, zoomifiedAttribute);
            } catch (ZoomaException e) {
                getLog().warn(e.getMessage());
            }
        }

        return zoomifiedAttribute;
    }

    private static TransitionalAttribute applyZoomificationsToTransitionalAttribute(TransitionalAttribute attribute, ZoomaResultsProfile zoomaResultsProfile) {
        TransitionalAttribute zoomifiedAttribute = attribute;

        zoomifiedAttribute.setZoomaMappingCategory(zoomaResultsProfile.getMappingCategory());

        zoomifiedAttribute.setAnnotationSummary(zoomaResultsProfile.getAutomaticAnnotation());
        zoomifiedAttribute.setRunnerUpAnnotation(zoomaResultsProfile.getRunnerUpAnnotation());

        zoomifiedAttribute.setNumberOfUnfilteredResults(zoomaResultsProfile.getNumberOfUnfilteredResults());
        zoomifiedAttribute.setNumberOfFilteredResults(zoomaResultsProfile.getNumberOfFilteredResults());

        zoomifiedAttribute.setErrorMessage(zoomaResultsProfile.getErrorMessage());

        return zoomifiedAttribute;
    }

    protected static boolean excludeAttribute(TransitionalAttribute transAttribute) {
        if (cacheOfExclusionsApplied.get(transAttribute.getFields()) != null) return true;
        else return checkAllExclusionProfiles(transAttribute);
    }

    private static boolean checkAllExclusionProfiles(TransitionalAttribute transAttribute) {


        boolean exclude = false;

        // Check the cache of exclusions applied
        if (cacheOfExclusionsApplied.get(transAttribute.getFields()) != null) {
            log.debug("Retrieved " + Arrays.toString(transAttribute.getFields()) + " from exclusions cache.");
            exclude = true;
            return exclude;
        }

        String input = transAttribute.getNormalisedType() + ":" + transAttribute.getOriginalTermValue();


        // If it has not previously been excluded, check the string length
        if (transAttribute.getOriginalTermValue().length() < minStringLength) {

            // if it is less than the minimum
            log.debug("'" + transAttribute.getOriginalTermValue() + "' does not meet the specified length requirement of " + minStringLength + " characters and will not be zoomified.");

            // set the reason for exclusion
            transAttribute.setBasisForExclusion("minStringLength. ");


            // cache it
            if (!ZoomageUtils.getMasterCache().containsKey(input)) {
                putInMasterCacheWithoutOverwriting(input, transAttribute);
            }

            cacheOfExclusionsApplied.put(Arrays.toString(transAttribute.getFields()), null);

            exclude = true;
            return exclude;
        }


        // otherwise, if it DOES meet the length requirements, then check if it matches any exclusion profile
        for (ExclusionProfileAttribute exclusionProfile : exclusionProfiles) {

            if (transAttribute.excludeBasedOn(exclusionProfile)) {

                // if it does match an exclusion profile, cache the exclusion
                if (!ZoomageUtils.getMasterCache().containsKey(input)) {
                    putInMasterCacheWithoutOverwriting(input, transAttribute);
                }

                cacheOfExclusionsApplied.put(Arrays.toString(transAttribute.getFields()), null);

                exclude = true;
                return exclude;
            }
        }

        return exclude;
    }


    private static ArrayList<ExclusionProfileAttribute> parseExclusionProfiles(String appResourcesPath, String delim) {

        String exclusionProfilesResource = appResourcesPath += "zoomage-exclusions.tsv";

        ArrayList<ExclusionProfileAttribute> exclusionProfiles = new ArrayList<ExclusionProfileAttribute>();

        // read sources from file
        try {
            InputStream in = OptionsParser.getInputStreamFromFilePath(ZoomageUtils.class, exclusionProfilesResource);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String exclusionLine;
            while ((exclusionLine = reader.readLine()) != null) {
                if (!exclusionLine.startsWith("#") && !exclusionLine.isEmpty()) {
                    int indexFirstDelim = exclusionLine.indexOf(delim);
                    if (indexFirstDelim != 0) {
                        String type;
                        if (indexFirstDelim == -1)
                            type = exclusionLine;
                        else type = exclusionLine.substring(0, indexFirstDelim);
                        String normalisedType = ZoomageUtils.normaliseType(type);

                        if (!type.equalsIgnoreCase(normalisedType)) {
                            //todo: check that this doesn't replace more than the type
                            exclusionLine = exclusionLine.replace(type + delim, normalisedType + delim);
                        }
                    }

                    ExclusionProfileAttribute exclusionProfileAttribute = new ExclusionProfileAttribute(exclusionLine, delim);
                    exclusionProfiles.add(exclusionProfileAttribute);
                }
            }
        } catch (FileNotFoundException e) {
            getLog().error("Failed to load stream: could not locate file '" + exclusionProfilesResource + "'.  " +
                    "No properties will be excluded");
        } catch (IOException e) {
            getLog().error("Failed to load stream: could not read file '" + exclusionProfilesResource + "'.  " +
                    "No properties will be excluded");
        } catch (URISyntaxException e) {
            e.printStackTrace();  //todo:
        }

        return exclusionProfiles;
    }


    public static HashMap<String, TransitionalAttribute> getMasterCache() {
        return masterCache;
    }

    private static void putInMasterCacheWithoutOverwriting(String key, TransitionalAttribute value) {
        try {
            if (masterCache.containsKey(key)) {
                String errorMessage = "Overwriting item in mastercache: " + key;

                log.warn(errorMessage
                        + "\n" + ZoomageLogger.transitionalAttributeToLogRow(value, ",", "")
                        + "\n" + ZoomageLogger.transitionalAttributeToLogRow(masterCache.get(key), ",", ""));

                throw new IllegalArgumentException();

            } else {
                masterCache.put(key, value);
                log.debug("New item now in mastercache: " + key);
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }


}
