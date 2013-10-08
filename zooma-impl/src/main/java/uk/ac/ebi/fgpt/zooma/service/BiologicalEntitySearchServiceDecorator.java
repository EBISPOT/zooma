package uk.ac.ebi.fgpt.zooma.service;

import uk.ac.ebi.fgpt.zooma.model.BiologicalEntity;

import java.net.URI;
import java.util.Collection;
import java.util.Map;

/**
 * An abstract decorator of a {@link BiologicalEntitySearchService}.  You should subclass this decorator to create
 * different decorations that add functionality to biological entity searches.
 *
 * @author Tony Burdett
 * @date 02/08/13
 * @see BiologicalEntitySearchService
 */
public abstract class BiologicalEntitySearchServiceDecorator implements BiologicalEntitySearchService {
    private BiologicalEntitySearchService _biologicalEntitySearchService;

    protected BiologicalEntitySearchServiceDecorator(BiologicalEntitySearchService biologicalEntitySearchService) {
        this._biologicalEntitySearchService = biologicalEntitySearchService;
    }

    @Override public Collection<BiologicalEntity> searchBySemanticTags(String... semanticTagShortnames) {
        return _biologicalEntitySearchService.searchBySemanticTags(semanticTagShortnames);
    }

    @Override public Collection<BiologicalEntity> searchBySemanticTags(URI... semanticTags) {
        return _biologicalEntitySearchService.searchBySemanticTags(semanticTags);
    }

    @Override public Collection<BiologicalEntity> searchBySemanticTags(boolean useInference,
                                                                       String... semanticTagShortnames) {
        return _biologicalEntitySearchService.searchBySemanticTags(useInference, semanticTagShortnames);
    }

    @Override public Collection<BiologicalEntity> searchBySemanticTags(boolean useInference, URI... semanticTags) {
        return _biologicalEntitySearchService.searchBySemanticTags(useInference, semanticTags);
    }

    @Override public Map<BiologicalEntity, Float> searchAndScoreBySemanticTags(String... semanticTagShortnames) {
        return _biologicalEntitySearchService.searchAndScoreBySemanticTags(semanticTagShortnames);
    }

    @Override public Map<BiologicalEntity, Float> searchAndScoreBySemanticTags(URI... semanticTags) {
        return _biologicalEntitySearchService.searchAndScoreBySemanticTags(semanticTags);
    }

    @Override public Map<BiologicalEntity, Float> searchAndScoreBySemanticTags(boolean useInference,
                                                                               String... semanticTagShortnames) {
        return _biologicalEntitySearchService.searchAndScoreBySemanticTags(useInference, semanticTagShortnames);
    }

    @Override public Map<BiologicalEntity, Float> searchAndScoreBySemanticTags(boolean useInference,
                                                                               URI... semanticTags) {
        return _biologicalEntitySearchService.searchAndScoreBySemanticTags(useInference, semanticTags);
    }
}
