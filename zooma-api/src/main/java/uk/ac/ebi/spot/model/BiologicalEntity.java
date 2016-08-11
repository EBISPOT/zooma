package uk.ac.ebi.spot.model;

import java.util.Collection;

/**
 * Represents any type of biological entity (which may be a biological sample, an assay, or something else) which can be
 * named and identified in the context of a single study.
 *
 * @author Tony Burdett
 * @date 13/03/12
 */
public interface BiologicalEntity {
    /**
     * Returns the name that was assigned to this biological entity to identify it within the context of the study in
     * which it was described.
     *
     * @return the name of this biological entity
     */
    String getName();

    /**
     * Returns the collection of studies that contain a reference to this biological entity
     *
     * @return the study that references this sample/assay/etc
     */
    Collection<Study> getStudies();
}

