package uk.ac.ebi.fgpt.zooma.search.ontodiscover;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import uk.ac.ebi.utils.memory.SimpleCache;

/**
 * A memory cache, based on {@link SimpleCache}.
 *
 * <dl><dt>date</dt><dd>1 Aug 2014</dd></dl>
 * @author Marco Brandizi
 *
 */
public class OntoTermMemCache extends OntoTermDiscoveryCache
{
	private final Map<String, List<DiscoveredTerm>> baseCache;

	public OntoTermMemCache ( Map<String, List<DiscoveredTerm>> baseCache )
	{
		this.baseCache = baseCache;
	}

	public OntoTermMemCache ()
	{
		this ( new SimpleCache<String, List<DiscoveredTerm>> ( (int) 1E6 ) );
	}
	
	@Override
	public List<DiscoveredTerm> save ( String valueLabel, String typeLabel, List<DiscoveredTerm> terms )
		throws OntologyDiscoveryException
	{
		return baseCache.put ( StringUtils.trimToEmpty ( typeLabel )  + ":" + valueLabel, terms );
	}

	@Override
	public List<DiscoveredTerm> getOntologyTermUris ( String valueLabel, String typeLabel ) throws OntologyDiscoveryException
	{
		return baseCache.get ( StringUtils.trimToEmpty ( typeLabel )  + ":" + valueLabel );
	}

	@Override
	public void clear ()
	{
		baseCache.clear ();
	}
	
}
