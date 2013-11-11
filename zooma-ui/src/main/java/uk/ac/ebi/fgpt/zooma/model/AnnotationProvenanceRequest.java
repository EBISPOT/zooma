package uk.ac.ebi.fgpt.zooma.model;

import java.net.URI;
import java.util.Date;

/**
 * An implementation of AnnotationProvenance designed to be used by jackson to deserialize annotation provenance
 * requests.  You should NOT use this implementation in code; objects are designed to be transient and in order to
 * handle serialization demands are also mutable.  If you want to code with annotation provenance objects, using {@link
 * uk.ac.ebi.fgpt.zooma.model.SimpleAnnotationProvenance} is advisable.
 *
 * @author Tony Burdett
 * @date 06/08/13
 */
public class AnnotationProvenanceRequest implements AnnotationProvenance {
    private static final long serialVersionUID = 4400943014934414023L;

    private AnnotationSource source;
    private Evidence evidence;
    private Accuracy accuracy;
    private String generator;
    private Date generatedDate;
    private String annotator;
    private Date annotationDate;

    public AnnotationSource getSource() {
        return source;
    }

    public void setSource(AnnotationSource source) {
        this.source = source;
    }

    public Evidence getEvidence() {
        return evidence;
    }

    public void setEvidence(Evidence evidence) {
        this.evidence = evidence;
    }

    public String getGenerator() {
        return generator;
    }

    public void setGenerator(String generator) {
        this.generator = generator;
    }

    public Date getGeneratedDate() {
        return generatedDate;
    }

    public void setGeneratedDate(Date generatedDate) {
        this.generatedDate = generatedDate;
    }

    public String getAnnotator() {
        return annotator;
    }

    public void setAnnotator(String annotator) {
        this.annotator = annotator;
    }

    public Date getAnnotationDate() {
        return annotationDate;
    }

    public void setAnnotationDate(Date annotationDate) {
        this.annotationDate = annotationDate;
    }

    public Accuracy getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(Accuracy accuracy) {
        this.accuracy = accuracy;
    }
    @Override
    public String toString() {
        return "AnnotationProvenanceRequest {" +
                "source=" + source +
                ", evidence=" + evidence +
                ", accuracy=" + accuracy +
                ", generator='" + generator + '\'' +
                ", generationDate=" + generatedDate +
                ", annotator='" + annotator + '\'' +
                ", annotationDate=" + annotationDate +
                '}';
    }

    @Override
    public URI getURI() {
        return null;
    }
}
