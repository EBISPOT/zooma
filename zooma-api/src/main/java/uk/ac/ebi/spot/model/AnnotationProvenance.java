package uk.ac.ebi.spot.model;

import java.io.Serializable;
import java.util.Date;

/**
 * A general representation of the data provenance that can be attached to an annotation.
 * <p/>
 * Every annotation must include some provenance about the process that lead to the annotation being asserted.
 * Generally, this will probably include some simple information that can be used to identify where this annotation is
 * asserted and some history associated with it - who made the link, when it was made, and so on,
 *
 * @author Tony Burdett
 * @author Simon Jupp
 * @date 13/03/12
 */
public interface AnnotationProvenance extends Serializable {
    /**
     * Returns the annotation source that contains the annotation with this provenance.
     *
     * @return the datasource containing an annotation with this provenance
     */
    AnnotationSource getSource();

    /**
     * Returns a brief description of the evidence attached to the annotation with this provenance.
     *
     * @return the evidence for this annotation
     */
    Evidence getEvidence();

    /**
     * Returns a code for the accuracy of the annotation
     *
     * @return the accuracy for this annotation
     */
    Accuracy getAccuracy();

    /**
     * Returns a string describing the agent that generated the annotation object. For all annotations created as part
     * of this distribution the agent is ZOOMA
     *
     * @return the generator name
     */
    String getGenerator();

    /**
     * Returns the date on which the annotation was generated in the ZOOMA knowledgebase
     *
     * @return the generation date
     */
    Date getGeneratedDate();

    /**
     * Returns a string describing the agent who generated the annotation. This is usually a person name
     *
     * @return the annotator name
     */
    String getAnnotator();

    /**
     * Returns the date on which the annotation was generated in the source database
     *
     * @return the annotation creation date, may return null for unknown dates.
     */
    Date getAnnotationDate();

    /**
     * The evidence for the existance of a particular annotation.  This described a little about how this annotation was
     * derived - whether it was manually asserted, inferred to exist from some other source, and so on.
     */
    enum Evidence {
        /**
         * A type of curator inference that is used in a manual assertion.
         */
        MANUAL_CURATED,
        /**
         * An annotation inferred by ZOOMA from previous curated entry. A type of Automatic curation
         */
        ZOOMA_INFERRED_FROM_CURATED,
        /**
         * An evidence code that states the existence of an annotation was computed based on a match to a semantic tag.
         * Use this when a property value exactly matches a class label or synonym from an ontology
         */
        COMPUTED_FROM_ONTOLOGY,
        /**
         * An evidence code that states the existence of an annotation was computed based on a text match to a property
         * in a previous annotation.  Use this whenever an annotation is predicted based on an annotation that has not
         * been manually curated
         */
        COMPUTED_FROM_TEXT_MATCH,
        /**
         * An evidence code that states the existence of an annotation was provided by a submitter, usually within the
         * scope of a single Study or Biological Entity.  This annotation has never subsequently been confirmed or
         * curated and may not represent a good annotation with respect to other available annotations.
         */
        SUBMITTER_PROVIDED,
        /**
         * An evidence code that states this annotation was supported by evidence at some point, but it is unknown what
         * this evidence was, or that the evidence for this annotation has been lost.
         */
        NON_TRACEABLE,
        /**
         * An evidence code that states this annotation has no evidence to support it.
         */
        NO_EVIDENCE,
        /**
         * UNKNOWN
         */
        UNKNOWN;

        public static Evidence lookup(String id) {
            for (Evidence e : Evidence.values()) {
                if (e.name().equals(id)) {
                    return e;
                }
            }
            return Evidence.UNKNOWN;
        }
    }

    /**
     * A measure of the accuracy of an annotation
     */
    public enum Accuracy {

        /**
         * The Zooma annotation of ontology term to property value only boradly related suggeting that a more granular
         * ontology term is needed
         */
        BROAD,

        /**
         * The Zooma annotation of ontology term to property value lacks precision
         */
        IMPRECISE,

        /**
         * The Zooma annotation only holds for part of the property value being annotated by these ontology terms
         */
        PARTIAL,

        /**
         * The Zooma annotation of ontology term to property value is a good match
         */
        PRECISE,

        NOT_SPECIFIED;

        public static Accuracy lookup(String id) {
            for (Accuracy e : Accuracy.values()) {
                if (e.name().equals(id)) {
                    return e;
                }
            }
            return Accuracy.NOT_SPECIFIED;
        }
    }
}
