package uk.ac.ebi.spot.service;


import uk.ac.ebi.spot.model.Property;
import uk.ac.ebi.spot.model.Study;

import java.net.URI;
import java.util.Collection;

/**
 * A ZOOMA service that allows searching over the set of {@link Study}s known to ZOOMA.  The search facililty on this
 * class allows you to search for studies which annotate to a set of semantic tags.
 *
 * @author Tony Burdett
 * @date 12/06/12
 */
public interface StudySearchService {
    /**
     * Search the set of studies in ZOOMA for those with annotations that closely match the set of supplied semantic
     * tags.
     *
     * @param semanticTagShortnames the set of semantic tags that all results should annotate to
     * @return a collection of studies that annotate to all of the supplied entities
     */
    Collection<Study> searchBySemanticTags(String... semanticTagShortnames);

    /**
     * Search the set of studies in ZOOMA for those with annotations that closely match the set of supplied semantic
     * tags.
     *
     * @param semanticTags the set of semantic tags that all results should annotate to
     * @return a collection of studies that annotate to all of the supplied entities
     */
    Collection<Study> searchBySemanticTags(URI... semanticTags);

    /**
     * Search the set of studies in ZOOMA for those with annotations that closely match the set of supplied semantic
     * tags.
     * <p/>
     * This form of the method has a flag to specify whether or not inference should be used on the set of semantic
     * tags: if true, results will annotate to all of the supplied semantic tags or any of their subtypes
     *
     * @param useInference          whether or not to use inference on the semantic tags
     * @param semanticTagShortnames the set of semantic tags that all results should annotate to
     * @return a collection of studies that annotate to all of the supplied entities
     */
    Collection<Study> searchBySemanticTags(boolean useInference, String... semanticTagShortnames);

    /**
     * Search the set of studies in ZOOMA for those with annotations that closely match the set of supplied semantic
     * tags.
     * <p/>
     * This form of the method has a flag to specify whether or not inference should be used on the set of semantic
     * tags: if true, results will annotate to all of the supplied semantic tags or any of their subtypes
     *
     * @param useInference whether or not to use inference on the semantic tags
     * @param semanticTags the set of semantic tags that all results should annotate to
     * @return a collection of studies that annotate to all of the supplied entities
     */
    Collection<Study> searchBySemanticTags(boolean useInference, URI... semanticTags);

    /**
     * Search the set of studies in ZOOMA for those with a matching accession. Accession matches are partial matches.  Note that
     * accessions in ZOOMA are not unique, as ZOOMA subsumes several different datasources.  It is reasonable to assume
     * that a study accession is unique within a single datasource, but this assumption may not hold across the whole
     * dataset in ZOOMA, and as such this method returns a collection of results.
     *
     * @param accession the accession to search for
     * @return a collection of studies that have a matching accession
     */
    Collection<Study> searchByStudyAccession(String accession);

    /**
     * Search the set of studies in ZOOMA for those with a matching properties.  Property matches are exact.
     *
     * @param property the properties to search for
     * @return a collection of studies that have a matching accession
     */
    Collection<Study> searchByProperty(Property... property);
}
