package uk.ac.ebi.fgpt.zooma.service;

import uk.ac.ebi.fgpt.zooma.model.Study;

import java.net.URI;
import java.util.Collection;
import java.util.Map;

/**
 * An abstract decorator of a {@link StudySearchService}.  You should subclass this decorator to create different
 * decorations that add functionality to study searches.
 *
 * @author Tony Burdett
 * @date 02/08/13
 */
public abstract class StudySearchServiceDecorator implements StudySearchService {
    private final StudySearchService _studySearchService;

    protected StudySearchServiceDecorator(StudySearchService studySearchService) {
        this._studySearchService = studySearchService;
    }

    @Override public Collection<Study> searchBySemanticTags(String... semanticTagShortnames) {
        return _studySearchService.searchBySemanticTags(semanticTagShortnames);
    }

    @Override public Collection<Study> searchBySemanticTags(URI... semanticTags) {
        return _studySearchService.searchBySemanticTags(semanticTags);
    }

    @Override public Collection<Study> searchBySemanticTags(boolean useInference, String... semanticTagShortnames) {
        return _studySearchService.searchBySemanticTags(useInference, semanticTagShortnames);
    }

    @Override public Collection<Study> searchBySemanticTags(boolean useInference, URI... semanticTags) {
        return _studySearchService.searchBySemanticTags(useInference, semanticTags);
    }

    @Override public Collection<Study> searchByStudyAccession(String accession) {
        return _studySearchService.searchByStudyAccession(accession);
    }

    @Override public Map<Study, Float> searchAndScoreBySemanticTags(String... semanticTagShortnames) {
        return _studySearchService.searchAndScoreBySemanticTags(semanticTagShortnames);
    }

    @Override public Map<Study, Float> searchAndScoreBySemanticTags(URI... semanticTags) {
        return _studySearchService.searchAndScoreBySemanticTags(semanticTags);
    }

    @Override public Map<Study, Float> searchAndScoreBySemanticTags(boolean useInference,
                                                                    String... semanticTagShortnames) {
        return _studySearchService.searchAndScoreBySemanticTags(useInference, semanticTagShortnames);
    }

    @Override public Map<Study, Float> searchAndScoreBySemanticTags(boolean useInference, URI... semanticTags) {
        return _studySearchService.searchAndScoreBySemanticTags(useInference, semanticTags);
    }

    @Override public Map<Study, Float> searchAndScoreByStudyAccession(String accession) {
        return _studySearchService.searchAndScoreByStudyAccession(accession);
    }
}
