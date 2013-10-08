package uk.ac.ebi.fgpt.zooma.service;

import uk.ac.ebi.fgpt.zooma.datasource.BiologicalEntityDAO;
import uk.ac.ebi.fgpt.zooma.model.BiologicalEntity;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A biological entity search service that uses an implementation of {@link uk.ac.ebi.fgpt.zooma.datasource.BiologicalEntityDAO}
 * to search for biological entities.
 *
 * @author Tony Burdett
 * @date 12/06/12
 */
public class DAOBasedBiologicalEntitySearchService
        extends AbstractShortnameResolver
        implements BiologicalEntitySearchService {
    private BiologicalEntityDAO biologicalEntityDAO;

    public BiologicalEntityDAO getBiologicalEntityDAO() {
        return biologicalEntityDAO;
    }

    public void setBiologicalEntityDAO(BiologicalEntityDAO biologicalEntityDAO) {
        this.biologicalEntityDAO = biologicalEntityDAO;
    }

    @Override public Collection<BiologicalEntity> searchBySemanticTags(String... semanticTagShortnames) {
        List<URI> uris = new ArrayList<>();
        for (String s : semanticTagShortnames) {
            uris.add(getURIFromShortname(s));
        }
        return searchBySemanticTags(uris.toArray(new URI[semanticTagShortnames.length]));
    }

    @Override public Collection<BiologicalEntity> searchBySemanticTags(URI... semanticTags) {
        return getBiologicalEntityDAO().readBySemanticTags(semanticTags);
    }

    @Override
    public Collection<BiologicalEntity> searchBySemanticTags(boolean useInference,
                                                             String... semanticTagShortnames) {
        List<URI> uris = new ArrayList<>();
        for (String s : semanticTagShortnames) {
            uris.add(getURIFromShortname(s));
        }
        return searchBySemanticTags(useInference, uris.toArray(new URI[semanticTagShortnames.length]));
    }

    @Override public Collection<BiologicalEntity> searchBySemanticTags(boolean useInference,
                                                                       URI... semanticTags) {
        return getBiologicalEntityDAO().readBySemanticTags(useInference, semanticTags);
    }

    @Override public Map<BiologicalEntity, Float> searchAndScoreBySemanticTags(String... semanticTagShortnames) {
        return addScores(searchBySemanticTags(semanticTagShortnames));
    }

    @Override public Map<BiologicalEntity, Float> searchAndScoreBySemanticTags(URI... semanticTags) {
        return addScores(searchBySemanticTags(semanticTags));
    }

    @Override public Map<BiologicalEntity, Float> searchAndScoreBySemanticTags(boolean useInference,
                                                                               String... semanticTagShortnames) {
        return addScores(searchBySemanticTags(useInference, semanticTagShortnames));
    }

    @Override public Map<BiologicalEntity, Float> searchAndScoreBySemanticTags(boolean useInference,
                                                                               URI... semanticTags) {
        return addScores(searchBySemanticTags(useInference, semanticTags));
    }

    private Map<BiologicalEntity, Float> addScores(Collection<BiologicalEntity> biologicalEntities) {
        Map<BiologicalEntity, Float> results = new HashMap<>();
        for (BiologicalEntity be : biologicalEntities) {
            results.put(be, 1.0f);
        }
        return results;
    }
}
