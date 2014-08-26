package uk.ac.ebi.fgpt.zooma.search.ontodiscover;

import static org.apache.commons.lang3.StringUtils.abbreviate;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.utils.memory.SimpleCache;

/**
 * A wrapper of {@link OntologyTermDiscoverer} that caches the term of a base {@link OntologyTermDiscoverer discoverer}.
 * Both the base discoverer and the {@link OntoTermDiscoveryCache cache} can be customised, so that you might realise
 * pipeline multiple levels of caches or discoverers.  
 *
 * <dl><dt>date</dt><dd>May 27, 2013, modified Aug 2014</dd></dl>
 * @author Marco Brandizi
 *
 */
public class CachedOntoTermDiscoverer extends OntologyTermDiscoverer
{
	private final OntologyTermDiscoverer base;
	private final OntoTermDiscoveryCache cache;
	private final Logger log = LoggerFactory.getLogger ( this.getClass () );

	public static final List<DiscoveredTerm> NULL_RESULT = Collections.emptyList ();
	
	public CachedOntoTermDiscoverer ( OntologyTermDiscoverer base, OntoTermDiscoveryCache cache ) 
		throws IllegalArgumentException
	{
		if ( base == null ) throw new IllegalArgumentException ( 
			"A cached ontology term discoverer needs a non-null base discoverer" 
		);
		if ( cache == null ) throw new IllegalArgumentException ( 
			"A cached ontology term discoverer needs a non-null cache" 
		);
		this.base = base;
		this.cache = cache;
	}
			
	/**
	 * Defaults to  {@link SimpleCache} as caching object, so it frees entries with an LRU approach.
	 * 
	 */
	public CachedOntoTermDiscoverer ( OntologyTermDiscoverer base )
	{
		this ( base, new OntoTermMemCache () );
	}
			
	/**
	 * Returns the value in the cache, if available, else tries to discover a mapping via the base discoverer.
	 */
	@Override
	public List<DiscoveredTerm> getOntologyTermUris ( String valueLabel, String typeLabel ) 
		throws OntologyDiscoveryException
	{
  	if ( ( valueLabel = StringUtils.trimToNull ( valueLabel ) ) == null ) return NULL_RESULT;
  	valueLabel = valueLabel.toLowerCase ();
  	typeLabel = StringUtils.trimToEmpty ( typeLabel ).toLowerCase ();
  	
  	// The class name is added to further minimise the small chance that someone synchronise on this same entry 
  	Object synch = cache.getSynchronisingObject ( valueLabel, typeLabel );

  	synchronized ( synch )
  	{
			List<DiscoveredTerm> result = cache.getOntologyTermUris ( valueLabel, typeLabel );
			if ( result != null )
			{
				if ( log.isTraceEnabled () )
					log.trace ( 
						"Returning cached result " + abbreviate ( result.toString (), 50 ) + " for '{}:{}'",
						valueLabel, typeLabel
					);
				return result;
			}
			
			result = base.getOntologyTermUris ( valueLabel, typeLabel );
			if ( result == null ) 
			{
				log.trace ( "Null result for '{}:{}', turning it into an empty list", typeLabel, valueLabel );
				result = NULL_RESULT;
			}
		
			if ( log.isTraceEnabled () ) log.trace ( 
				"Returning and caching '" + abbreviate ( result.toString (), 50 ) + "' for '{}:{}'", typeLabel, valueLabel 
			);
			
			cache.save ( valueLabel, typeLabel, result );
			return result;
			
  	} // synchronized sequence
	}

	public void clearCache () {
		cache.clear ();
	}
}
