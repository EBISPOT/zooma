package uk.ac.ebi.fgpt.zooma.search.ontodiscover;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.fgpt.zooma.search.ZOOMASearchClient;
import uk.ac.ebi.fgpt.zooma.search.ontodiscover.OntologyTermDiscoverer.DiscoveredTerm;
import uk.ac.ebi.utils.memory.SimpleCache;
import uk.ac.ebi.utils.time.XStopWatch;

/**
 * Tests the uk.ac.ebi.fgpt.zooma.search.ontodiscover package.
 *
 * <dl><dt>date</dt><dd>31 Jul 2014</dd></dl>
 * @author Marco Brandizi
 *
 */
public class OntoTermDiscovererTest
{
	private Logger log = LoggerFactory.getLogger ( this.getClass () );
	
	@Test
	public void testBasics ()
	{
		OntologyTermDiscoverer client = new ZoomaOntoTermDiscoverer ( new ZOOMASearchClient (), 50f );
		List<DiscoveredTerm> terms = client.getOntologyTermUris ( "homo sapiens", "specie" );

		log.info ( "Discovered terms for Homo Sapiens:\n" + terms );
		
		boolean hasNCBITaxon_9606 = false, hasDupes = false;
		Set<String> uris = new HashSet<> ();
		for ( DiscoveredTerm term: terms )
		{
			hasNCBITaxon_9606 = hasNCBITaxon_9606 || StringUtils.contains ( term.getUri ().toASCIIString (), "NCBITaxon_9606" );
			if ( uris.contains ( term.getUri () ) )
			{
				log.error ( "Ouch! the term '{}' is duplicated!", term.getUri () );
				hasDupes = true;
			}
		}
		
		assertTrue ( "Damn! I couldn't find NCBITax:9606!", hasNCBITaxon_9606 );
		assertFalse ( "Oh no! results has duplicates!", hasDupes );
	}
	
	
	@Test
	public void testCache()
	{
		XStopWatch timer = new XStopWatch ();
		
		Map<String, List<DiscoveredTerm>> baseCache = new SimpleCache<> ( 1000 );
		OntologyTermDiscoverer client = new CachedOntoTermDiscoverer ( 
			new ZoomaOntoTermDiscoverer ( new ZOOMASearchClient () ), new OntoTermDiscoveryMemCache ( baseCache )
		);
		
		timer.start ();
		List<DiscoveredTerm> terms = client.getOntologyTermUris ( "homo sapiens", "organism" );
		long time1 = timer.getTime ();
				
		assertEquals ( "entry not saved in the cache!", terms, baseCache.get ( "organism:homo sapiens" ) );
		
		timer.reset ();
		timer.start ();
		for ( int i = 0; i < 100; i++ )
		{
			terms = client.getOntologyTermUris ( "homo sapiens", "organism" );
			log.trace ( "Call {}, time {}", i, timer.getTime () );
		}
		timer.stop ();
		
		double time2 = timer.getTime () / 100.0;
		
		log.info ( "Second-call versus first-call time: {}, {}", time2, time1 );
		assertTrue ( "WTH?! Second call time bigger than first!", time2 < time1 );
	}
	
}
