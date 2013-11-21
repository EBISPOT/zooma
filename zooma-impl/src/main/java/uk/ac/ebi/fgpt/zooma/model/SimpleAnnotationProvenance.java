package uk.ac.ebi.fgpt.zooma.model;

import java.util.Date;

/**
 * A basic implementation of an Annotation Provenance object, declaring the origin of an annotation
 *
 * @author Tony Burdett
 * @author Simon Jupp
 * @date 10/04/12
 */
public class SimpleAnnotationProvenance implements AnnotationProvenance {
    private AnnotationSource source;
    private Evidence evidence;
    private Accuracy accuracy;
    private String generator;
    private Date generationDate;
    private String annotator;
    private Date annotationDate;

    public SimpleAnnotationProvenance(AnnotationSource source,
                                      Evidence evidence,
                                      Accuracy accuracy,
                                      String generator,
                                      Date generationDate,
                                      String annotator,
                                      Date annotationDate) {
        this.source = source;
        this.evidence = evidence;
        this.accuracy = accuracy;
        this.generator = generator;
        this.generationDate = generationDate;
        this.annotator = annotator;
        this.annotationDate = annotationDate;
    }

    public SimpleAnnotationProvenance(AnnotationSource source,
                                      Evidence evidence,
                                      String generator,
                                      Date generationDate) {
        this.source = source;
        this.evidence = evidence;
        this.generator = generator;
        this.generationDate = generationDate;
    }

    public AnnotationSource getSource() {
        return source;
    }

    public Evidence getEvidence() {
        return evidence;
    }

    public String getGenerator() {
        return generator;
    }

    public Date getGeneratedDate() {
        return generationDate;
    }

    public String getAnnotator() {
        return annotator;
    }

    public Date getAnnotationDate() {
        return annotationDate;
    }

    public Accuracy getAccuracy() {
        return accuracy;
    }

    public Date getGenerationDate() {
        return generationDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SimpleAnnotationProvenance that = (SimpleAnnotationProvenance) o;

        if (generationDate != null ? !generationDate.equals(that.generationDate) : that.generationDate != null) {
            return false;
        }
        if (generator != null ? !generator.equals(that.generator) : that.generator != null) {
            return false;
        }
        if (annotationDate != null ? !annotationDate.equals(that.annotationDate) : that.annotationDate != null) {
            return false;
        }
        if (annotator != null ? !annotator.equals(that.annotator) : that.annotator != null) {
            return false;
        }
        if (evidence != that.evidence) {
            return false;
        }
        if (source != null ? !source.equals(that.source) : that.source != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = source != null ? source.hashCode() : 0;
        result = 31 * result + (evidence != null ? evidence.hashCode() : 0);
        result = 31 * result + (generator != null ? generator.hashCode() : 0);
        result = 31 * result + (generationDate != null ? generationDate.hashCode() : 0);
        result = 31 * result + (annotator != null ? annotator.hashCode() : 0);
        result = 31 * result + (annotationDate != null ? annotationDate.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SimpleAnnotationProvenance{" +
                "source=" + source +
                ", evidence=" + evidence +
                ", accuracy=" + accuracy +
                ", generator='" + generator + '\'' +
                ", generationDate=" + generationDate +
                ", annotator='" + annotator + '\'' +
                ", annotationDate=" + annotationDate +
                '}';
    }
}
