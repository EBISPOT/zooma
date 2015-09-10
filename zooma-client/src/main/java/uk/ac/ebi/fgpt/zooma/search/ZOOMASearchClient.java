package uk.ac.ebi.fgpt.zooma.search;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.zooma.exception.SearchException;
import uk.ac.ebi.fgpt.zooma.model.Annotation;
import uk.ac.ebi.fgpt.zooma.model.AnnotationPrediction;
import uk.ac.ebi.fgpt.zooma.model.AnnotationProvenance;
import uk.ac.ebi.fgpt.zooma.model.AnnotationSource;
import uk.ac.ebi.fgpt.zooma.model.AnnotationSummary;
import uk.ac.ebi.fgpt.zooma.model.BiologicalEntity;
import uk.ac.ebi.fgpt.zooma.model.Property;
import uk.ac.ebi.fgpt.zooma.model.SimpleAnnotation;
import uk.ac.ebi.fgpt.zooma.model.SimpleAnnotationPrediction;
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
import java.util.Iterator;
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

    private final String zoomaAnnotationsBase;

    private final String zoomaServicesBase;
    private final String zoomaAnnotateServiceBase;

    private final String zoomaPropertyValueArgument;
    private final String zoomaPropertyTypeArgument;
    private final String zoomaFilterArgument;
    private final String zoomaArgumentSeparator;

    private final String zoomaRequiredParam;
    private final String zoomaPreferredParam;
    private final String zoomaFilterParamStart;
    private final String zoomaFilterParamEnd;
    private final String zoomaFilterParamSeparator;

    private Map<String, String> prefixMappings;

    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public ZOOMASearchClient(URL zoomaLocation) {
        this.zoomaBase = zoomaLocation.toString() + "/v2/api/";

        this.zoomaAnnotationsBase = zoomaBase + "annotations/";

        this.zoomaServicesBase = zoomaBase + "services/";
        this.zoomaAnnotateServiceBase = zoomaServicesBase + "annotate?";

        this.zoomaPropertyValueArgument = "propertyValue=";
        this.zoomaPropertyTypeArgument = "propertyType=";
        this.zoomaFilterArgument = "filter=";
        this.zoomaArgumentSeparator = "&";

        this.zoomaRequiredParam = "required:";
        this.zoomaPreferredParam = "preferred:";
        this.zoomaFilterParamStart = "[";
        this.zoomaFilterParamEnd = "]";
        this.zoomaFilterParamSeparator = ",";

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

    public List<AnnotationPrediction> annotate(Property property) {
        return annotate(property, Collections.<String>emptyList());
    }

    public List<AnnotationPrediction> annotate(Property property, List<String> requiredSources) {
        return annotate(property, requiredSources, Collections.<String>emptyList());
    }

    /**
     * @param property         what you're looking for
     * @param requiredSources  the list of sources which are required in making an annotation prediction
     * @param preferredSources the list of sources, in order of preference, to predict an annotation from
     * @return a {@link LinkedHashMap} map, where the entries are ordered by decreasing score.
     */
    public List<AnnotationPrediction> annotate(Property property,
                                               List<String> requiredSources,
                                               List<String> preferredSources) {
        String query = property.getPropertyValue();

        // get annotation predictions
        try {
            // construct the URL from supplied args
            String searchUrl = zoomaAnnotationsBase + zoomaPropertyValueArgument +
                    URLEncoder.encode(property.getPropertyValue(), "UTF-8");
            searchUrl = property instanceof TypedProperty
                    ? searchUrl + zoomaArgumentSeparator + zoomaPropertyTypeArgument +
                    URLEncoder.encode(((TypedProperty) property).getPropertyType(), "UTF-8")
                    : searchUrl;
            if (!requiredSources.isEmpty() || !preferredSources.isEmpty()) {
                StringBuilder filters = new StringBuilder();
                filters.append(zoomaArgumentSeparator).append(zoomaFilterArgument);
                if (!requiredSources.isEmpty()) {
                    filters.append(zoomaRequiredParam).append(zoomaFilterParamStart);
                    Iterator<String> requiredIt = requiredSources.iterator();
                    while (requiredIt.hasNext()) {
                        filters.append(requiredIt.next());
                        if (requiredIt.hasNext()) {
                            filters.append(zoomaFilterParamSeparator);
                        }
                    }
                    filters.append(zoomaFilterParamEnd);
                }
                if (!preferredSources.isEmpty()) {
                    filters.append(zoomaPreferredParam).append(zoomaFilterParamStart);
                    Iterator<String> preferredIt = preferredSources.iterator();
                    while (preferredIt.hasNext()) {
                        filters.append(preferredIt.next());
                        if (preferredIt.hasNext()) {
                            filters.append(zoomaFilterParamSeparator);
                        }
                    }
                    filters.append(zoomaFilterParamEnd);
                }
                searchUrl = searchUrl.concat(filters.toString());
            }
            URL queryURL = new URL(searchUrl);
            getLog().trace("Sending query [" + queryURL + "]...");

            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(queryURL, new TypeReference<List<SimpleAnnotationPrediction>>() {});

        }
        catch (IOException e) {
            getLog().error("Failed to query ZOOMA for property '" + query + "' (" + e.getMessage() + ")");
            throw new RuntimeException("Failed to query ZOOMA for property '" + query + "' " +
                                               "(" + e.getMessage() + ")", e);
        }
    }

    public Annotation getAnnotation(URI annotationURI) throws SearchException {
        try {
            String shortname = lookupShortname(annotationURI);
            URL fetchURL = new URL(zoomaAnnotationsBase + shortname);

            // populate required fields from result of query
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
                annotatedProperty = new SimpleTypedProperty(propertyNode.get("propertyType").asText(),
                                                            propertyNode.get("propertyValue").asText());
            }

            JsonNode biologicalEntitiesNode = annotationNode.get("annotatedBiologicalEntities");
            if (biologicalEntitiesNode != null) {
                for (JsonNode biologicalEntityNode : biologicalEntitiesNode) {
                    List<Study> studies = new ArrayList<>();
                    JsonNode studiesNode = biologicalEntityNode.get("studies");
                    for (JsonNode studyNode : studiesNode) {
                        Study study = new SimpleStudy(URI.create(studyNode.get("uri").asText()),
                                                      studyNode.get("accession").asText());
                        studies.add(study);
                    }

                    BiologicalEntity be = new SimpleBiologicalEntity(
                            URI.create(biologicalEntityNode.get("uri").asText()),
                            biologicalEntityNode.get("name").asText(),
                            studies.toArray(new Study[studies.size()]));
                    biologicalEntities.add(be);
                }
            }

            JsonNode provenanceNode = annotationNode.get("provenance");
            if (provenanceNode != null) {
                JsonNode sourceNode = provenanceNode.get("source");
                if (sourceNode != null) {
                    AnnotationSource.Type type = AnnotationSource.Type.valueOf(sourceNode.get("type").asText());
                    AnnotationSource annotationSource =
                            new SimpleAnnotationSource(URI.create(sourceNode.get("uri").asText()),
                                                       sourceNode.get("name").asText(),
                                                       type);
                    annotationProvenance = new SimpleAnnotationProvenance(
                            annotationSource,
                            AnnotationProvenance.Evidence.valueOf(provenanceNode.get("evidence").asText()),
                            provenanceNode.get("generator").asText(),
                            new Date(provenanceNode.get("generatedDate").asLong()));
                }
            }

            JsonNode stsNode = annotationNode.get("semanticTags");
            for (JsonNode stNode : stsNode) {
                URI de = URI.create(stNode.asText());
                semanticTags.add(de);
            }

            // create and return the annotation
            return new SimpleAnnotation(annotationURI,
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

    public String getLabel(URI uri) throws IOException, SearchException {
        if (uri == null) {
            throw new IllegalArgumentException("Cannot lookup label for URI 'null'");
        }

        String shortform = null;
        try {
            shortform = URIUtils.getShortform(prefixMappings, uri);
        }
        catch (IllegalArgumentException e) {
            throw new SearchException("Failed to lookup label for <" + uri.toString() + ">", e);
        }
        if (shortform != null) {
            getLog().trace("Formulating search for label of '" + shortform + "' (derived from <" + uri + ">)");
            URL labelsURL = new URL(zoomaServicesBase + "labels/" + shortform);
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Set<String>> labelMap =
                    mapper.readValue(labelsURL, new TypeReference<Map<String, Set<String>>>() {
                    });
            return labelMap.get("label").iterator().next();
        }
        else {
            String msg = "URI <" + uri + "> resolved to 'null' shortform";
            getLog().error(msg);
            throw new RuntimeException(msg);
        }
    }

    public Collection<String> getSynonyms(URI uri) throws IOException {
        String shortform = URIUtils.getShortform(prefixMappings, uri);
        getLog().trace("Formulating search for synonyms of '" + shortform + "' (derived from <" + uri + ">)");
        URL labelsURL = new URL(zoomaServicesBase + "labels/" + shortform);
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Set<String>> labelMap = mapper.readValue(labelsURL, new TypeReference<Map<String, Set<String>>>() {
        });
        return labelMap.get("synonyms");
    }

    private String lookupShortname(URI uri) {
        // try to recover URI
        String shortname;
        try {
            shortname = URIUtils.getShortform(prefixMappings, uri);
        }
        catch (IllegalArgumentException e) {
            // if we get an illegal argument exception, refresh cache and retry
            getLog().debug(e.getMessage() + ": reloading prefix mappings cache and retrying...");
            loadPrefixMappings();
            shortname = URIUtils.getShortform(prefixMappings, uri);
        }
        return shortname;
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
        String mid = result.get("mid").asText();
        float resultScore = Float.parseFloat(result.get("score").asText());

        URL summaryURL = new URL(zoomaBase + "summaries/" + mid);
        try {
            JsonNode summaryNode = mapper.readValue(summaryURL, JsonNode.class);

            URI propertyUri =
                    summaryNode.get("annotatedPropertyUri") != null && !summaryNode.get("annotatedPropertyUri").isNull()
                            ? URI.create(summaryNode.get("annotatedPropertyUri").asText())
                            : null;
            String propertyType = summaryNode.get("annotatedPropertyType").asText();
            String propertyValue = summaryNode.get("annotatedPropertyValue").asText();

            List<URI> semanticTags = new ArrayList<>();
            JsonNode stsNode = summaryNode.get("semanticTags");
            for (JsonNode stNode : stsNode) {
                semanticTags.add(URI.create(stNode.asText()));
            }

            List<URI> annotationURIs = new ArrayList<>();
            JsonNode annsNode = summaryNode.get("annotationURIs");
            for (JsonNode annNode : annsNode) {
                annotationURIs.add(URI.create(annNode.asText()));
            }

            List<URI> annotationSourceURIs = new ArrayList<>();
            JsonNode annsSourceNode = summaryNode.get("annotationSourceURIs");
            for (JsonNode annSourceNode : annsSourceNode) {
                annotationSourceURIs.add(URI.create(annSourceNode.asText()));
            }

            // collect summary into map with it's score
            return new SimpleAnnotationSummary(mid,
                                               propertyUri,
                                               propertyType,
                                               propertyValue,
                                               semanticTags,
                                               annotationURIs,
                                               resultScore,
                                               annotationSourceURIs);
        }
        catch (IOException e) {
            getLog().error("Failed to read AnnotationSummary object at '" + summaryURL + "'", e);
            throw e;
        }
    }
}
