package uk.ac.ebi.fgpt.zooma.search;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import uk.ac.ebi.fgpt.zooma.model.Annotation;
import uk.ac.ebi.fgpt.zooma.model.AnnotationSummary;
import uk.ac.ebi.fgpt.zooma.model.Property;
import uk.ac.ebi.utils.time.XStopWatch;

/**
 * A wrapper of {@link ZOOMASearchFilter}, which dynamically logs usage statistics (no of calls, throughput, etc).
 * It also has a capability to throttle the client throughput, so that the server is not crashed by too many
 * requests.
 * 
 * <dl><dt>date</dt><dd>16 Nov 2014</dd></dl>
 * @author Marco Brandizi
 *
 */
public class StatsZOOMASearchFilter extends ZOOMASearchFilter
{
	private long searchZOOMACalls = 0, failedSearchZOOMACalls = 0;
	private double avgSearchZOOMACalls = 0, avgFailedSearchZOOMACalls = 0;
	
	private long getAnnotationCalls = 0, failedGetAnnotationCalls = 0;
	private double avgGetAnnotationCalls = 0, avgFailedGetAnnotationCalls = 0;
	
	private long getLabelCalls = 0, failedGetLabelCalls = 0;
	private double avgGetLabelCalls = 0, avgFailedGetLabelCalls = 0;

	private boolean throttleMode = false;
	
	private boolean isThrottling = false;
	
	private long samplingTimeMs = 15 * 1000 * 60;

	private Object statsLock = new Object ();
	
	private XStopWatch timer = new XStopWatch ();


	public StatsZOOMASearchFilter ( ZOOMASearchInterface base )
	{
		super ( base );
	}

	
	@Override
	public Map<AnnotationSummary, Float> searchZOOMA ( Property property, float score, boolean excludeType, boolean noEmptyResult )
	{
		doThrottle ();
		if ( timer.isStopped () ) timer.start ();
		
		try {
			return base.searchZOOMA ( property, score, excludeType, noEmptyResult );
		}
		catch ( Exception ex )
		{
			failedSearchZOOMACalls++;
			throw ex;
		}
		finally 
		{
			searchZOOMACalls++;
			doStats ();
		}
	}

	@Override
	public Annotation getAnnotation ( URI annotationURI )
	{
		doThrottle ();
		if ( timer.isStopped () ) timer.start ();
		
		try {
			return base.getAnnotation ( annotationURI );
		}
		catch ( Exception ex )
		{
			failedGetAnnotationCalls++;
			throw ex;
		}
		finally 
		{
			getAnnotationCalls++;
			doStats ();
		}
	}

	@Override
	public String getLabel ( URI uri ) throws IOException
	{
		doThrottle ();
		if ( timer.isStopped () ) timer.start ();
		
		try {
			return base.getLabel ( uri );
		}
		catch ( Exception ex )
		{
			failedGetLabelCalls++;
			throw ex;
		}
		finally 
		{
			getLabelCalls++;
			doStats ();
		}
	}

	
	private void doStats ()
	{
		if ( timer.getTime () < samplingTimeMs ) return;

		synchronized ( statsLock )
		{
			if ( timer.getTime () < samplingTimeMs ) return;
			
			// ---- searchZOOMA () ----
			
			avgSearchZOOMACalls = 1.0 * searchZOOMACalls / samplingTimeMs;  
			if ( searchZOOMACalls > 0 ) avgFailedSearchZOOMACalls = 1.0 * failedSearchZOOMACalls / searchZOOMACalls;

			log.info ( String.format ( 
				"---- ZOOMA Statistics, searchZOOMA(), throughput: %.0f calls/min, failed: %.1f %%",
				avgSearchZOOMACalls * 60000, avgFailedSearchZOOMACalls * 100
			));
			
			searchZOOMACalls = failedSearchZOOMACalls = 0;

			// ---- getAnnotation () ----

			avgGetAnnotationCalls = 1.0 * getAnnotationCalls / samplingTimeMs;
			if ( getAnnotationCalls > 0 ) avgFailedGetAnnotationCalls = 1.0 * failedGetAnnotationCalls / getAnnotationCalls; 
			
			log.info ( String.format ( 
				"---- ZOOMA Statistics, getAnnotation(), throughput: %.0f calls/min, failed: %.1f %%",
				avgGetAnnotationCalls * 60000, avgFailedGetAnnotationCalls * 100
			));
			
			getAnnotationCalls = failedGetAnnotationCalls = 0;

			
			// ---- getLabel () ----

			avgGetLabelCalls = 1.0 * getLabelCalls / samplingTimeMs;
			if ( getLabelCalls > 0 ) avgFailedGetLabelCalls = 1.0 * failedGetLabelCalls / getLabelCalls;
			
			log.info ( String.format ( 
				"---- ZOOMA Statistics, getLabel(), throughput: %.0f calls/min, failed: %.1f %%",
				avgGetLabelCalls * 60000, avgFailedGetLabelCalls * 100
			));
			
			getLabelCalls = failedGetLabelCalls = 0;
			
			timer.reset ();
			timer.start ();
		}
	}

	
	private boolean doThrottle ()
	{
		if ( !throttleMode ) return false;
		double failedCalls = Math.max ( 
			Math.max ( avgFailedSearchZOOMACalls, avgFailedGetAnnotationCalls ),
			avgFailedGetLabelCalls
		);
		
		if ( failedCalls <= 0.1 ) 
		{
			if ( isThrottling ) {
				log.info ( "ZOOMA back to good performance, throttling ends" );
				isThrottling = false;
			}
			return false;
		}

		// The thresholds are related to twice their values most of the time, eg, 
		// previous checkpoint it was 0, then it becomes 35, average is 17.5
		long delay = 
			failedCalls <= 0.30 ? 500 
			: failedCalls <= 0.50 ? 5 * 1000 
			: failedCalls <= 0.70 ? 1 * 60 * 1000
			: 5 * 60 * 1000; 

		if ( !isThrottling ) {
			log.info ( "Throttling ZOOMA to avoid server crashing, calls are slowed down by {}ms per call", delay );
			isThrottling = true;
		}
		
		try {
			Thread.sleep ( delay );
		}
		catch ( InterruptedException e ) {
			throw new RuntimeException ( "Internal error: " + e.getMessage (), e );
		}
		
		return true;
	}
	
	
	
	public long getSamplingTimeMs ()
	{
		return samplingTimeMs;
	}

	public synchronized void setSamplingTimeMs ( long samplingTimeMs )
	{
		this.samplingTimeMs = samplingTimeMs;
	}

	public double getAvgSearchZOOMACalls ()
	{
		return avgSearchZOOMACalls;
	}

	public double getAvgFailedSearchZOOMACalls ()
	{
		return avgFailedSearchZOOMACalls;
	}

	public double getAvgGetAnnotationCalls ()
	{
		return avgGetAnnotationCalls;
	}

	public double getAvgFailedGetAnnotationCalls ()
	{
		return avgFailedGetAnnotationCalls;
	}

	public double getAvgGetLabelCalls ()
	{
		return avgGetLabelCalls;
	}

	public double getAvgFailedGetLabelCalls ()
	{
		return avgFailedGetLabelCalls;
	}

	/**
	 * If true, calls to the server are slowed down when the failure ratio is too high and things are speed-up again
	 * when such ratio goes back to normal. This is to prevent crashes we have with ZOOMA server, when we hammer at
	 * it too much. 
	 */
	public boolean isThrottleMode ()
	{
		return throttleMode;
	}


	public void setThrottleMode ( boolean throttleMode )
	{
		this.throttleMode = throttleMode;
	}
	
}
