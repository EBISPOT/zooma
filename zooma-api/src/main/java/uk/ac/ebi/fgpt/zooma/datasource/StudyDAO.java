package uk.ac.ebi.fgpt.zooma.datasource;

import uk.ac.ebi.fgpt.zooma.model.Property;
import uk.ac.ebi.fgpt.zooma.model.Study;

import java.net.URI;
import java.util.Collection;

/**
 * A data access object that defines methods to create, retrieve, update and delete studies from a ZOOMA datasource.
 *
 * @author Tony Burdett
 * @date 29/03/12
 */
public interface StudyDAO extends ZoomaDAO<Study> {
    /**
     * Retrieves a collection of studies from a zooma datasource, limited to those which annotate to the ALL of the
     * given entities.  Note that the retrieved studies may annotate to other entities as well as those supplied, but
     * every result returned must annotate to all of the supplied entities.
     * <p/>
     * This method shoudl not use inference by default: this is equivalent to <code>readBySemanticTags(false,
     * semanticTags);</code>.
     *
     * @param semanticTags the URIs of the entities which the required studies annotate to
     * @return the collection of studiess declared to link to the supplied ontology term
     */
    Collection<Study> readBySemanticTags(URI... semanticTags);

    /**
     * Retrieves a collection of studies from a zooma datasource, limited to those which annotate to the ALL of the
     * given entities.  Note that the retrieved studies may annotate to other entities as well as those supplied, but
     * every result returned must annotate to all of the supplied entities.
     * <p/>
     * The additional boolean flag on this method indicates whether inference on the supplied semantic tags should be
     * used.  If true, studies which annotate to all of the semanticTags or any of their inferred subtypes are
     * returned.
     *
     * @param useInference true if the results include those which annotate to any of the supplied semantic tags or any
     *                     of their subtypes
     * @param semanticTags the URIs of the entities which the required studies annotate to
     * @return the collection of studiess declared to link to the supplied ontology term
     */
    Collection<Study> readBySemanticTags(boolean useInference, URI... semanticTags);

    /**
     * Retrieves a collection of studies from a zooma datasource with the supplied accession.  Note that accessions are
     * not guaranteed to be unique, although you should expect that they are unique within a single datasource.
     *
     * @param accession the accession of the study to retrieve
     * @return the studies with this accession
     */
    Collection<Study> readByAccession(String accession);

    /**
     * Retrives a collection of studies by properties.
     * @param property
     * @return
     */
    Collection<Study> readByProperty(Property... property);
}
