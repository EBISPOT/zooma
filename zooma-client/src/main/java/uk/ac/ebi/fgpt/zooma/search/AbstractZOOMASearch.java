package uk.ac.ebi.fgpt.zooma.search;

import static org.apache.commons.lang3.StringUtils.length;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.fgpt.zooma.model.AnnotationSummary;
import uk.ac.ebi.fgpt.zooma.model.Property;
import uk.ac.ebi.fgpt.zooma.model.TypedProperty;

/**
 * A base implementation of common elements in {@link ZOOMASearchInterface}.
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
	 * @return true if the parameters are within {@link #getMaxPropertyValueLength()} and 
	 * {@link #getMaxPropertyTypeLength()}. This should be used to implement 
	 * {@link #searchZOOMA(Property, float, boolean, boolean)}.
	 */
	protected boolean checkStringLimits ( Property property, boolean excludeType ) 
  {
    if ( length ( property.getPropertyValue () )  > this.getMaxPropertyValueLength () ) return false;
    if ( excludeType || ! ( property instanceof TypedProperty ) ) return true;
    return length ( ( (TypedProperty) property ).getPropertyType() ) <= this.getMaxPropertyTypeLength ();
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