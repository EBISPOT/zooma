package uk.ac.ebi.fgpt.zooma.search;

import java.io.IOException;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

import uk.ac.ebi.fgpt.zooma.model.Annotation;
import uk.ac.ebi.fgpt.zooma.model.AnnotationSummary;
import uk.ac.ebi.fgpt.zooma.model.Property;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>17 Dec 2014</dd>
 *
 */
public interface ZOOMASearchInterface
{
	public Map<String, String> getPrefixMappings () throws IOException;

	public Map<AnnotationSummary, Float> searchZOOMA ( Property property,
			float score );

	public Map<AnnotationSummary, Float> searchZOOMA ( Property property,
			float score, boolean excludeType );

	/**
	 * @param property      what you're looking for
	 * @param score         returns only the summaries above this threshold
	 * @param excludeType   generic, search, with no type specification
	 * @param noEmptyResult if true and there is no summary scored above the threshold, tries to return something
	 *                      anyway, i.e., summaries that are not so well scored. If this flag is false, the search
	 *                      returns only summaries with score above the score parameter, possibly an empty Map.
	 * @return a {@link LinkedHashMap} map, where the entries are ordered by decreasing score.
	 */
	public Map<AnnotationSummary, Float> searchZOOMA ( Property property,
			float score, boolean excludeType, boolean noEmptyResult );

	public Annotation getAnnotation ( URI annotationURI );

	public String getLabel ( URI uri ) throws IOException;

	/**
	 * The client will ignore property values longer than this and it won't contact the web service if it meets them
	 * This defaults to 150, which we computed by looking at some statistics on the stuff stored in ZOOMA, i.e., 
	 * average property value length is 29 and 99% of them are shorter than 100.
	 */
	public int getMaxPropertyValueLength ();

	public void setMaxPropertyValueLength ( int maxPropertyValueLength );

	/**
	 * The client will ignore property types longer than this and it won't contact the web service if it meets them
	 * This defaults to 150, which we computed by looking at some statistics on the stuff stored in ZOOMA, i.e., 
	 * average property type length is 21 and it's never longer than 121.
	 */
	public int getMaxPropertyTypeLength ();

	public void setMaxPropertyTypeLength ( int maxPropertyTypeLength );

}