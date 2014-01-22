package uk.ac.ebi.fgpt.zooma.service;

import uk.ac.ebi.fgpt.zooma.datasource.StudyDAO;
import uk.ac.ebi.fgpt.zooma.model.Study;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An study search service that uses an implementation of {@link uk.ac.ebi.fgpt.zooma.datasource.StudyDAO} to search for
 * studies.
 *
 * @author Tony Burdett
 * @date 12/06/12
 */
public class DAOBasedStudySearchService extends AbstractShortnameResolver implements StudySearchService {
    private StudyDAO studyDAO;

    public StudyDAO getStudyDAO() {
        return studyDAO;
    }

    public void setStudyDAO(StudyDAO studyDAO) {
        this.studyDAO = studyDAO;
    }

    @Override public Collection<Study> searchBySemanticTags(String... semanticTagShortnames) {
        List<URI> uris = new ArrayList<>();
        for (String s : semanticTagShortnames) {
            uris.add(getURIFromShortname(s));
        }
        return searchBySemanticTags(uris.toArray(new URI[semanticTagShortnames.length]));
    }

    @Override public Collection<Study> searchBySemanticTags(URI... semanticTags) {
        return getStudyDAO().readBySemanticTags(semanticTags);
    }

    @Override
    public Collection<Study> searchBySemanticTags(boolean useInference, String... semanticTagShortnames) {
        List<URI> uris = new ArrayList<>();
        for (String s : semanticTagShortnames) {
            uris.add(getURIFromShortname(s));
        }
        return searchBySemanticTags(useInference, uris.toArray(new URI[semanticTagShortnames.length]));
    }

    @Override public Collection<Study> searchBySemanticTags(boolean useInference, URI... semanticTags) {
        return getStudyDAO().readBySemanticTags(useInference, semanticTags);
    }

    @Override public Collection<Study> searchByStudyAccession(String accession) {
        return getStudyDAO().readByAccession(accession);
    }
}
