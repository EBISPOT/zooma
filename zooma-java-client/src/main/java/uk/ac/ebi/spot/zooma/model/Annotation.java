package uk.ac.ebi.spot.zooma.model;

import lombok.Data;

import java.util.Collection;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 05/01/17
 */
@Data
public class Annotation {
    private final Property property;
    private final Collection<SemanticTag> semanticTags;
    private final BiologicalEntity biologicalEntity;
    private final Provenance provenance;

    private Collection<Annotation> replacedAnnotations;
    private Collection<Annotation> replacementAnnotations;

}
