package uk.ac.ebi.spot.zooma.model;

import java.util.Collection;
import java.util.Optional;

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
     * An accession attributed to this biological entity, which should be unique within the context of the datasource it
     * was derived from. This accession may or may not be globally unique. A biosamples database accession is one
     * example of the sort of value stored here. This may be null for resources that do not explicitly accession
     * biological entities.
     *
     * @return the resource-specific accession of this study
     */
    Optional<String> getAccession();

    /**
     * Returns the collection of studies that contain a reference to this biological entity
     *
     * @return the study that references this sample/assay/etc
     */
    Collection<Study> getStudies();
}

