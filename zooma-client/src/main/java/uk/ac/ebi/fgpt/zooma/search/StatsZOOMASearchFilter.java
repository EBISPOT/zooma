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
 *
 * <dl><dt>date</dt><dd>16 Nov 2014</dd></dl>
 * @author Marco Brandizi
 *
 */
public class StatsZOOMASearchFilter extends ZOOMASearchFilter
{
	private long searchZOOMACalls = 0, failedSearchZOOMACalls = 0;
	private double avgSearchZOOMACalls = -1, avgFailedSearchZOOMACalls = -1;
	
	private long getAnnotationCalls = 0, failedGetAnnotationCalls = 0;
	private double avgGetAnnotationCalls = -1, avgFailedGetAnnotationCalls = -1;
	
	private long getLabelCalls = 0, failedGetLabelCalls = 0;
	private double avgGetLabelCalls = -1, avgFailedGetLabelCalls = -1;
	
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
			
			avgSearchZOOMACalls = avgSearchZOOMACalls == -1 
				? 1.0 * searchZOOMACalls / samplingTimeMs  
				: ( 1.0 * searchZOOMACalls / samplingTimeMs + avgSearchZOOMACalls ) / 2;

			double thisFailedSearchZOOMACalls = failedSearchZOOMACalls == 0 ? 0 : 1.0 * failedSearchZOOMACalls / searchZOOMACalls;
			avgFailedSearchZOOMACalls = avgFailedSearchZOOMACalls == -1 
				? thisFailedSearchZOOMACalls 
				: ( thisFailedSearchZOOMACalls + avgSearchZOOMACalls ) / 2;

			log.info ( String.format ( 
				"---- ZOOMA Statistics, searchZOOMA(), throughput: %.0f calls/min, failed: %.1f %%",
				avgSearchZOOMACalls * 60000, avgFailedSearchZOOMACalls * 100
			));
			
			searchZOOMACalls = failedSearchZOOMACalls = 0;


			// ---- getAnnotation () ----

			avgGetAnnotationCalls = avgGetAnnotationCalls == -1 
				? 1.0 * getAnnotationCalls / samplingTimeMs
				: ( 1.0 * getAnnotationCalls / samplingTimeMs + avgGetAnnotationCalls ) / 2; 
			
			double thisFailedGetAnnotationCalls = failedGetAnnotationCalls == 0 ? 0 : 1.0 * failedGetAnnotationCalls / getAnnotationCalls;
			avgFailedGetAnnotationCalls = avgFailedGetAnnotationCalls == -1 
				? thisFailedGetAnnotationCalls 
				: ( thisFailedGetAnnotationCalls + avgGetAnnotationCalls ) / 2;
			
			log.info ( String.format ( 
				"---- ZOOMA Statistics, getAnnotation(), throughput: %.0f calls/min, failed: %.1f %%",
				avgGetAnnotationCalls * 60000, avgFailedGetAnnotationCalls * 100
			));
			
			getAnnotationCalls = failedGetAnnotationCalls = 0;

			
			// ---- getLabel () ----

			avgGetLabelCalls = avgGetLabelCalls == -1  
				? 1.0 * getLabelCalls / samplingTimeMs
				: ( getLabelCalls / samplingTimeMs + avgGetLabelCalls ) / 2;
			
			double thisFailedGetLabelCalls = failedGetLabelCalls == 0 ? 0 : 1.0 * failedGetLabelCalls / getLabelCalls;
			avgFailedGetLabelCalls = avgFailedGetLabelCalls == -1 
				? thisFailedGetLabelCalls 
				: ( thisFailedGetLabelCalls + avgGetLabelCalls ) / 2;
			
			log.info ( String.format ( 
				"---- ZOOMA Statistics, getLabel(), throughput: %.0f calls/min, failed: %.1f %%",
				avgGetLabelCalls * 60000, avgFailedGetLabelCalls * 100
			));
			
			getLabelCalls = failedGetLabelCalls = 0;
			
			timer.reset ();
			timer.start ();
		}
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
	
}
