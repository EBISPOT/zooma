package uk.ac.ebi.spot.zooma.model;

import lombok.Value;

import java.util.Date;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 05/01/17
 */
@Value
public class Provenance {
    Datasource datasource;
    Evidence evidence;
    Accuracy accuracy;
    Annotator annotator;
    AnnotationDate annotationDate;

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
    }

    enum Accuracy {

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
    }

    @Value
    class Annotator {
        String name;
    }

    @Value
    class AnnotationDate {
        Date date;
    }
}
