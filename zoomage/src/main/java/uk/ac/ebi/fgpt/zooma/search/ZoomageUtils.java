package uk.ac.ebi.fgpt.zooma.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.zooma.exception.SearchException;
import uk.ac.ebi.fgpt.zooma.model.AnnotationSummary;
import uk.ac.ebi.fgpt.zooma.util.URIUtils;

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
    private static List<String> requiredSources;

    private static final ZoomageUtils INSTANCE = new ZoomageUtils();

    // Private constructor prevents instantiation from other classes
    private ZoomageUtils() {
    }

    public static ZoomageUtils getInstance() {
        return INSTANCE;
    }

    public static void initialise(String zoomaPath, float cutoffScoreForAutomaticCuration, float cutoffPercentageForAutomaticCuration, int minStringLength, String exclusionProfilesResource, String exclusionProfilesDelimiter, boolean olsShortIds, String compoundAnnotationDelimiter, List<String> requiredSources) {
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
        ZoomageUtils.requiredSources = requiredSources;

        ZoomageUtils.masterCache = new HashMap<String, TransitionalAttribute>();
        ZoomageUtils.cacheOfExclusionsApplied = new HashMap<String, boolean[]>();
    }

    public static void clearMasterCache() {
        masterCache = new HashMap<String, TransitionalAttribute>();
    }


    public static ArrayList<String> parseRefsAndAccessions(AnnotationSummary zoomaAnnotationSummary, boolean longform) {
        return parseRefsAndAccessions(zoomaAnnotationSummary, olsShortIds, longform);
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
    public static ArrayList<String> parseRefsAndAccessions(AnnotationSummary zoomaAnnotationSummary, boolean olsShortIds, boolean longForm) throws IllegalArgumentException {

        if (zoomaAnnotationSummary == null) {
            String msg = "Zooma annotation summary is null.";
            getLog().debug(msg);
            throw new IllegalArgumentException(msg);
        }

        // set termSourceREF and termAccessionNumber
        Collection<URI> semanticTags = zoomaAnnotationSummary.getSemanticTags();
        ArrayList<String> refAndAccession = new ArrayList<String>();

        if (semanticTags == null || semanticTags.size() == 0) {
            // since we've already checked to see that there is a Zooma annotation, this should never happen.
            getLog().error("No semantic tags detected for " + zoomaAnnotationSummary.getAnnotatedPropertyValue());
            refAndAccession.add(null);
            refAndAccession.add(null);

        } else {

            String compoundTermSourceRef = "";
            String compoundAccession = "";

            Iterator<URI> uriIterator = semanticTags.iterator();
            // for each URI build up corresponding delimited strings for refs and accessions
            while (uriIterator.hasNext()) {

                URI semanticTag = uriIterator.next();
                String accession = "";
                if (longForm) accession = semanticTag.toString();
                else accession = URIUtils.extractFragment(semanticTag);

                String ref = URIUtils.extractNamespace(semanticTag).toString();

                if (olsShortIds && !longForm) {
                    String olsShortId = accession.replace("_", ":");
                    compoundAccession += olsShortId;
                } else {
                    compoundAccession += accession;
                }

                compoundTermSourceRef += ref;

                if (uriIterator.hasNext()) {
                    compoundAccession += compoundAnnotationDelimiter;
                    compoundTermSourceRef += compoundAnnotationDelimiter;
                }
            }

            refAndAccession.add(compoundTermSourceRef);
            refAndAccession.add(compoundAccession);

            if (semanticTags.size() > 1) {
                getLog().warn("Magetab parser not yet able to handle the edge-case of compound semantic tags." +
                        "\nTerm source reference will be concatenated to <" + compoundTermSourceRef + ">." +
                        "\nTerm accession will be concatenated to <" + compoundAccession + ">.");
            }
        }

        return refAndAccession;
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

    protected static Logger getLog() {
        return log;
    }


    public static String getLabel(AnnotationSummary zoomaAnnotationSummary) {

        if (zoomaAnnotationSummary == null) {
            String msg = "Zooma annotation summary is null.";
            getLog().debug(msg);
            throw new IllegalArgumentException(msg);
        }

        URI uri = zoomaAnnotationSummary.getSemanticTags().iterator().next();

        // note that in the edge case where there is a semantic tag, the label will not need to be concatenated.
        String ontLabel = null;

        try {
            ontLabel = zoomaClient.getLabel(uri);
        } catch (IOException e) {
            getLog().error("IO exception getting label from");
        } catch (SearchException e) {
            getLog().error(e.getMessage());
            ontLabel = URIUtils.extractFragment(uri);
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
                zoomaResultsProfile = new ZoomaResultsProfile(baselineAttribute.getOriginalType(), baselineAttribute.getOriginalTermValue(), cutoffScoreForAutomaticCuration, cutoffPercentageForAutomaticCuration, requiredSources, zoomaClient);
                zoomifiedAttribute = applyZoomificationsToTransitionalAttribute(baselineAttribute, zoomaResultsProfile);
                putInMasterCacheWithoutOverwriting(input, zoomifiedAttribute);
            } catch (ZoomaException e) {
                getLog().warn(e.getMessage());
                if (e.getMessage().contains("HTTP response code: 500")) {
                    getLog().error("Zooma appears to be unresponsive.");
                    System.exit(1);
                }
                baselineAttribute.setErrorMessage(e.getMessage());
                putInMasterCacheWithoutOverwriting(input, baselineAttribute);
                return baselineAttribute;
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

                log.warn(errorMessage);

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
