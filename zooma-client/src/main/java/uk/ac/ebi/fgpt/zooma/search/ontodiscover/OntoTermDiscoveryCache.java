package uk.ac.ebi.fgpt.zooma.search.ontodiscover;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * A generic cache interface for {@link OntologyTermDiscoverer}. See {@link #save(String, String, List)} for details.
 * This is mainly useful for {@link CachedOntoTermDiscoverer} and your cache implementation should extend this interface.
 * 
 * Basically, when a result hasn't {@link #save(String, String, List) saved} yet in the cache, {@link #getOntologyTermUris(String, String)}
 * should return null, after a result is saved, it should return it. Note that this include the case a string pair is 
 * not associated to any URI, in which case the cache should represent it as an empty string (which is different than 
 * null, in the sense that you now know the result is empty).
 *
 * <dl><dt>date</dt><dd>1 Aug 2014</dd></dl>
 * @author Marco Brandizi
 *
 */
public abstract class OntoTermDiscoveryCache extends OntologyTermDiscoverer
{
	/**
	 * This should be used when you have a new string/URI mapping that you want to save in the cache. After the call to this
	 * method {@link #getOntologyTermUris(String, String)} will return the saved result, until the cache's internal decides
	 * that's too old (in which case it will return null again), or {@link #clear()} is invoked.
	 * 
	 * Note that you should save a string pair returning no good URI, as an empty list. This is the let the cache know
	 * the discovery has already been attempted on that pair (nulls are for when this hasn't been done yet).
	 *  
	 * @return should return the old value in the cache, if any, null otherwise.
	 */
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
	public void clear () {}
	
}
