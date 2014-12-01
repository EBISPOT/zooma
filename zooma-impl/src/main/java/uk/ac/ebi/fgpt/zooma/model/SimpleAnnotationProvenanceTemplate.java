package uk.ac.ebi.fgpt.zooma.model;

import java.util.Date;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 01/12/14
 */
public class SimpleAnnotationProvenanceTemplate implements AnnotationProvenanceTemplate {
    private final AnnotationSource source;
    private final Evidence evidence;
    private final String generator;
    private final Date generatedDate;

    private Accuracy accuracy;
    private String annotator;
    private Date annotationDate;

    private boolean modded = false;
    private final AnnotationProvenance unmodded;

    public SimpleAnnotationProvenanceTemplate(AnnotationSource source,
                                              Evidence evidence,
                                              Accuracy accuracy,
                                              String generator,
                                              Date generatedDate,
                                              String annotator,
                                              Date annotationDate) {
        this.source = source;
        this.evidence = evidence;
        this.accuracy = accuracy;
        this.generator = generator;
        this.generatedDate = generatedDate;
        this.annotator = annotator;
        this.annotationDate = annotationDate;

        this.unmodded = new SimpleAnnotationProvenance(source,
                                                       evidence,
                                                       accuracy,
                                                       generator,
                                                       generatedDate,
                                                       annotator,
                                                       annotationDate);
    }

    public SimpleAnnotationProvenanceTemplate(AnnotationSource source,
                                              Evidence evidence,
                                              String generator,
                                              Date generatedDate) {
        this(source, evidence, null, generator, generatedDate, null, null);
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
        return generatedDate;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SimpleAnnotationProvenanceTemplate that = (SimpleAnnotationProvenanceTemplate) o;

        if (generatedDate != null ? !generatedDate.equals(that.generatedDate) : that.generatedDate != null) {
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
        result = 31 * result + (generatedDate != null ? generatedDate.hashCode() : 0);
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
                ", generatedDate=" + generatedDate +
                ", annotator='" + annotator + '\'' +
                ", annotationDate=" + annotationDate +
                '}';
    }

    @Override
    public AnnotationProvenanceTemplate annotatorIs(String annotator) {
        this.annotator = annotator;
        this.modded = true;
        return this;
    }

    @Override
    public AnnotationProvenanceTemplate annotationDateIs(Date date) {
        this.annotationDate = date;
        this.modded = true;
        return this;
    }

    @Override
    public AnnotationProvenanceTemplate accuracyIs(Accuracy accuracy) {
        this.accuracy = accuracy;
        this.modded = true;
        return this;
    }

    @Override
    public AnnotationProvenance complete() {
        return !modded ? unmodded : new SimpleAnnotationProvenance(source,
                                                                   evidence,
                                                                   accuracy,
                                                                   generator,
                                                                   generatedDate,
                                                                   annotator,
                                                                   annotationDate);
    }
}
