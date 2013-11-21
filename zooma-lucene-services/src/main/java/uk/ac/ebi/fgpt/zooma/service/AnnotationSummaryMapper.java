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
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 10/07/13
 */
public class AnnotationSummaryMapper implements LuceneDocumentMapper<AnnotationSummary> {
    private int totalAnnotationCount;

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
    public AnnotationSummaryMapper(int totalAnnotationCount) {
        this.totalAnnotationCount = totalAnnotationCount;
    }

    @Override public AnnotationSummary mapDocument(Document d) {
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
        float score = getDocumentQuality(d);

        getLog().trace("\nNext Annotation summary:\n\t" +
                               "property type '" + propertyType + "',\n\t" +
                               "property value '" + propertyValue + "',\n\t" +
                               "semantic tags " + semanticTags + ",\n\t" +
                               "annotation URIs " + annotations + "\n\t" +
                               "Quality Score: " + score);
        return new SimpleAnnotationSummary(id, propertyType, propertyValue, semanticTags, annotations, score);
    }

    @Override public float getDocumentQuality(Document d) {
        float topScore = Float.parseFloat(d.get("topScore"));
        int veris = Integer.parseInt(d.get("timesVerified"));
        float freq = (float) Integer.parseInt(d.get("frequency"));
        float count = (float) totalAnnotationCount;
        float normalizedFreq = 1.0f + (freq / count);
        getLog().trace("Document quality: " +
                               "(" + topScore + " + " + veris + ") x (1 + " + freq + "/" + count + ") = " +
                               (topScore + veris) + " x " + normalizedFreq + " = " +
                               ((topScore + veris) * normalizedFreq));
        return (topScore + veris) * normalizedFreq;
    }
}
