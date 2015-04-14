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
import java.util.Set;

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

    /**
     * Returns a clone of this mapper, but using the given source rankings instead of the originals
     *
     * @param sourceRanking the new set of source rankings to use
     * @return an annotation summary mapper identical to this one, but with new ranking orders
     */
    public AnnotationSummaryMapper withRankings(URI... sourceRanking) {
        return new AnnotationSummaryMapper(this.totalAnnotationCount,
                                           this.totalAnnotationSummaryCount,
                                           this.maximumQualityScore,
                                           sourceRanking);
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
        URI propertyUri = d.get("propertyuri") != null ? URI.create(d.get("propertyuri")) : null;
        String propertyType = d.get("propertytype");
        String propertyValue = d.get("property");
        // grab multi-cardinality fields
        String[] deStrs = d.getValues("semanticTag");
        String[] aStrs = d.getValues("annotation");
        String[] sourceStrs = d.getValues("source");
        // tokenise on spaces
        getLog().trace("Annotation search has " + aStrs.length + " results");
        getLog().trace("Semantic tag search has " + deStrs.length + " results");
        getLog().trace("Annotation search has " + sourceStrs.length + " sources");
        Collection<URI> semanticTags = new HashSet<>();
        Collection<URI> annotations = new HashSet<>();
        Collection<URI> annotationSourceURIs = new HashSet<>();
        for (String s : deStrs) {
            semanticTags.add(URI.create(s));
        }
        for (String s : aStrs) {
            annotations.add(URI.create(s));
        }
        for (String s : sourceStrs) {
            annotationSourceURIs.add(URI.create(s));
        }
        float score = getDocumentQuality(d, rank);

        getLog().trace("\nNext Annotation summary:\n\t" +
                               "property uri '" + propertyUri + "',\n\t" +
                               "property type '" + propertyType + "',\n\t" +
                               "property value '" + propertyValue + "',\n\t" +
                               "semantic tags " + semanticTags + ",\n\t" +
                               "annotation URIs " + annotations + "\n\t" +
                               "annotation source URIs " + annotationSourceURIs + "\n\t" +
                               "Quality Score: " + score);
        return new SimpleAnnotationSummary(id,
                                           propertyUri,
                                           propertyType,
                                           propertyValue,
                                           semanticTags,
                                           annotations,
                                           score,
                                           annotationSourceURIs);
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
        Set<URI> sources = new HashSet<>();
        for (String sourceString : d.getValues("source")) {
            sources.add(URI.create(sourceString));
        }

        float annotationCount = (float) totalAnnotationCount;
        float annotationSummaryCount = (float) totalAnnotationSummaryCount;
        float sourceRank = 0.05f * getSourceRanking(sources);

        float normalizedFreq = 1.0f + (annotationCount > 0 ? (freq / annotationCount) : 0);
        float normalizedAnnotationRank = 1.0f - (annotationSummaryCount > 0 ? (rank / annotationSummaryCount) : 0);
        float normalizedSourceRank = 1.0f + sourceRank;

        float score = (topScore + veris) * normalizedAnnotationRank * normalizedFreq * normalizedSourceRank;

        if (getLog().isTraceEnabled()) {
            getLog().trace("Document quality: " +
                                   "(" + topScore + " + " + veris + ") x " +
                                   "(1 - " + rank + "/" + annotationSummaryCount + ") x " +
                                   "(1 + " + freq + "/" + annotationCount + ") x " +
                                   "(1 + " + sourceRank + ") = " +
                                   (topScore + veris) + " x " +
                                   normalizedAnnotationRank + " x " +
                                   normalizedFreq + " x " +
                                   normalizedSourceRank + " = " +
                                   score);
        }
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

    /**
     * Evaluates a ranking for a set of sources.  If one of the supplied sources is set in the source rankings for this
     * mapper, the highest scoring source is used.  The score is a boost value between 0 and 1; a score of 0 is
     * returned for an unranked source, or in cases where there are no rankings defined, and 1 is the biggest possible
     * boost for a source that has been supplied in the source rankings.
     *
     * @param sources the list of sources to rank
     * @return a boost score; 0 if none of the sources is ranked, 1 for best ranked match
     */
    protected float getSourceRanking(Set<URI> sources) {
        if (sourceRanking.length == 0) {
            return 0;
        }
        else {
            for (int i = 0; i < sourceRanking.length; i++) {
                if (sources.contains(sourceRanking[i])) {
                    if (sourceRanking.length == 1) {
                        return 1f;
                    }
                    else {
                        return (((float) (sourceRanking.length - i)) / (sourceRanking.length));
                    }
                }
            }
            return 0;
        }
    }
}
