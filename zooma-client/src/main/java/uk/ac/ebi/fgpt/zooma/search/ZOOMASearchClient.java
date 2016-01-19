package uk.ac.ebi.fgpt.zooma.search;

import static org.apache.commons.lang3.StringUtils.abbreviate;

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
import java.util.Comparator;
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
public class ZOOMASearchClient extends AbstractZOOMASearch {
    private final String zoomaBase;

    private final String zoomaAnnotationsBase;

    private final String zoomaServicesBase;

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

    public ZOOMASearchClient() {
    	this ( (String) null );
    }

    public ZOOMASearchClient(URL zoomaLocation) {
    	this ( zoomaLocation == null ? null : zoomaLocation.toString () );
    }
    
    public ZOOMASearchClient(String zoomaLocation) 
    {
	    	if ( zoomaLocation == null ) 
		  		zoomaLocation = System.getProperty ( 
		  			"uk.ac.ebi.fg.biosd.biosd2rdf.zooma.apiurl", 
		  			"http://www.ebi.ac.uk/fgpt/zooma" 
	  		);
    	
        this.zoomaBase = zoomaLocation + "/v2/api/";

        this.zoomaAnnotationsBase = zoomaBase + "annotations/";

        this.zoomaServicesBase = zoomaBase + "services/";
//TODO: remove        this.zoomaAnnotateServiceBase = zoomaServicesBase + "annotate?";

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

// TODO: remove
//    private URI lookupURI(String shortname) {
//        // try to recover URI
//        URI uri;
//        try {
//            uri = URIUtils.getURI(prefixMappings, shortname);
//        }
//        catch (IllegalArgumentException e) {
//            // if we get an illegal argument exception, refresh cache and retry
//            getLog().debug(e.getMessage() + ": reloading prefix mappings cache and retrying...");
//            loadPrefixMappings();
//            uri = URIUtils.getURI(prefixMappings, shortname);
//        }
//        return uri;
//    }

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

    
    /**
     * This is a legacy method, coming from the old client, and kept here to ensure backward compatibility
     * with some ZOOMA dependant project. 
     * 
     * This method search the ontology terms corresponding to a textual value or pair. It basically returns a top 
     * score-ordered set of {@link AnnotationSummary} instances, i.e., ontology term URIs, their scores and a few 
     * other details.  
     * 
     * @param property      what you're looking for
     * @param score         returns only the summaries above this threshold
     * @param excludeType   generic, search, with no type specification
     * @param noEmptyResult if true and there is no summary scored above the threshold, tries to return something
     *                      anyway, i.e., summaries that are not so well scored. If this flag is false, the search
     *                      returns only summaries with score above the score parameter, possibly an empty Map.
     *                      
     * @return a {@link LinkedHashMap} map, where the entries are ordered by decreasing score.
     */
    @Override
	public Map<AnnotationSummary, Float> searchZOOMA ( 
		Property property, float score, boolean excludeType, boolean noEmptyResult )
	{
		String valueStr = property.getPropertyValue ();

		// search for annotation summaries
		Map<AnnotationSummary, Float> results = new LinkedHashMap<> ();

		// Exclude too long strings
		if ( !this.checkStringLimits ( property, excludeType ) ) return results;
		
		try
		{
			String search = zoomaBase + "summaries?query="
					+ URLEncoder.encode ( property.getPropertyValue (), "UTF-8" );
			String typedSearch = search + "&type=";
			URL queryURL = property instanceof TypedProperty && !excludeType ? new URL (
					typedSearch
							+ URLEncoder.encode (
									( (TypedProperty) property ).getPropertyType (), "UTF-8" ) )
					: new URL ( search );
			getLog ().trace ( "Sending query [" + queryURL + "]..." );

			ObjectMapper mapper = new ObjectMapper ();
			JsonNode resultsNode = mapper.readValue ( queryURL, JsonNode.class );
			if ( log.isTraceEnabled () ) log.trace ( resultsNode.toString () );

			List<AnnotationSummary> summaries = new ArrayList<> ();
			
			if ( resultsNode != null )
			{
				for ( JsonNode result : resultsNode )
				{
					// meets significance score?
					float resultScore = Float.parseFloat ( result.get ( "quality" )
							.asText () );
					AnnotationSummary as = mapAnnotationSummary ( result );
					getLog ().trace (
							"Annotation hit:\n\t\t" + "Searched: " + property + "\t"
									+ "Found: " + as.getAnnotatedPropertyValue () + " " + "["
									+ as.getAnnotatedPropertyType () + "] -> "
									+ as.getSemanticTags () + "\tScore: " + resultScore );

					summaries.add ( as );
				}
				
				if ( summaries.isEmpty () ) return results;
				
				// Now sort it based on score
				Collections.sort ( summaries, new Comparator<AnnotationSummary> () 
				{
					@Override
					public int compare ( AnnotationSummary as1, AnnotationSummary as2 ) {
						return (int) ( as2.getQuality () - as1.getQuality () );
					}
				});
				
				// And collect the results in the format requested by the legacy interface
				for ( AnnotationSummary as: summaries )
				{
					float asScore = as.getQuality ();

					// If we have a result with a good score, or 
					// we haven't seen a good result yet, but we want something in any case, then take the bad results
					// instead
					if ( asScore >= score || noEmptyResult && results.isEmpty () )
						results.put ( as, asScore );
					else
						// Else, we already have good results, or we only want them, let's discard the rest of the list
						break;
				}

				log.trace ( "Keeping {} out of {} total result(s)", results.size (), summaries.size () );
			}
		}
		catch ( IOException e )
		{
			throw new RuntimeException ( 
				"Failed to query ZOOMA for property '" + abbreviate ( valueStr, 30 ) + "' (" + e.getMessage () + ")", e 
			);
		}

		return results;
	}   


	private AnnotationSummary mapAnnotationSummary ( JsonNode summaryNode )
	{
		// acquire the annotation summary for this result
		String mid = summaryNode.get ( "id" ).asText ();
		float resultScore = (float) summaryNode.get ( "quality" ).asDouble ();

		JsonNode propUriJSN = summaryNode.get ( "annotatedPropertyUri" );
		String propUriStr = propUriJSN == null || propUriJSN.isNull () ? null
				: propUriJSN.asText ();
		URI propertyUri = propUriStr == null ? null : URI.create ( propUriStr );
		String propertyType = summaryNode.get ( "annotatedPropertyType" ).asText ();
		String propertyValue = summaryNode.get ( "annotatedPropertyValue" )
				.asText ();

		List<URI> semanticTags = new ArrayList<> ();
		JsonNode stsNode = summaryNode.get ( "semanticTags" );
		for ( JsonNode stNode : stsNode )
		{
			semanticTags.add ( URI.create ( stNode.asText () ) );
		}

		List<URI> annotationURIs = new ArrayList<> ();
		JsonNode annsNode = summaryNode.get ( "annotationURIs" );
		for ( JsonNode annNode : annsNode )
		{
			annotationURIs.add ( URI.create ( annNode.asText () ) );
		}

		List<URI> annotationSourceURIs = new ArrayList<> ();
		JsonNode annsSourceNode = summaryNode.get ( "annotationSourceURIs" );
		for ( JsonNode annSourceNode : annsSourceNode )
		{
			annotationSourceURIs.add ( URI.create ( annSourceNode.asText () ) );
		}

		// collect summary into map with it's score
		return new SimpleAnnotationSummary ( mid, propertyUri, propertyType,
				propertyValue, semanticTags, annotationURIs, resultScore,
				annotationSourceURIs );
	}
}
