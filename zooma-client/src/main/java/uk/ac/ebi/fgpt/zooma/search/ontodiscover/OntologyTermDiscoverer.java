package uk.ac.ebi.fgpt.zooma.search.ontodiscover;

import java.net.URI;
import java.util.List;

/**
 * A generic interface for representing a service that is able to find the URI of an OWL class which of a value/type
 * label strings are assumed to represent an instance. Specific implementations use services like ZOOMA for that.
 *
 * <dl><dt>date</dt><dd>May 27, 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public abstract class OntologyTermDiscoverer
{
	/**
	 * Used to represent a discovered result.
	 *
	 * <dl><dt>date</dt><dd>8 Aug 2014</dd></dl>
	 * @author Marco Brandizi
	 *
	 */
	public static class DiscoveredTerm
	{
		private URI uri;
		private float score;
		
		public DiscoveredTerm ( URI uri, float score )
		{
			super ();
			this.uri = uri;
			this.score = score;
		}

		public URI getUri ()
		{
			return uri;
		}

		public float getScore ()
		{
			return score;
		}
		
		@Override
		public String toString () {
			return String.format ( "<%s> (%f)", getUri (), getScore () );
		}

	}
	
	
	/**
	 * Returns a score-ordered list of {@link DiscoveredTerm terms} associated to the parameters. 
	 * Should return an empty list if no sensible URI was found for the parameters (or if the parameters are null or invalid)
	 */
	public abstract List<DiscoveredTerm> getOntologyTermUris ( String valueLabel, String typeLabel ) throws OntologyDiscoveryException;
	
}
