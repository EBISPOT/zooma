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

    private final HashSet inelegibleProperties;
    private Logger log = LoggerFactory.getLogger(getClass());

    private int minInputLength;       // minimum length of a string to match for annotations
    private float cutoffPercentage;   // cutoff percentage (eg. .8 keeps only the top 20%)
    private float cutoffScore;        // minimum score to accept as a zooma hit
    private boolean olsShortIds;     // yes if output should include term ids in their (OLS-compatible) short form rather than full URI
    private static HashMap<String, AnnotationSummary> resultsCache = new HashMap<String, AnnotationSummary>();

    private final ZOOMASearchClient client;

    public ZoomaRESTClient(int minInputLength, float cutoffPercentage, float cutoffScore, boolean  olsShortIds, String excludedPropertiesResource) {
        this.minInputLength = minInputLength;
        this.cutoffPercentage = cutoffPercentage;
        this.cutoffScore = cutoffScore;
        this.olsShortIds = olsShortIds;

        this.inelegibleProperties = parseIneligibleProperties(excludedPropertiesResource);

        try {
            this.client = new ZOOMASearchClient(URI.create("http://www.ebi.ac.uk/fgpt/zooma").toURL());
        }
        catch (MalformedURLException e) {
            throw new RuntimeException("Failed to create ZOOMASearchCLient", e);
        }
    }

    public ZoomaRESTClient(int minInputLength, float cutoffPercentage, float cutoffScore, boolean  olsShortIds, HashSet excludedProperties) {
        this.minInputLength = minInputLength;
        this.cutoffPercentage = cutoffPercentage;
        this.cutoffScore = cutoffScore;
        this.olsShortIds = olsShortIds;
//
        inelegibleProperties = excludedProperties;

        try {
            this.client = new ZOOMASearchClient(URI.create("http://www.ebi.ac.uk/fgpt/zooma").toURL());
        }
        catch (MalformedURLException e) {
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
    private ArrayList<String> concatenateCompoundURIs(AnnotationSummary zoomaAnnotationSummary) {

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
            String uri = String.valueOf(iterator.next());
            int delimiterIndex = Math.max(uri.lastIndexOf("/"), uri.lastIndexOf("#")) + 1;

            String namespace =  uri.substring(0, delimiterIndex - 1);   //todo: check this!!!
            refAndAccession.add(namespace);

            String olsShortId = uri.substring(delimiterIndex).replace("_",":");
            refAndAccession.add(olsShortId);

            return refAndAccession;
        }

        // for the edge cases: compound URIs
        else if (semanticTags.size() > 1) {

            String compoundTermSourceRef = "";
            String compoundAccession = "";

            //todo: MAGETAB parser to handle this ultimately
            getLog().warn("Compound URI detected; MAGETAB parser not yet able to handle this edge case.");

            // for each URI (except the last one) build up corresponding delimited strings for refs and accessions
            for (int i = 0; i < semanticTags.size() - 1; i++) {
                URI uri = (URI) iterator.next();
                compoundTermSourceRef += uri.getHost() + ",";
                compoundAccession += uri.getFragment() + ",";
                //todo: maybe parameterize the delimiter
            }

            // for the very last URI, append to the delimited string without a trailing delimiter
            URI lastURI = (URI) iterator.next();
            compoundTermSourceRef += lastURI.getHost();
            compoundAccession += lastURI.getFragment();

            refAndAccession.add(compoundTermSourceRef);
            refAndAccession.add(compoundAccession);
        }

        return refAndAccession;
    }

    //eg: http://wwwdev.ebi.ac.uk/fgpt/zooma/v2/api/search?query=colon&type=OrganismPart

    /**
     * Get best Zooma AnnotationSummary for specified attribute type and value
     *
     * @param attributeType  (eg: organism)
     * @param attributeValue (eg: human)
     * @return corresponding best-scoring Zooma annotation
     */
    public AnnotationSummary getZoomaAnnotationSummary(String attributeType, String attributeValue) {

        //to visually separate log output in console
        System.out.println();

        // Don't proceed with zoomification unless the input meets the specified length requirement
        if (attributeValue.length() < minInputLength) {
            return null;
        }

        // concatenate input to simplify storage and retrieval from map of cached results.
        String input = attributeType + "|" + attributeValue;

        // if there's a result in the cache, fetch it
        if (resultsCache.containsKey(input)) {
            getLog().info("Fetching result from cache for \"" + input + "\"");
            return resultsCache.get(input);
        }

        // if there's no result in the cache, initiate a new Zooma query
        else try {
            Property property = new SimpleTypedProperty(attributeType, attributeValue);
            Map<AnnotationSummary, Float> resultMap = client.searchZOOMA(property, 0);

            // filter results based on cutoffpercentage specified by the user
            if (resultMap.size() != 0) {
                getLog().info("Filtering " + resultMap.size() + " Zooma result(s)");
                Set<AnnotationSummary> filteredResultSet = ZoomaUtils.filterAnnotationSummaries(resultMap,cutoffScore, cutoffPercentage );
                // from among filtered results, get the best one and return it
                return getBestMatch(input, filteredResultSet);
            }

        }
        catch (IOException e) {
            e.printStackTrace();  //todo
        }

        // if there are no results from the query, still store this info in the resultsCache for expediency
        resultsCache.put(input, null);
        return null;
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
        if (bestResult.getSemanticTags().size() > 1) log.warn("Compound URI detected.");
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
    public TransitionalAttribute zoomify(TransitionalAttribute attribute) {
        // is attribute type in exclusion list?
        String normalizedType = attribute.getType().toLowerCase().trim().replaceAll("\\s", "").replaceAll("_", "");
        if (!inelegibleProperties.contains(normalizedType)) {
            // fetch the best matching Zooma Annotation summary
            AnnotationSummary zoomaAnnotationSummary = getZoomaAnnotationSummary(attribute.getType(), attribute.getValue());

            // if there are no zooma results, warn and return
            if (zoomaAnnotationSummary == null) {
                if (attribute.getValue().length() < minInputLength)
                    log.info("Input value \"" + attribute.getValue() + "\" is below specified length of " + minInputLength + " characters.");
                else log.warn("No Zooma Annotation found for " + attribute.getType() + ":" + attribute.getValue());
            }

            // if there *is* a zooma result, update the attribute accordingly
            else {
                attribute.setType(zoomaAnnotationSummary.getAnnotatedPropertyType());
                attribute.setValue(zoomaAnnotationSummary.getAnnotatedPropertyValue());

                ArrayList<String> refAndAcession = concatenateCompoundURIs(zoomaAnnotationSummary);
                attribute.setTermSourceREF(refAndAcession.get(0));
                attribute.setTermAccessionNumber(refAndAcession.get(1));
            }

            // return updated (zoomified) attribute
            return attribute;  //todo: Tony...bad form to return same object that was passed in?
        }
        else {
            getLog().info("Property type '" + normalizedType + "' found in the exclusions list");
            return attribute;
        }
    }

    private HashSet parseIneligibleProperties(String excludedPropertiesResource) {
        HashSet<String> excludedTypes = new HashSet<>();

        // read sources from file
        try {
            InputStream in = getClass().getClassLoader().getResourceAsStream(excludedPropertiesResource);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("#") && !line.isEmpty()) {
                    String s = line.toLowerCase();
                    if (s != null) {
                        excludedTypes.add(s);
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
