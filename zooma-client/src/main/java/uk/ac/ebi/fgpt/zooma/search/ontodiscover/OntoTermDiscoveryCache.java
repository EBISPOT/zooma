package uk.ac.ebi.fgpt.zooma.search.ontodiscover;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>1 Aug 2014</dd></dl>
 * @author Marco Brandizi
 *
 */
public abstract class OntoTermDiscoveryCache extends OntologyTermDiscoverer
{
	
	public abstract List<DiscoveredTerm> save ( String valueLabel, String typeLabel, List<DiscoveredTerm> terms )
		throws OntologyDiscoveryException;

	
	/**
	 * This is used by {@link CachedOntoTermDiscoverer}, to synchronise the check and update of an entry. It should
	 * return an object that is shared in the JVM and is linked to the entry, so that a synchronized() block around it
	 * allows us to safely see if the already exists in the cache and possibly create it.  
	 * 
	 * By default, we return (this.getClass ().getName () + ":" + trimToEmpty(typeLabel) + ":" + valueLabel ).intern (), which 
	 * has the required features (the class is added, in order to avoid the unlikely case the type/value string 
	 * combination is used somewhere else.
	 *  
	 */
	public Object getSynchronisingObject ( String valueLabel, String typeLabel )
	{
		return (this.getClass ().getName () + ":" + StringUtils.trimToEmpty ( typeLabel ) + ":" + valueLabel ).intern (); 
	}
	
	/**
	 * Empty the cache, default doesn't do anything.
	 */
	public void clear () {
	}
	
}
