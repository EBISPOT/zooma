package uk.ac.ebi.spot.repositories.custom;

import uk.ac.ebi.spot.model.Property;
import uk.ac.ebi.spot.model.Study;

import java.net.URI;
import java.util.List;

/**
 * This custom interface of the {@link uk.ac.ebi.spot.repositories.StudyRepository} gives the opportunity for other queries, other than the
 * standard ones provided by the {@link org.springframework.data.mongodb.repository.MongoRepository}, to be constructed. An implementation of this interface
 * can be defined as long as it is named "StudyRepositoryImpl" and as long as the {@link uk.ac.ebi.spot.repositories.StudyRepository}
 * extends this interface.
 *
 *
 * Created by olgavrou on 14/09/2016.
 */
public interface CustomStudyRepository {

    /**
     * Retrieves all distinct studies from a zooma datasource.
     *
     * @return a list of all distinct studies
     */
    List<Study> findDistinctStudies();

    /**
     * Retrieves a list of studies from a zooma datasource, limited to those which annotate to the ALL of the
     * given entities.  Note that the retrieved studies may annotate to other entities as well as those supplied, but
     * every result returned must annotate to all of the supplied entities.
     *
     * @param semanticTags the URIs of the entities which the required studies annotate to
     * @return the list of studies declared to link to the supplied ontology term
     */
    List<Study> findBySemanticTags(URI... semanticTags);

    /**
     * Retrieves a list of studies from a zooma datasource with the supplied accession.  Note that accessions are
     * not guaranteed to be unique, although you should expect that they are unique within a single datasource.
     *
     * @param accession the accession of the study to retrieve
     * @return the studies with this accession
     */
    List<Study> findByAccession(String accession);

    /**
     * Retrives a list of studies by properties.
     * @param properties
     * @return the list of studies that are linked with the supplied properties
     */
    List<Study> findByProperty(Property... properties);
}
