package uk.ac.ebi.fgpt.zooma.service;

import uk.ac.ebi.fgpt.zooma.datasource.StudyDAO;
import uk.ac.ebi.fgpt.zooma.model.Study;

import java.net.URI;
import java.util.Collection;

/**
 * An study service that uses an implementation of {@link uk.ac.ebi.fgpt.zooma.datasource.StudyDAO} to retrieve
 * studies.
 *
 * @author Tony Burdett
 * @date 12/07/13
 */
public class DAOBasedStudyService extends AbstractShortnameResolver implements StudyService {
    private StudyDAO studyDAO;

    public StudyDAO getStudyDAO() {
        return studyDAO;
    }

    public void setStudyDAO(StudyDAO studyDAO) {
        this.studyDAO = studyDAO;
    }


    @Override public Collection<Study> getStudies() {
        return getStudyDAO().read();
    }

    @Override public Collection<Study> getStudies(int limit, int start) {
        return getStudyDAO().read(limit, start);
    }

    @Override public Study getStudy(String shortname) {
        return getStudyDAO().read(getURIFromShortname(shortname));
    }

    @Override public Study getStudy(URI uri) {
        return getStudyDAO().read(uri);
    }
}
