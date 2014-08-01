package uk.ac.ebi.fgpt.zooma.search.ontodiscover;

import static uk.ac.ebi.fgpt.zooma.search.ontodiscover.CachedOntoTermDiscoverer.NULL_RESULT;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.fgpt.zooma.model.AnnotationSummary;
import uk.ac.ebi.fgpt.zooma.model.Property;
import uk.ac.ebi.fgpt.zooma.model.SimpleTypedProperty;
import uk.ac.ebi.fgpt.zooma.model.SimpleUntypedProperty;
import uk.ac.ebi.fgpt.zooma.search.ZOOMASearchClient;

/**
 * Ontology Discoverer based on <a href = 'http://www.ebi.ac.uk/fgpt/zooma/docs/'>ZOOMA2</a>.
 *
 * <dl><dt>date</dt><dd>23 Oct 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class Zooma2OntoTermDiscoverer extends OntologyTermDiscoverer
{
	private ZOOMASearchClient zoomaClient;
	private float zoomaThreesholdScore = 70.0f;
	
	private Logger log = LoggerFactory.getLogger ( this.getClass () );
	
	public Zooma2OntoTermDiscoverer ( String zoomaLocation )
	{
		try
		{
			zoomaClient = new ZOOMASearchClient ( new URL ( zoomaLocation ) );
		} 
		catch ( MalformedURLException ex ) {
			throw new OntologyDiscoveryException ( "Internal error while instantiating Zooma: " + ex.getMessage (), ex );
		}
	}

	
	public Zooma2OntoTermDiscoverer ( URL zoomaLocation ) 
	{
		zoomaClient = new ZOOMASearchClient ( zoomaLocation );
	}

	public Zooma2OntoTermDiscoverer () 
	{
		this ( "http://www.ebi.ac.uk/fgpt/zooma" );
	}
	
	
	/**
	 * Uses the top-ranked result from {@link ZOOMASearchClient}.searchZOOMA(), it sends to it a pair of value and type
	 * label, depending on the fact that the type is null or not.  
	 */
	@Override
	public List<DiscoveredTerm> getOntologyTermUri ( String valueLabel, String typeLabel )
		throws OntologyDiscoveryException
	{
		try
		{
			if ( (valueLabel = StringUtils.trimToNull ( valueLabel )) == null ) return NULL_RESULT;
			typeLabel = StringUtils.trimToNull ( typeLabel );
			
			Property zprop = typeLabel == null 
				? new SimpleUntypedProperty ( valueLabel ) 
				: new SimpleTypedProperty ( typeLabel, valueLabel ); 

				Map<AnnotationSummary, Float> zresult = zoomaClient.searchZOOMA ( zprop, zoomaThreesholdScore, typeLabel == null );
			
			if ( zresult == null || zresult.size () == 0 ) return NULL_RESULT;
			List<DiscoveredTerm> result = new ArrayList<> ();
			for ( AnnotationSummary zsum: zresult.keySet () )
			{
				float score = zsum.getQualityScore ();
				
				Collection<URI> semTags = zsum.getSemanticTags ();
				if ( semTags == null ) continue;
				
				for ( URI uri: semTags )
					result.add ( new DiscoveredTerm ( uri, score ) );
			}

			// Now sort it, cause we cannot be sure of the returned order
			/* Actually not needed, we know a sorted linked-map is returned
			Collections.sort ( result, new Comparator<DiscoveredTerm>() 
			{
				@Override
				public int compare ( DiscoveredTerm t1, DiscoveredTerm t2 )
				{
						return Float.compare ( t2.getScore (), t1.getScore () );
				}
			});*/
			
			// Also remove duplicates
			// This automatically picks the best-scored result, cause we've sorted them
			List<DiscoveredTerm> resultUniqs = new ArrayList<> ();
			DiscoveredTerm prevTerm = null;
			for ( DiscoveredTerm t: result )
				if ( prevTerm == null || !t.getUri ().equals ( prevTerm.getUri () ) ) {
					resultUniqs.add ( prevTerm = t );
				}
			
			// And eventually, here you are
			return resultUniqs;
		} 
		catch ( Exception ex )
		{
			log.error ( String.format ( 
				"Error while consulting ZOOMA for '%s' / '%s': %s. Returning null", 
				valueLabel, typeLabel, ex.getMessage () ), ex 
			);
			return null;
		}
	}


	/**
	 * This is passed to {@link ZOOMASearchClient#searchZOOMA(Property, float)} and hence only results above
	 * such threeshold are returned.
	 */
	public float getZoomaThreesholdScore ()
	{
		return zoomaThreesholdScore;
	}

	public void setZoomaThreesholdScore ( float zoomaThreesholdScore )
	{
		this.zoomaThreesholdScore = zoomaThreesholdScore;
	}
	
}
