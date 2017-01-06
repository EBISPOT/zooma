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
public class BiologicalEntity {
    private final Accession accession;
    private Collection<Study> studies;
}
