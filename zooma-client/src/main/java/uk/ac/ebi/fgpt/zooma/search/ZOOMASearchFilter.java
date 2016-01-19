package uk.ac.ebi.fgpt.zooma.search;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.fgpt.zooma.model.Annotation;
import uk.ac.ebi.fgpt.zooma.model.AnnotationSummary;
import uk.ac.ebi.fgpt.zooma.model.Property;

/**
 * A filter for {@link ZOOMASearchInterface}, which is intended to implement decorators, performing some additional
 * operations before/after the upstream call. For example, @see {@link StatsZOOMASearchFilter}.
 * 
 * TODO: support new methods, as explained in {@link ZOOMASearchInterface}.
 *
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>17 Dec 2014</dd>
 *
 */
public class ZOOMASearchFilter extends AbstractZOOMASearch
{
	protected ZOOMASearchInterface base;
		
	public ZOOMASearchFilter ( ZOOMASearchInterface base )
	{
		super ();
		this.base = base;
	}

	@Override
	public Map<String, String> getPrefixMappings () throws IOException
	{
		return base.getPrefixMappings ();
	}

	@Override
	public Map<AnnotationSummary, Float> searchZOOMA ( Property property, float score, boolean excludeType, boolean noEmptyResult )
	{
		return base.searchZOOMA ( property, score, excludeType, noEmptyResult );
	}

	@Override
	public Annotation getAnnotation ( URI annotationURI )
	{
		return base.getAnnotation ( annotationURI );
	}

	@Override
	public String getLabel ( URI uri ) throws IOException
	{
		return base.getLabel ( uri );
	}

	@Override
	public int getMaxPropertyValueLength ()
	{
		return base.getMaxPropertyValueLength ();
	}

	@Override
	public void setMaxPropertyValueLength ( int maxPropertyValueLength )
	{
		base.setMaxPropertyValueLength ( maxPropertyValueLength );
	}

	@Override
	public int getMaxPropertyTypeLength ()
	{
		return base.getMaxPropertyTypeLength ();
	}

	@Override
	public void setMaxPropertyTypeLength ( int maxPropertyTypeLength )
	{
		base.setMaxPropertyTypeLength ( maxPropertyTypeLength );
	}
	
}
