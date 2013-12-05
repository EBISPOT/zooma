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
import java.net.URL;
import java.util.*;

/**
 * Class to access Zooma and parse resulting annotations
 */
public class ZoomaRESTClient {

    private final HashSet excludedProperties;
    private final Logger log = LoggerFactory.getLogger(getClass());

    private int minInputLength;       // minimum length of a string to match for annotations
    private float cutoffPercentage;   // cutoff percentage (eg. .8 keeps only the top 20%)
    private float cutoffScore;        // minimum score to accept as a zooma hit
    private boolean olsShortIds;     // yes if output should include term ids in their (OLS-compatible) short form rather than full URI
    private boolean overwriteAnnotations;     // yes if existing annotations should be stripped and, where available, overwritten
    private static HashMap<String, AnnotationSummary> resultsCache = new HashMap<String, AnnotationSummary>();

    private final ZOOMASearchClient client;

    //    public ZoomaRESTClient(int minInputLength, float cutoffPercentage, float cutoffScore, boolean  olsShortIds, String excludedPropertiesResource) {
    public static HashMap<String, AnnotationSummary> getResultsCache() {
        return resultsCache;
    }

    public ZoomaRESTClient(int minInputLength, float cutoffPercentage, float cutoffScore, boolean olsShortIds, boolean overwriteAnnotations, String excludedPropertiesResource) {
        this.minInputLength = minInputLength;
        this.cutoffPercentage = cutoffPercentage;
        this.cutoffScore = cutoffScore;
        this.olsShortIds = olsShortIds;
        this.overwriteAnnotations = overwriteAnnotations;

        this.excludedProperties = parseExcludedProperties(excludedPropertiesResource);
        try {
            this.client = new ZOOMASearchClient(URI.create("http://www.ebi.ac.uk/fgpt/zooma").toURL());
        } catch (MalformedURLException e) {
            throw new RuntimeException("Failed to create ZOOMASearchCLient", e);
        }
    }

    public ZoomaRESTClient(int minInputLength, float cutoffPercentage, float cutoffScore, boolean olsShortIds, boolean overwriteAnnotations, HashSet excludedProperties) {
        this.minInputLength = minInputLength;
        this.cutoffPercentage = cutoffPercentage;
        this.cutoffScore = cutoffScore;
        this.olsShortIds = olsShortIds;
        this.overwriteAnnotations = overwriteAnnotations;

        this.excludedProperties = excludedProperties;
        try {
            this.client = new ZOOMASearchClient(URI.create("http://www.ebi.ac.uk/fgpt/zooma").toURL());
        } catch (MalformedURLException e) {
            throw new RuntimeException("Failed to create ZOOMASearchCLient", e);
        }
    }


    protected Logger getLog() {
        return log;
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
    public ArrayList<String> concatenateCompoundURIs(AnnotationSummary zoomaAnnotationSummary, boolean olsShortIds) {

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

            String accession = parseAccession(uri, olsShortIds) + ",";
            String ref = accession.substring(0, accession.indexOf(":"));

            refAndAccession.add(ref);
            refAndAccession.add(accession);
        }

        // for the edge cases: compound URIs
        else if (semanticTags.size() > 1) {

            String compoundTermSourceRef = "";
            String compoundAccession = "";

            //todo: MAGETAB parser to handle this ultimately
            getLog().error("Compound URI detected; MAGETAB parser not yet able to handle this edge case.");

            // for each URI build up corresponding delimited strings for refs and accessions
            for (URI uri : semanticTags) {
                String accession = parseAccession(uri, olsShortIds);
                String ref = accession.substring(0, accession.indexOf(":"));
                compoundTermSourceRef += ref + "|";
                compoundAccession += accession + "|";
            }

            log.info("\tCompound term source ref: " + compoundTermSourceRef);
            log.info("\tCompound accession: " + compoundAccession);

            refAndAccession.add(compoundTermSourceRef);
            refAndAccession.add(compoundAccession);
        }

        return refAndAccession;
    }

//    public String parseTermSourceRef(URI semanticTag) {
//        String host = semanticTag.getHost();
//        int delimiterIndex = Math.max(host.lastIndexOf("/"), host.lastIndexOf("#")) + 1;
//        System.out.println(semanticTag + ":" + host + "," + delimiterIndex);
//        if (delimiterIndex == 0) return host;
//        else return host.substring(0, delimiterIndex - 1);
//    }

    public String parseAccession(URI semanticTag, boolean olsShortIds) {

        String tag = String.valueOf(semanticTag);

        if (olsShortIds) {
            int delimiterIndex = parseDelimIndex(semanticTag);
            tag = tag.substring(delimiterIndex).replace("_", ":");
        }

        return tag;
    }

    public int parseDelimIndex(URI semanticTag) {
        String uri = String.valueOf(semanticTag);
        return Math.max(uri.lastIndexOf("/"), uri.lastIndexOf("#")) + 1;
    }

    /**
     * Get best Zooma AnnotationSummary for specified attribute type and value
     *
     * @param attributeType (eg: organism)
     * @return cleanedAttributeType
     */
    public String cleanAndCheckType(String attributeType) {

        //to visually separate log output in console
        System.out.println();

        // Clean the attribute type of underscores, camelcasing, double spaces etc.
        String cleanedAttributeType = cleanAttributeType(attributeType);

        // Check that the cleaned type is not excluded
        if (excludedProperties.contains(cleanedAttributeType)) {
            getLog().info("Attribute type '" + cleanedAttributeType + "' was marked for exclusion and will be ignored.");
            return "exclude";
        }

        // concatenate input to simplify storage and retrieval from map of cached results.
        return cleanedAttributeType;

    }


    private AnnotationSummary getZoomaAnnotationSummary(String cleanedAttributeType, String originalAttributeValue) {

        if (originalAttributeValue.length() < minInputLength) {
            getLog().info("Input '" + originalAttributeValue + "' is below the length requirement.");
            return null;
        }

        String input = cleanedAttributeType + "|" + originalAttributeValue;

        // if there's a result in the cache, fetch it
        if (resultsCache.containsKey(input)) {
            if (resultsCache.get(input) != null) getLog().info("Fetching result from cache for \"" + input + "\"");
            return resultsCache.get(input);
        }

        // if there's no result in the cache, initiate a new Zooma query
        getLog().info("Initiating new zooma query for " + input);

        Property property = new SimpleTypedProperty(cleanedAttributeType, originalAttributeValue);
        Map<AnnotationSummary, Float> resultMap = client.searchZOOMA(property, 0);

        // filter results based on cutoffpercentage specified by the user
        if (resultMap.size() != 0) {
            getLog().info("Filtering " + resultMap.size() + " Zooma result(s)");
            Set<AnnotationSummary> filteredResultSet = ZoomaUtils.filterAnnotationSummaries(resultMap, cutoffScore, cutoffPercentage);

            // if more than one result for the given percentage and score params, don't automate the annotation
            if (filteredResultSet.size() > 1) {
                getLog().warn("More than one filtered result meets user criteria; no automatic curation applied.");
                // For performance considerations, still put null in the cache for this input
                resultsCache.put(input, null);
                return null;
            }

            // otherwise if there is only one result, cache it and then return it.
            else {
                AnnotationSummary singleZoomaResult = filteredResultSet.iterator().next();
                getLog().info("Zooma annotation being applied...");

                resultsCache.put(input, singleZoomaResult);
                return singleZoomaResult;
            }

            // from among filtered results, get the best one and return it
            // todo: this would be a prompt-the-user feature, not an automated curation feature
//                return getBestMatch(input, filteredResultSet);
        }
        return null;
    }

    private String cleanAttributeType(String attributeType) {

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

        // further clean this value before comparing it to excluded types
        return cleanedAttributeType.trim().toLowerCase().replaceAll("//s+", " ");

    }

    /**
     * Iterates through the set of Zooma AnnotationSummaries (if there are any) and returns the one with the
     * highest quality score
     *
     * @param input     concatenated type and value of an attribute (just used for logging purposes) todo: maybe delete this param
     * @param resultSet of Zooma AnnotationSummaries
     * @return Zooma AnnotationSummary with the highest quality score
     * @throws IOException
     */
    private AnnotationSummary getBestMatch(String input, Set<AnnotationSummary> resultSet) throws IOException {

        // for good measure, check if result set is empty, but this should be checked before method is called.
        if (resultSet.isEmpty()) return null;

        // else if there's at least one result, initialize best result
        AnnotationSummary bestResult = resultSet.iterator().next();

        // if there's more than one result, iterate to find best
        if (resultSet.size() > 1) {

            // if there's more than one result, iterate through to update best result
            getLog().info("Iterating over " + resultSet.size() + " filtered Zooma results to find best match.");

            for (AnnotationSummary aresult : resultSet) {
                if (aresult.getQualityScore() > bestResult.getQualityScore()) bestResult = aresult;
            }
        }

        // log and cache the result
        if (bestResult.getSemanticTags().size() > 1) getLog().warn("Compound URI detected.");
        getLog().info("Input: " + input);
        getLog().info("Best ZoomaResult:" + bestResult.getID() + "\t" + bestResult.getAnnotatedPropertyType() + "\t" + bestResult.getAnnotatedPropertyValue() + "\t" + bestResult.getSemanticTags());
        getLog().info("ZoomaResult score: " + bestResult.getQualityScore());

        // then return it
        return bestResult;
    }

    /**
     * Delegates acquisition of a ZoomaAnnotationSummary based on type and value of the attribute passed in.
     * Using the resulting best summary, update the attribute accordingly.
     *
     * @param attribute original (unzoomified) TransitionalAttribute
     * @return zoomified attribute.
     */
    public TransitionalAttribute zoomifyAttribute(TransitionalAttribute attribute) {

        getLog().debug("Zoomifying transitional attribute");

        // clean and check the attribute type
        String cleanedType = cleanAndCheckType(attribute.getType());

        // if the type is not in the exclude list,
        if (!cleanedType.equals("exclude")) {

            // if overwrite annotations, first check to see if there are any
            if (overwriteAnnotations) {

                // if an annotation already exists
                if (attribute.getOriginalTermAccessionNumber() != null && !attribute.getOriginalTermAccessionNumber().equals("")) {
//                    getLog().warn("Existing annotation for '" + cleanedType + "|" + attribute.getOriginalValue() +"' was '"
//                            +attribute.getOriginalTermSourceREF()+"|"+attribute.getOriginalTermAccessionNumber()+ "'. It will be removed or overwritten.");
                    //set the annotation to null
                    attribute.setOriginalTermSourceREF(null);
                    attribute.setOriginalTermAccessionNumber(null);
                }
            }

            // then get the corresponding zooma annotations summary, if any
            AnnotationSummary zoomaAnnotationSummary = getZoomaAnnotationSummary(cleanedType, attribute.getOriginalValue());

            // if there are zooma results, store them in the transitional attribute
            if (zoomaAnnotationSummary != null) {

                attribute.setType(zoomaAnnotationSummary.getAnnotatedPropertyType());

                // note that this doesn't overwrite the ORIGINAL property value, just stores the zoomified one
                attribute.setZoomifiedValue(zoomaAnnotationSummary.getAnnotatedPropertyValue());   //Todo: Tony, these seem to be fetching inputs rather than annotated values

                ArrayList<String> refAndAccession = concatenateCompoundURIs(zoomaAnnotationSummary, olsShortIds);

                // note that this doesn't overwrite the ORIGINAL Term Source Ref, but just stores the Zooma one
                attribute.setZoomifiedTermSourceREF(refAndAccession.get(0));

                // note that this doesn't overwrite the ORIGINAL accession, but just stores the Zooma one
                attribute.setZoomifiedTermAccessionNumber(refAndAccession.get(1));
            }
        }

        // return modified attribute
        return attribute;
    }

    private TransitionalAttribute stripExistingAnnotations(TransitionalAttribute attribute) {

        if (attribute.getOriginalTermSourceREF() != null && !attribute.getOriginalTermSourceREF().equals("")) {
            getLog().warn("For '" + attribute.getOriginalValue() + "', existing annotation (" + attribute.getOriginalTermSourceREF() + ") is being stripped.");
            attribute.setZoomifiedTermSourceREF(null);
            attribute.setZoomifiedTermAccessionNumber(null);
        }

        return attribute;
    }

    private HashSet parseExcludedProperties(String excludedPropertiesResource) {
        HashSet excludedTypes = new HashSet<>();

        // read sources from file
        try {
            InputStream in = getClass().getClassLoader().getResourceAsStream(excludedPropertiesResource);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("#") && !line.isEmpty()) {
                    String propertyType = line.toLowerCase().replaceAll("_", " ");

                    if (propertyType != null) {
                        excludedTypes.add(propertyType);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            getLog().error("Failed to load properties: could not locate file '" + excludedPropertiesResource + "'.  " +
                    "No properties will be excluded");
        } catch (IOException e) {
            getLog().error("Failed to load properties: could not read file '" + excludedPropertiesResource + "'.  " +
                    "No properties will be excluded");
        }

        return excludedTypes;
    }


}
