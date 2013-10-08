package uk.ac.ebi.fgpt.zooma.search;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.zooma.model.Annotation;
import uk.ac.ebi.fgpt.zooma.model.AnnotationProvenance;
import uk.ac.ebi.fgpt.zooma.model.AnnotationSource;
import uk.ac.ebi.fgpt.zooma.model.AnnotationSummary;
import uk.ac.ebi.fgpt.zooma.model.BiologicalEntity;
import uk.ac.ebi.fgpt.zooma.model.Property;
import uk.ac.ebi.fgpt.zooma.model.SimpleAnnotation;
import uk.ac.ebi.fgpt.zooma.model.SimpleAnnotationProvenance;
import uk.ac.ebi.fgpt.zooma.model.SimpleAnnotationSource;
import uk.ac.ebi.fgpt.zooma.model.SimpleAnnotationSummary;
import uk.ac.ebi.fgpt.zooma.model.SimpleBiologicalEntity;
import uk.ac.ebi.fgpt.zooma.model.SimpleStudy;
import uk.ac.ebi.fgpt.zooma.model.SimpleTypedProperty;
import uk.ac.ebi.fgpt.zooma.model.Study;
import uk.ac.ebi.fgpt.zooma.model.TypedProperty;
import uk.ac.ebi.fgpt.zooma.util.URIUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A simple search client stub that takes a list of properties and uses them to search a ZOOMA service.
 *
 * @author Tony Burdett
 * @author Adam Faulconbridge
 * @date 03/09/12
 */
public class ZOOMASearchClient {
    private final String zoomaBase;

    private final String zoomaSearchBase;
    private final String zoomaSearchBaseOLD;
    private final String zoomaAnnotationsBase;
    private final String zoomaServicesBase;

    private Map<String, String> prefixMappings;

    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public ZOOMASearchClient(URL zoomaLocation) {
        this.zoomaBase = zoomaLocation.toString() + "/v2/api/";
        this.zoomaSearchBase = zoomaBase + "search?query=";
        this.zoomaSearchBaseOLD = zoomaBase + "searchOLD?query=";

        this.zoomaAnnotationsBase = zoomaBase + "annotations/";
        this.zoomaServicesBase = zoomaBase + "services/";
        loadPrefixMappings();
    }

    public Map<String, String> getPrefixMappings() throws IOException {
        URL prefixMappingsURL = new URL(zoomaServicesBase + "prefixMappings");
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> results = mapper.readValue(
                prefixMappingsURL,
                new TypeReference<Map<String, String>>() {
                });
        getLog().trace(results.toString());
        return results;
    }

    public Map<AnnotationSummary, Float> searchZOOMA(Property property, float score) {

        getLog().trace("searchZOOMA1");

        return searchZOOMA(property, score, false);
    }

    public Map<AnnotationSummary, Float> searchZOOMA(Property property, float score, boolean excludeType) {
        getLog().trace("searchZOOMA2");
        return searchZOOMA(property, score, excludeType, false);
    }

    public Map<AnnotationSummary, Float> searchZOOMAOLD(Property property, float score) {
        getLog().trace("searchZOOMAOLD1");
        return searchZOOMAOLD(property, score, false);
    }

    public Map<AnnotationSummary, Float> searchZOOMAOLD(Property property, float score, boolean excludeType) {
        getLog().trace("searchZOOMAOLD2");
        return searchZOOMAOLD(property, score, excludeType, false);
    }

    /**
     * @param property      what you're looking for
     * @param score         returns only the summaries above this threshold
     * @param excludeType   generic, search, with no type specification
     * @param noEmptyResult if true and there is no summary scored above the threshold, tries to return something
     *                      anyway, i.e., summaries that are not so well scored. If this flag is false, the search
     *                      returns only summaries with score above the score parameter, possibly an empty Map.
     * @return a {@link LinkedHashMap} map, where the entries are ordered by decreasing score.
     */
    public Map<AnnotationSummary, Float> searchZOOMA(Property property,
                                                     float score,
                                                     boolean excludeType,
                                                     boolean noEmptyResult) {

        getLog().trace("searchZOOMA3");

        String query = property.getPropertyValue();

        // search for annotation summaries
        Map<AnnotationSummary, Float> summaries = new LinkedHashMap<>();
        try {
            String search = zoomaSearchBase + URLEncoder.encode(property.getPropertyValue(), "UTF-8");
            String typedSearch = search + "&type=";
            URL queryURL = property instanceof TypedProperty && !excludeType
                    ? new URL(typedSearch + URLEncoder.encode(((TypedProperty) property).getPropertyType(), "UTF-8"))
                    : new URL(search);
            getLog().trace("Sending query [" + queryURL + "]...");

            ObjectMapper mapper = new ObjectMapper();
            JsonNode resultsNode = mapper.readValue(queryURL, JsonNode.class);
            getLog().trace(resultsNode.toString());

            int goodResultCount = 0;
            JsonNode resultNode = resultsNode.get("result");
            if (resultNode != null) {
                for (JsonNode result : resultNode) {
                    // meets significance score?
                    float resultScore = Float.parseFloat(result.get("score").getTextValue());
                    AnnotationSummary as = mapAnnotationSummary(result, mapper);
                    getLog().trace(
                            "Annotation hit:\n\t\t" +
                                    "Searched: " + property + "\t" +
                                    "Found: " + as.getAnnotatedPropertyValue() + " " +
                                    "[" + as.getAnnotatedPropertyType() + "] -> " +
                                    as.getSemanticTags() + "\tScore: " + resultScore);

                    if (resultScore > score) {
                        goodResultCount++;
                        // this result scores highly enough to retain
                        // so map annotation summary to score and include in results
                        summaries.put(as, resultScore);
                    }
                    else {
                        // resultScore gone below the threshold

                        // low-score results starts appearing now, have we found something above it?
                        if (goodResultCount == 0) {
                            if (noEmptyResult) {
                                // No, but you want something anyway, here you are
                                summaries.put(as, resultScore);
                            }
                            else {
                                // No and you don't want bad stuff, so let's stop with empty result
                                break;
                            }
                        }
                        else {
                            // We found results above the threshold and now badly-scored summaries start to come in,
                            // we can stop and discard them
                            break;
                        }
                    }
                }

                if (goodResultCount == 0 && !summaries.isEmpty()) {
                    getLog().debug("No good search results for '" + property + "' - " +
                                           "some that fall below the required score have been retained " +
                                           "and will require curation");
                }
            }
        }
        catch (IOException e) {
            getLog().error("Failed to query ZOOMA for property '" + query + "' (" + e.getMessage() + ")");
            throw new RuntimeException("Failed to query ZOOMA for property '" + query + "' " +
                                               "(" + e.getMessage() + ")", e);
        }

        return summaries;
    }

    /**
     * @param property      what you're looking for
     * @param score         returns only the summaries above this threshold
     * @param excludeType   generic, search, with no type specification
     * @param noEmptyResult if true and there is no summary scored above the threshold, tries to return something
     *                      anyway, i.e., summaries that are not so well scored. If this flag is false, the search
     *                      returns only summaries with score above the score parameter, possibly an empty Map.
     * @return a {@link LinkedHashMap} map, where the entries are ordered by decreasing score.
     */
    public Map<AnnotationSummary, Float> searchZOOMAOLD(Property property,
                                                        float score,
                                                        boolean excludeType,
                                                        boolean noEmptyResult) {

        getLog().trace("searchZOOMAOLD3");

        String query = property.getPropertyValue();

        // search for annotation summaries
        Map<AnnotationSummary, Float> summaries = new LinkedHashMap<>();
        try {
            String search = zoomaSearchBaseOLD + URLEncoder.encode(property.getPropertyValue(), "UTF-8");
            String typedSearch = search + "&type=";
            URL queryURL = property instanceof TypedProperty && !excludeType
                    ? new URL(typedSearch + URLEncoder.encode(((TypedProperty) property).getPropertyType(), "UTF-8"))
                    : new URL(search);
            getLog().trace("Sending query [" + queryURL + "]...");

            ObjectMapper mapper = new ObjectMapper();
            JsonNode resultsNode = mapper.readValue(queryURL, JsonNode.class);
            getLog().trace(resultsNode.toString());

            int goodResultCount = 0;
            JsonNode resultNode = resultsNode.get("result");
            if (resultNode != null) {
                for (JsonNode result : resultNode) {
                    // meets significance score?
                    float resultScore = Float.parseFloat(result.get("score").getTextValue());
                    AnnotationSummary as = mapAnnotationSummary(result, mapper);
                    getLog().trace(
                            "Annotation hit:\n\t\t" +
                                    "Searched: " + property + "\t" +
                                    "Found: " + as.getAnnotatedPropertyValue() + " " +
                                    "[" + as.getAnnotatedPropertyType() + "] -> " +
                                    as.getSemanticTags() + "\tScore: " + resultScore);

                    if (resultScore > score) {
                        goodResultCount++;
                        // this result scores highly enough to retain
                        // so map annotation summary to score and include in results
                        summaries.put(as, resultScore);
                    }
                    else {
                        // resultScore gone below the threshold

                        // low-score results starts appearing now, have we found something above it?
                        if (goodResultCount == 0) {
                            if (noEmptyResult) {
                                // No, but you want something anyway, here you are
                                summaries.put(as, resultScore);
                            }
                            else {
                                // No and you don't want bad stuff, so let's stop with empty result
                                break;
                            }
                        }
                        else {
                            // We found results above the threshold and now badly-scored summaries start to come in,
                            // we can stop and discard them
                            break;
                        }
                    }
                }

                if (goodResultCount == 0 && !summaries.isEmpty()) {
                    getLog().debug("No good search results for '" + property + "' - " +
                                           "some that fall below the required score have been retained " +
                                           "and will require curation");
                }
            }
        }
        catch (IOException e) {
            getLog().error("Failed to query ZOOMA for property '" + query + "' (" + e.getMessage() + ")");
            throw new RuntimeException("Failed to query ZOOMA for property '" + query + "' " +
                                               "(" + e.getMessage() + ")", e);
        }

        return summaries;
    }

    public Annotation getAnnotation(URI annotationURI) {
        try {
            URL fetchURL = new URL(zoomaAnnotationsBase + annotationURI.toString());

            // populate required fields from result of query
            URI uri = lookupURI(annotationURI.toString());
            Collection<BiologicalEntity> biologicalEntities = new ArrayList<>();
            Property annotatedProperty = null;
            AnnotationProvenance annotationProvenance = null;
            List<URI> semanticTags = new ArrayList<>();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode annotationNode = null;
            int tries = 0;
            boolean success = false;
            IOException lastException = null;
            while (!success & tries < 3) {
                try {
                    annotationNode = mapper.readValue(fetchURL, JsonNode.class);
                    success = true;
                }
                catch (IOException e) {
                    // could be due to an intermittent HTTP 500 exception, allow a couple of retries
                    tries++;
                    getLog().error(e.getMessage() + ": retrying.  Retries remaining = " + (3 - tries));
                    lastException = e;
                }
            }
            if (!success) {
                throw lastException;
            }

            getLog().trace("Got the following result from <" + fetchURL + ">...\n" + annotationNode.toString());

            JsonNode propertyNode = annotationNode.get("annotatedProperty");
            if (propertyNode != null) {
                annotatedProperty = new SimpleTypedProperty(propertyNode.get("propertyType").getTextValue(),
                                                            propertyNode.get("propertyValue").getTextValue());
            }

            JsonNode biologicalEntitiesNode = annotationNode.get("annotatedBiologicalEntities");
            if (biologicalEntitiesNode != null) {
                for (JsonNode biologicalEntityNode : biologicalEntitiesNode) {
                    List<Study> studies = new ArrayList<>();
                    JsonNode studiesNode = biologicalEntityNode.get("studies");
                    for (JsonNode studyNode : studiesNode) {
                        Study study = new SimpleStudy(URI.create(studyNode.get("uri").getTextValue()),
                                                      studyNode.get("accession").getTextValue());
                        studies.add(study);
                    }

                    BiologicalEntity be = new SimpleBiologicalEntity(
                            URI.create(biologicalEntityNode.get("uri").getTextValue()),
                            biologicalEntityNode.get("name").getTextValue(),
                            studies.toArray(new Study[studies.size()]));
                    biologicalEntities.add(be);
                }
            }

            JsonNode provenanceNode = annotationNode.get("provenance");
            if (provenanceNode != null) {
                JsonNode sourceNode = provenanceNode.get("source");
                if (sourceNode != null) {
                    AnnotationSource.Type type = AnnotationSource.Type.valueOf(sourceNode.get("type").getTextValue());
                    AnnotationSource annotationSource =
                            new SimpleAnnotationSource(URI.create(sourceNode.get("uri").getTextValue()), type);
                    annotationProvenance = new SimpleAnnotationProvenance(
                            annotationSource,
                            AnnotationProvenance.Evidence.valueOf(provenanceNode.get("evidence").getTextValue()),
                            provenanceNode.get("generator").getTextValue(),
                            new Date(provenanceNode.get("generatedDate").getLongValue()));
                }
            }

            JsonNode stsNode = annotationNode.get("semanticTags");
            for (JsonNode stNode : stsNode) {
                URI de = lookupURI(stNode.getTextValue());
                semanticTags.add(de);
            }

            // create and return the annotation
            return new SimpleAnnotation(uri,
                                        biologicalEntities,
                                        annotatedProperty,
                                        annotationProvenance,
                                        semanticTags.toArray(new URI[semanticTags.size()]));
        }
        catch (IOException e) {
            getLog().error("Failed to query ZOOMA for annotation '" + annotationURI.toString() + "' " +
                                   "(" + e.getMessage() + ")");
            throw new RuntimeException("Failed to query ZOOMA for annotation '" + annotationURI.toString() + "' " +
                                               "(" + e.getMessage() + ")", e);
        }
    }

    public String getLabel(URI uri) throws IOException {
        String shortform = URIUtils.getShortform(prefixMappings, uri);
        getLog().trace("Formulating search for label of '" + shortform + "' (derived from <" + uri + ">)");
        URL labelsURL = new URL(zoomaServicesBase + "labels/" + shortform);
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Set<String>> labelMap = mapper.readValue(labelsURL, new TypeReference<Map<String, Set<String>>>() {
        });
        return labelMap.get("label").iterator().next();
    }

    private URI lookupURI(String shortname) {
        // try to recover URI
        URI uri;
        try {
            uri = URIUtils.getURI(prefixMappings, shortname);
        }
        catch (IllegalArgumentException e) {
            // if we get an illegal argument exception, refresh cache and retry
            getLog().debug(e.getMessage() + ": reloading prefix mappings cache and retrying...");
            loadPrefixMappings();
            uri = URIUtils.getURI(prefixMappings, shortname);
        }
        return uri;
    }

    private void loadPrefixMappings() {
        Map<String, String> mappings;
        try {
            mappings = getPrefixMappings();
        }
        catch (IOException e) {
            getLog().error("Unable to retrieve prefix mappings, using defaults");
            mappings = new HashMap<>();
        }
        this.prefixMappings = Collections.unmodifiableMap(mappings);
    }

    private AnnotationSummary mapAnnotationSummary(JsonNode result, ObjectMapper mapper) throws IOException {
        // acquire the annotation summary for this result
        String mid = result.get("mid").getTextValue();

        if (result != null) {

            if (mid != null) {

                float resultScore = Float.parseFloat(result.get("score").getTextValue());

                URL summaryURL = new URL(zoomaBase + "summaries/" + mid);
                JsonNode summaryNode = mapper.readValue(summaryURL, JsonNode.class);

                String propertyType = summaryNode.get("annotatedPropertyType").getTextValue();
                String propertyValue = summaryNode.get("annotatedPropertyValue").getTextValue();

                List<URI> semanticTags = new ArrayList<>();
                JsonNode stsNode = summaryNode.get("semanticTags");
                for (JsonNode stNode : stsNode) {
                    semanticTags.add(lookupURI(stNode.getTextValue()));
                }

                List<URI> annotationURIs = new ArrayList<>();
                JsonNode annsNode = summaryNode.get("annotationURIs");
                for (JsonNode annNode : annsNode) {
                    annotationURIs.add(URI.create(annNode.getTextValue()));
                }


                // collect summary into map with it's score
                return new SimpleAnnotationSummary(mid,
                                                   propertyType,
                                                   propertyValue,
                                                   semanticTags,
                                                   annotationURIs,
                                                   resultScore);

            }
            else {
                
               /* AnnotationSummary of post-coordinate terms have specific issues:
                * mid=null 
                * 2 semantic tags.
                * Following lines manage it.
                */

                float resultScore = Float.parseFloat(result.get("score").getTextValue());

                JsonNode node = result.get("notable");

                if (node != null) {

                    String node2 = node.get("name").getTextValue();

                    node2 = node2.replaceAll("null; ", "");
                    String[] uris = node2.split(", ");


                    List<URI> semanticTags = new ArrayList<>();

                    for (int i = 0; i < uris.length; i++) {

                        try {
                            semanticTags.add(new URI(URLEncoder.encode(uris[i], "UTF-8")));

                        }
                        catch (Exception e) {
                            e.printStackTrace();

                        }


                    }

                    return new SimpleAnnotationSummary(mid,
                                                       "",
                                                       "",
                                                       semanticTags,
                                                       null,
                                                       resultScore);
                }
            }
        }
        return null;
    }
}
