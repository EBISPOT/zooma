package uk.ac.ebi.spot.zooma.model;

/**
 * Represents a general biological study.  In ZOOMA, a study is defined as anything that is uniquely identifiable and
 * contains a series of biological entities.  Implicitly, some experimental examination will have been carried out on
 * these biological entities, but ZOOMA does not attempt to model this information.
 * <p/>
 * Studies may also have additional metadata associated with them that can be curated.  Implementations are free to
 * define this set of metadata.
 *
 * @author Tony Burdett
 * @date 13/03/12
 */
public interface Study {
    /**
     * An accession attributed to this study, usually unique within the context of the datasource it was derived from.
     * This accession may not be globally unique (for instance, in the case of studies present in several datasources
     * but with differing annotations in each).
     *
     * @return the resource-specific accession of this study
     */
    String getAccession();
}
