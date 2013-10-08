package uk.ac.ebi.fgpt.zooma.model;

import uk.ac.ebi.fgpt.zooma.Namespaces;

import java.io.Serializable;
import java.net.URI;
import java.util.Date;

/**
 * A general representation of the data provenance that can be attached to an annotation.
 * <p/>
 * Every annotation must include some provenance about the process that lead to the annotation being asserted.
 * Generally, this will probably include some simple information that can be used to identify where this annotation is
 * asserted and some history associated with it - who made the link, when it was made, and so on,
 * <p/>
 * Implementations are free to define the additional provenance they may require
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
     * Returns a string describing the agent that generated the annotation object. For all annotations created
     * as part of this distribution the agent is ZOOMA
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
     * @return the annotation creation date
     */
    Date getAnnotationDate();


    /**
     * The evidence for the existance of a particular annotation.  This described a little about how this annotation was
     * derived - whether it was manually asserted, inferred to exist from some other source, and so on.
     */
    public enum Evidence {

        /**
         *  A type of curator inference that is used in a manual assertion.
         */
        MANUAL_CURATED("http://purl.obolibrary.org/obo/ECO_0000305"),

        /**
         * An annotation inferred by ZOOMA from previous curated entry. A type of Automatic curation
         */
        ZOOMA_INFERRED_FROM_CURATED(Namespaces.ZOOMA_TERMS.getURI() + "ZOOMA_0000101"),

        /**
         * An assertion method that does not involve human review.
         */
        AUTOMATIC ("http://purl.obolibrary.org/obo/ECO_0000203"), // http://purl.obolibrary.org/obo/ECO_0000203

        /**
         * An evidence code that states the existence of an annotation was computed based on a match to a semantic tag.
         * Use this when a property value exactly matches a class label or synonym from an ontology
         */
        COMPUTED_FROM_ONTOLOGY (Namespaces.ZOOMA_TERMS.getURI() + "ZOOMA_0000102"),
        /**
         * An evidence code that states the existence of an annotation was computed based on a text match to a previous
         * annotation that has not been curated.  This is a computed match based on a match that is (at best) inferred
         * to exist, and hence has at least two degrees of separation from a curated match. This is therefore assigned a
         * low confidence.
         */
        COMPUTED_FROM_TEXT_MATCH (Namespaces.ZOOMA_TERMS.getURI() + "ZOOMA_0000103"),
        /**
         * An evidence code that states the existence of an annotation was provided by a submitter, usually within the
         * scope of a single Study or Biological Entity.  This annotation has never subsequently been confirmed or
         * curated and may not represent a good annotation with respect to other available annotations.
         */
        SUBMITTER_PROVIDED (Namespaces.ZOOMA_TERMS.getURI() + "ZOOMA_0000104"),
        /**
         * An evidence code that states this annotation was supported by evidence at some point, but it is unknown what
         * this evidence was, or that the evidence for this annotation has been lost.
         */
        NON_TRACEABLE (Namespaces.ZOOMA_TERMS.getURI() + "ZOOMA_0000105"),
        /**
         * An evidence code that states this annotation has no evidence to support it.
         */
        NO_EVIDENCE (Namespaces.ZOOMA_TERMS.getURI() + "ZOOMA_0000106"),

        /**
         * UNKNOWN
         */
        UNKNOWN(Namespaces.ZOOMA_TERMS.getURI() + "ZOOMA_0000107");

        private String id;
        Evidence (String id) {
            this.id = id;
        }

        public static Evidence lookup (String id) {
            for (Evidence e : Evidence.values()) {
                if (e.id.equals(id)) {
                    return e;
                }
            }
            return Evidence.UNKNOWN;
        }

        public String getId() {
            return id;
        }

    }
}
