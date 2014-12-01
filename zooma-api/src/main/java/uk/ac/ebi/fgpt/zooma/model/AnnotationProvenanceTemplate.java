package uk.ac.ebi.fgpt.zooma.model;

import java.util.Date;

/**
 * A templating interface for {@link uk.ac.ebi.fgpt.zooma.model.AnnotationProvenance} objects.  This interface declares
 * the {@link #complete()} method, which allows implementations of this template to generate and return completed
 * annotation provenances having set up an initial set of variables.  This method also declares methods to update or
 * override a series of pre-initialized values on the template.
 *
 * @author Tony Burdett
 * @date 27/11/14
 */
public interface AnnotationProvenanceTemplate extends AnnotationProvenance {
    /**
     * Updates the templates current value for the annotator of the resulting provenance object
     *
     * @param annotator the annotator
     * @return a reference to this template, for chaining calls
     */
    AnnotationProvenanceTemplate annotatorIs(String annotator);

    /**
     * Updates the templates current value for the annotation date of the resulting provenance object
     *
     * @param date the annotation date
     * @return a reference to this template, for chaining calls
     */
    AnnotationProvenanceTemplate annotationDateIs(Date date);

    /**
     * Updates the templates current value for the accuracy of the resulting provenance object
     *
     * @param accuracy the annotation accuracy
     * @return a reference to this template, for chaining calls
     */
    AnnotationProvenanceTemplate accuracyIs(Accuracy accuracy);

    /**
     * Returns a completed, "concrete" instance of an {@link uk.ac.ebi.fgpt.zooma.model.AnnotationProvenance} object,
     * generated from this template
     *
     * @return a completed instance of an annotation provenance object, generated from this template
     */
    AnnotationProvenance complete();
}
