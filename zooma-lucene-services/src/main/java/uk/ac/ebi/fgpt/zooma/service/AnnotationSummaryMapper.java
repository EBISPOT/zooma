package uk.ac.ebi.fgpt.zooma.service;

import org.apache.lucene.document.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.zooma.model.AnnotationSummary;
import uk.ac.ebi.fgpt.zooma.model.SimpleAnnotationSummary;

import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

/**
 * Maps lucene documents into {@link AnnotationSummary} objects.
 *
 * @author Tony Burdett
 * @date 10/07/13
 */
public class AnnotationSummaryMapper implements LuceneDocumentMapper<AnnotationSummary> {
    private final int totalAnnotationCount;
    private final int totalAnnotationSummaryCount;

    private final boolean doNormalization;

    private final float expectedMinimumQualityScore;
    private final float maximumQualityScore;

    private final URI[] sourceRanking;

    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    /**
     * Construct a new annotation summary mapper, supplying the total number of annotations known to ZOOMA.  This mapper
     * uses this value to calculate the score - the annotation summary score is the maximum annotation quality score,
     * normalized by the frequency of use of this pattern.  The minimum quality score ZOOMA will use is 50, with no
     * upper bound (unless the alternative form of the constructor is used)
     *
     * @param totalAnnotationCount        the total number of annotations in zooma
     * @param totalAnnotationSummaryCount the total number of unique annotation summaries in zooma
     * @param sourceRanking               a configurable ranking of sources in preference order, which can be supplied
     *                                    at search time
     */
    public AnnotationSummaryMapper(int totalAnnotationCount, int totalAnnotationSummaryCount, URI... sourceRanking) {
        this(totalAnnotationCount, totalAnnotationSummaryCount, -1, sourceRanking);
    }

    /**
     * Construct a new annotation summary mapper, supplying the total number of annotations known to ZOOMA.  This mapper
     * uses this value to calculate the score - the annotation summary score is the maximum annotation quality score,
     * normalized by the frequency of use of this pattern.  This form of the constructor allows for the supply of a
     * maximum quality score, and in this case scores will be normalized to a maximum score of 100 to allow scores from
     * ZOOMA to be more easily compared.
     *
     * @param totalAnnotationCount        the total number of annotations in zooma
     * @param totalAnnotationSummaryCount the total number of unique annotation summaries in zooma
     * @param sourceRanking               a configurable ranking of sources in preference order, which can be supplied
     *                                    at search time
     */
    public AnnotationSummaryMapper(int totalAnnotationCount,
                                   int totalAnnotationSummaryCount,
                                   float maxQualityScore,
                                   URI... sourceRanking) {
        this.totalAnnotationCount = totalAnnotationCount;
        this.totalAnnotationSummaryCount = totalAnnotationSummaryCount;
        this.doNormalization = !(maxQualityScore == -1);
        this.sourceRanking = sourceRanking;

        if (doNormalization) {
            // calculate theoretical minimum quality score
            Date y2k;
            try {
                y2k = new SimpleDateFormat("YYYY").parse("2000");
            }
            catch (ParseException e) {
                throw new InstantiationError("Could not parse date '2000' (YYYY)");
            }
            float bottomScore = (float) (1.0 + Math.log10(y2k.getTime()));
            int veris = 1;
            float freq = 1.0f;
            float annotationCount = (float) totalAnnotationCount;
            float annotationSummaryCount = (float) totalAnnotationSummaryCount;
            float normalizedFreq = 1.0f + (freq / annotationCount);
            float normalizedRank = 1.0f - (200 / annotationSummaryCount);
            float sourceRank = 1.0f;
            this.expectedMinimumQualityScore = (bottomScore + veris) * normalizedRank * normalizedFreq + sourceRank;
            getLog().debug("Expected minimum quality score = " + expectedMinimumQualityScore);
            this.maximumQualityScore = maxQualityScore;
            getLog().debug("Maximum quality score = " + maximumQualityScore);
        }
        else {
            getLog().debug("Normalization parameters were not defined - " +
                                   "Annotation Summaries will be mapped with raw quality scores");
            this.expectedMinimumQualityScore = -1;
            this.maximumQualityScore = -1;
        }
    }

    @Override
    public AnnotationSummary mapDocument(Document d) {
        return mapDocument(d, 1);
    }

    @Override
    public AnnotationSummary mapDocument(Document d, int rank) {
        getLog().trace("Mapping document '" + d.toString() + "'...");

        // grab single cardinality fields
        String id = d.get("id");
        String propertyType = d.get("propertytype");
        String propertyValue = d.get("property");
        // grab multi-cardinality fields
        String[] deStrs = d.getValues("semanticTag");
        String[] aStrs = d.getValues("annotation");
        // tokenise on spaces
        getLog().trace("Annotation search has " + aStrs.length + " results");
        getLog().trace("Semantic tag search has " + deStrs.length + " results");
        Collection<URI> semanticTags = new HashSet<>();
        Collection<URI> annotations = new HashSet<>();
        for (String s : deStrs) {
            semanticTags.add(URI.create(s));
        }
        for (String s : aStrs) {
            annotations.add(URI.create(s));
        }
        float score = getDocumentQuality(d, rank);

        getLog().trace("\nNext Annotation summary:\n\t" +
                               "property type '" + propertyType + "',\n\t" +
                               "property value '" + propertyValue + "',\n\t" +
                               "semantic tags " + semanticTags + ",\n\t" +
                               "annotation URIs " + annotations + "\n\t" +
                               "Quality Score: " + score);
        return new SimpleAnnotationSummary(id, propertyType, propertyValue, semanticTags, annotations, score);
    }

    @Override
    public float getDocumentQuality(Document d) {
        return getDocumentQuality(d, 1);
    }

    @Override
    public float getDocumentQuality(Document d, int rank) {
        float topScore = Float.parseFloat(d.get("topScore"));
        int veris = Integer.parseInt(d.get("timesVerified"));
        float freq = (float) Integer.parseInt(d.get("frequency"));
        float annotationCount = (float) totalAnnotationCount;
        float annotationSummaryCount = (float) totalAnnotationSummaryCount;
        float normalizedFreq = 1.0f + (annotationCount > 0 ? (freq / annotationCount) : 0);
        float normalizedRank = 1.0f - (annotationSummaryCount > 0 ? (rank / annotationSummaryCount) : 0);
        float sourceRank = (float) Math.sqrt((double) getSourceRanking(URI.create(d.get("source"))));
        if (getLog().isTraceEnabled()) {
            getLog().trace("Document quality: " +
                                   "(" + topScore + " + " + veris + ") x " +
                                   "(1 - " + rank + "/" + annotationSummaryCount + ") x " +
                                   "(1 + " + freq + "/" + annotationCount + ") = " +
                                   (topScore + veris) + " x " + normalizedFreq + " = " +
                                   ((topScore + veris) * normalizedFreq));
        }
        float score = (topScore + veris) * normalizedRank * normalizedFreq + sourceRank;
        if (doNormalization) {
            if ((score - expectedMinimumQualityScore) < 0) {
                return 50;
            }
            else {
                return 50 + (50 * (score - expectedMinimumQualityScore) /
                        (maximumQualityScore - expectedMinimumQualityScore));
            }
        }
        else {
            return score;
        }
    }

    protected int getSourceRanking(URI source) {
        for (int i = 0; i < sourceRanking.length; i++) {
            if (sourceRanking[i].equals(source)) {
                return sourceRanking.length - i + 1;
            }
        }
        return 1;
    }
}
