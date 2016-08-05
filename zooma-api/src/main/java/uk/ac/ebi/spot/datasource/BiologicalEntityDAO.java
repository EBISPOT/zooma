package uk.ac.ebi.spot.datasource;


import uk.ac.ebi.spot.model.BiologicalEntity;
import uk.ac.ebi.spot.model.Study;

import java.net.URI;
import java.util.Collection;

/**
 * A data access object that defines methods to create, retrieve, update and delete biological entities from a ZOOMA
 * datasource.
 *
 * @author Tony Burdett
 * @date 29/03/12
 */
public interface BiologicalEntityDAO extends ZoomaDAO<BiologicalEntity> {
    /**
     * Retrieves a collection of biological entities from a zooma datasource, limited to those which annotate to the ALL
     * of the given entities.  Note that the retrieved biological entities may annotate to other entities as well as
     * those supplied, but every result returned must annotate to all of the supplied entities.
     * <p/>
     * This method should not use inference by default: this is equivalent to <code>readBySemanticTags(false,
     * semanticTags);</code>.
     *
     * @param semanticTags the URIs of the entities which the required studies annotate to
     * @return the collection of biological entities declared to link to the supplied ontology term
     */
    Collection<BiologicalEntity> readBySemanticTags(URI... semanticTags);

    /**
     * Retrieves a collection of biological entities from a zooma datasource, limited to those which annotate to the ALL
     * of the given entities.  Note that the retrieved biological entities may annotate to other entities as well as
     * those supplied, but every result returned must annotate to all of the supplied entities.
     * <p/>
     * The additional boolean flag on this method indicates whether inference on the supplied semantic tags should be
     * used.  If true, biological entities which annotate to all of the semantic tags or any of their inferred subtypes
     * are returned.
     *
     * @param useInference true if the results include those which annotate to any of the supplied semantic tags or any
     *                     of their subtypes
     * @param semanticTags the URIs of the entities which the required studies annotate to
     * @return the collection of biological entities declared to link to the supplied ontology term
     */
    Collection<BiologicalEntity> readBySemanticTags(boolean useInference, URI... semanticTags);

    /**
     * Retrieves a collection of biological entities from a zooma datasource, limited to those which are part of the
     * given study.
     *
     * @param study the study that results should be part of
     * @return a collection of biological entities that are part of the supplied study
     */
    Collection<BiologicalEntity> readByStudy(Study study);

    /**
     * Retrieves a collection of biological entities from a zooma datasource, limited to those which are part of the
     * given study and have the given name.
     *
     * @param study         the study accession
     * @param bioentityName the name of the biological entity to obtain
     * @return the collection of biological entities that satisfy the query
     */
    Collection<BiologicalEntity> readByStudyAndName(Study study, String bioentityName);

}
