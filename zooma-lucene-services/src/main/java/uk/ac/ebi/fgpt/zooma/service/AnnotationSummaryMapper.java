package uk.ac.ebi.fgpt.zooma.service;

import org.apache.lucene.document.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.zooma.model.AnnotationSummary;
import uk.ac.ebi.fgpt.zooma.model.SimpleAnnotationSummary;

import java.net.URI;
import java.util.Collection;
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

    private float minQualityScore;
    private float maxQualityScore;

    private final URI[] sourceRanking;

    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    /**
     * Construct a new annotation summary mapper, supplying the total number of annotations known to ZOOMA.  This mapper
     * uses this value to calculate the score - the annotation summary score is the maximum annotation quality score,
     * normalized by the frequency of use of this pattern.
     *
     * @param totalAnnotationCount the total number of annotations in zooma
     */
    public AnnotationSummaryMapper(int totalAnnotationCount, int totalAnnotationSummaryCount) {
        this.totalAnnotationCount = totalAnnotationCount;
        this.totalAnnotationSummaryCount = totalAnnotationSummaryCount;
        this.sourceRanking = new URI[0];
    }

    public AnnotationSummaryMapper(int totalAnnotationCount, int totalAnnotationSummaryCount, URI... sourceRanking) {
        this.totalAnnotationCount = totalAnnotationCount;
        this.totalAnnotationSummaryCount = totalAnnotationSummaryCount;
        this.sourceRanking = sourceRanking;
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
        float normalizationFactor;
        if (minQualityScore == -1 && maxQualityScore == -1) {
            getLog().warn("Annotation Summary quality upper and lower bounds not set: " +
                                  "summaries will be mapped with unnormalized scores");
            normalizationFactor = 1;
        }
        else {
            normalizationFactor = (maxQualityScore - minQualityScore) / 50;
        }

        float topScore = Float.parseFloat(d.get("topScore"));
        int veris = Integer.parseInt(d.get("timesVerified"));
        float freq = (float) Integer.parseInt(d.get("frequency"));
        float annotationCount = (float) totalAnnotationCount;
        float annotationSummaryCount = (float) totalAnnotationSummaryCount;
        float normalizedFreq = 1.0f + (freq / annotationCount);
        float normalizedRank = 1.0f - (rank / annotationSummaryCount);
        float sourceRank = (float) Math.sqrt((double) getSourceRanking(URI.create(d.get("source"))));
        getLog().trace("Document quality: " +
                               "(" + topScore + " + " + veris + ") x (1 + " + freq + "/" + annotationCount + ") = " +
                               (topScore + veris) + " x " + normalizedFreq + " = " +
                               ((topScore + veris) * normalizedFreq));
        float score = (topScore + veris) * normalizedRank * normalizedFreq + sourceRank;
        if (minQualityScore != -1) {
            return 50 + ((score - minQualityScore) * normalizationFactor);
        }
        else {
            return score;
        }
    }

    protected int getSourceRanking(URI source) {
        for (int i = 0; i<sourceRanking.length; i++) {
            if (sourceRanking[i].equals(source)) {
                return sourceRanking.length - i + 1;
            }
        }
        return 1;
    }
}
