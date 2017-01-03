package uk.ac.ebi.spot.config;

import uk.ac.ebi.spot.exception.TemplateBuildingException;
import uk.ac.ebi.spot.model.AnnotationProvenance;
import uk.ac.ebi.spot.model.AnnotationProvenanceTemplate;
import uk.ac.ebi.spot.model.AnnotationSource;
import uk.ac.ebi.spot.model.MongoAnnotationProvenance;

import java.util.Date;

/**
 * A builder for an {@link uk.ac.ebi.spot.model.AnnotationProvenance} instance.
 * Uses a {@link uk.ac.ebi.spot.model.AnnotationProvenanceTemplate} to populate the annotation source, generator and date
 *
 * @author Tony Burdett
 * @date 17/08/15
 */
public abstract class AnnotationProvenanceBuilder {
    public static AnnotationProvenanceTemplate createTemplate() {
        return createTemplate("ZOOMA");
    }

    public static AnnotationProvenanceTemplate createTemplate(String generator) {
        return createTemplate(generator, new Date());
    }

    public static AnnotationProvenanceTemplate createTemplate(String generator, Date generatedDate) {
        return createTemplate(null, generator, generatedDate);
    }

    public static AnnotationProvenanceTemplate createTemplate(AnnotationSource annotationSource, String generator) {
        return createTemplate(annotationSource, generator, new Date());
    }

    public static AnnotationProvenanceTemplate createTemplate(AnnotationSource annotationSource, String generator, Date generatedDate) {
        return new SimpleAnnotationProvenanceTemplate(annotationSource, generator, generatedDate);
    }

    private static class SimpleAnnotationProvenanceTemplate implements AnnotationProvenanceTemplate {
        private final String generator;
        private final Date generatedDate;

        private AnnotationSource source;
        private AnnotationProvenance.Evidence evidence;
        private AnnotationProvenance.Accuracy accuracy;
        private String annotator;
        private Date annotationDate;

        public SimpleAnnotationProvenanceTemplate(AnnotationSource annotationSource,
                                                  String generator,
                                                  Date generatedDate) {
            this.source = annotationSource;
            this.generator = generator;
            this.generatedDate = generatedDate;
            this.annotator = "ZOOMA";
            this.annotationDate = new Date();
        }

        public AnnotationSource getSource() {
            return source;
        }

        public AnnotationProvenance.Evidence getEvidence() {
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

        public AnnotationProvenance.Accuracy getAccuracy() {
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
            return "MongoAnnotationProvenance{" +
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
        public AnnotationProvenanceTemplate sourceIs(AnnotationSource source) {
            this.source = source;
            return this;
        }

        @Override
        public AnnotationProvenanceTemplate evidenceIs(AnnotationProvenance.Evidence evidence) {
            this.evidence = evidence;
            return this;
        }

        @Override
        public AnnotationProvenanceTemplate annotatorIs(String annotator) {
            this.annotator = annotator;
            return this;
        }

        @Override
        public AnnotationProvenanceTemplate annotationDateIs(Date date) {
            this.annotationDate = date;
            return this;
        }

        @Override
        public AnnotationProvenanceTemplate accuracyIs(AnnotationProvenance.Accuracy accuracy) {
            this.accuracy = accuracy;
            return this;
        }

        @Override
        public AnnotationProvenance build() {
            if (source == null) {
                throw new TemplateBuildingException("Cannot create annotation provenance without an Annotation Source");
            }
            if (evidence == null) {
                throw new TemplateBuildingException("Evidence is required to create an annotation provenance");
            }
            return new MongoAnnotationProvenance(source,
                                                  evidence,
                                                  accuracy,
                                                  generator,
                                                  generatedDate,
                                                  annotator,
                                                  annotationDate);
        }
    }
}
