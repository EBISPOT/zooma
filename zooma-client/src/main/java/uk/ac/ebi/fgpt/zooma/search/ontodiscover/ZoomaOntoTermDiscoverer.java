package uk.ac.ebi.fgpt.zooma.search.ontodiscover;

import static uk.ac.ebi.fgpt.zooma.search.ontodiscover.CachedOntoTermDiscoverer.NULL_RESULT;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.fgpt.zooma.model.AnnotationSummary;
import uk.ac.ebi.fgpt.zooma.model.Property;
import uk.ac.ebi.fgpt.zooma.model.SimpleTypedProperty;
import uk.ac.ebi.fgpt.zooma.model.SimpleUntypedProperty;
import uk.ac.ebi.fgpt.zooma.search.StatsZOOMASearchFilter;
import uk.ac.ebi.fgpt.zooma.search.ZOOMASearchClient;
import uk.ac.ebi.fgpt.zooma.search.ZOOMASearchInterface;

/**
 * Ontology Discoverer based on <a href = 'http://www.ebi.ac.uk/fgpt/zooma/docs/'>ZOOMA2</a>.
 *
 * <dl><dt>date</dt><dd>23 Oct 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class ZoomaOntoTermDiscoverer extends OntologyTermDiscoverer
{
	private ZOOMASearchInterface zoomaSearcher;
	private float zoomaThreesholdScore;
	
	private Logger log = LoggerFactory.getLogger ( this.getClass () );
	
	/**
	 * You can pass me your own ZOOMA client, useful if you want send in wrappers like {@link StatsZOOMASearchFilter}.
	 */
	public ZoomaOntoTermDiscoverer ( ZOOMASearchInterface zoomaSearcher, float zoomaThreeSholdScore )
	{
		this.zoomaSearcher = zoomaSearcher;
		this.zoomaThreesholdScore = zoomaThreeSholdScore;
	}

	/**
	 * Defaults to 80
	 */
	public ZoomaOntoTermDiscoverer ( ZOOMASearchInterface zoomaClient )
	{
		this ( zoomaClient, 80f );
	}
	
	/**
	 * Uses the top-ranked result from {@link ZOOMASearchClient}.searchZOOMA(), it sends to it a pair of value and type
	 * label, depending on the fact that the type is null or not.  
	 */
	@Override
	public List<DiscoveredTerm> getOntologyTermUris ( String valueLabel, String typeLabel )
		throws OntologyDiscoveryException
	{
		try
		{
			if ( (valueLabel = StringUtils.trimToNull ( valueLabel )) == null ) return NULL_RESULT;
			typeLabel = StringUtils.trimToNull ( typeLabel );
			
			Property zprop = typeLabel == null 
				? new SimpleUntypedProperty ( valueLabel ) 
				: new SimpleTypedProperty ( typeLabel, valueLabel ); 

				Map<AnnotationSummary, Float> zresult = zoomaSearcher.searchZOOMA ( zprop, zoomaThreesholdScore, typeLabel == null );
			
			// TODO: apply the logics suggested by ZOOMA people:
			// - 80 is a good threshold
			// - The difference between the score of the last accepted result and the first discarded should be > 10 
			//
				
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

			// ZOOMA uses a SortedMap internally and returns results in score descending order
			// So, here we picks best-scored unique results automatically
			//
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
	 * such threshold are returned. Default is 80.
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
