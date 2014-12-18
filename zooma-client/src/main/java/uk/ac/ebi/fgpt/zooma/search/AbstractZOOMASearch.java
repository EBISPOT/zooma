package uk.ac.ebi.fgpt.zooma.search;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.fgpt.zooma.model.AnnotationSummary;
import uk.ac.ebi.fgpt.zooma.model.Property;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>17 Dec 2014</dd>
 *
 */
public abstract class AbstractZOOMASearch implements ZOOMASearchInterface
{

	private int maxPropertyValueLength = 150;
	private int maxPropertyTypeLength = 150;
	protected Logger log = LoggerFactory.getLogger ( this.getClass () );


	@Override
	public Map<AnnotationSummary, Float> searchZOOMA ( Property property, float score )
	{
		return searchZOOMA ( property, score, false );
	}

	@Override
	public Map<AnnotationSummary, Float> searchZOOMA ( Property property, float score, boolean excludeType )
	{
		return searchZOOMA ( property, score, excludeType, false );
	}

	/**
	 * The client will ignore property values longer than this and it won't contact the web service if it meets them
	 * This defaults to 150, which we computed by looking at some statistics on the stuff stored in ZOOMA, i.e., 
	 * average property value length is 29 and 99% of them are shorter than 100.
	 */
	@Override
	public int getMaxPropertyValueLength ()
	{
		return maxPropertyValueLength;
	}

	@Override
	public void setMaxPropertyValueLength ( int maxPropertyValueLength )
	{
		this.maxPropertyValueLength = maxPropertyValueLength;
	}

	/**
	 * The client will ignore property types longer than this and it won't contact the web service if it meets them
	 * This defaults to 150, which we computed by looking at some statistics on the stuff stored in ZOOMA, i.e., 
	 * average property type length is 21 and it's never longer than 121.
	 */
	@Override
	public int getMaxPropertyTypeLength ()
	{
		return maxPropertyTypeLength;
	}

	@Override
	public void setMaxPropertyTypeLength ( int maxPropertyTypeLength )
	{
		this.maxPropertyTypeLength = maxPropertyTypeLength;
	}

}