package uk.ac.ebi.fgpt.zooma.access;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.ac.ebi.fgpt.zooma.model.Study;
import uk.ac.ebi.fgpt.zooma.service.StudySearchService;
import uk.ac.ebi.fgpt.zooma.service.StudyService;
import uk.ac.ebi.fgpt.zooma.util.URIUtils;

import java.net.URI;
import java.util.Collection;

/**
 * Search ZOOMA for {@link Study}s matching particular profiles.
 * <p/>
 * This class is a high level convenience implementation for searching studies.  It will work out of the box, but
 * requires configuration with underlying service implementations. It is also a controller 'stereotype' that can be used
 * to construct a REST API.
 *
 * @author Tony Burdett
 * @date 12/06/12
 */
@Controller
@RequestMapping("/studies")
public class ZoomaStudySearcher {
    private StudyService studyService;
    private StudySearchService studySearchService;

    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public StudyService getStudyService() {
        return studyService;
    }

    @Autowired
    public void setStudyService(StudyService studyService) {
        this.studyService = studyService;
    }

    public StudySearchService getStudySearchService() {
        return studySearchService;
    }

    @Autowired
    public void setStudySearchService(StudySearchService studySearchService) {
        this.studySearchService = studySearchService;
    }

    @RequestMapping(method = RequestMethod.GET)
    public @ResponseBody Collection<Study> fetch(
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "start", required = false) Integer start) {
        if (start == null) {
            if (limit == null) {
                return getStudyService().getStudies(100, 0);
            }
            else {
                return getStudyService().getStudies(limit, 0);
            }
        }
        else {
            if (limit == null) {
                return getStudyService().getStudies(100, start);
            }
            else {
                return getStudyService().getStudies(limit, start);
            }
        }
    }

    /**
     * Retrieves a biological entity with the given URI.
     *
     * @param shortStudyURI the shortened form of the URI of the study to fetch
     * @return the study with the given URI
     */
    @RequestMapping(value = "/{shortStudyURI}", method = RequestMethod.GET)
    public @ResponseBody Study fetch(@PathVariable String shortStudyURI) {
        URI studyURI = URIUtils.getURI(shortStudyURI);
        getLog().debug("Fetching " + studyURI);
        return getStudyService().getStudy(studyURI);
    }

    /**
     * Returns a collection of studies that annotate to the supplied semantic tags
     *
     * @param semanticTags the set of semantic tags
     * @param useInference whether to use inference to expand possible hits
     * @return the collection of studies that annotate to the supplied semantic tags
     */
    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public @ResponseBody Collection<Study> search(
            @RequestParam(value = "semanticTag", required = false) String[] semanticTags,
            @RequestParam(value = "accession", required = false) String accession,
            @RequestParam(value = "useInference", required = false, defaultValue = "false") boolean useInference) {
        if (semanticTags != null) {
            if (accession == null) {
                getLog().trace("Request contains " + semanticTags.length + " semantic tag parameters");
                URI[] deArray = new URI[semanticTags.length];
                for (int i = 0; i < deArray.length; i++) {
                    String s = semanticTags[i];
                    URI uri = URIUtils.getURI(s);
                    getLog().trace("Next request element: " + s + " (type = " + s.getClass() + ") -> URI '" + uri + "'");
                    deArray[i] = uri;
                }

                getLog().trace("Retrieving studies with the supplied combination of " + deArray.length + " entities");
                Collection<Study> results =
                        getStudySearchService().searchBySemanticTags(useInference, deArray);
                getLog().trace("Acquired " + results.size() + " studies");
                return results;
            }
            else {
                throw new IllegalArgumentException("Please supply one of semanticTag OR accession argument, not both");
            }
        }
        else {
            if (accession != null) {
                getLog().trace("Retrieving studies with the accession '" + accession + "'");
                Collection<Study> results = getStudySearchService().searchByStudyAccession(accession);
                getLog().trace("Acquired " + results.size() + " studies");
                return results;
            }
            else {
                throw new IllegalArgumentException("Please supply either a semanticTag or accession argument to search");
            }
        }
    }
}
