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
import uk.ac.ebi.fgpt.zooma.model.BiologicalEntity;
import uk.ac.ebi.fgpt.zooma.service.BiologicalEntitySearchService;
import uk.ac.ebi.fgpt.zooma.service.BiologicalEntityService;
import uk.ac.ebi.fgpt.zooma.util.URIUtils;

import java.net.URI;
import java.util.Collection;

/**
 * Search ZOOMA for {@link BiologicalEntity}s matching particular profiles.
 * <p/>
 * This class is a high level convenience implementation for searching biological entities.  It will work out of the
 * box, but requires configuration with underlying service implementations. It is also a controller 'stereotype' that
 * can be used to construct a REST API.
 *
 * @author Tony Burdett
 * @date 12/06/12
 */
@Controller
@RequestMapping("/biologicalEntities")
public class ZoomaBiologicalEntitySearcher {
    private BiologicalEntityService biologicalEntityService;
    private BiologicalEntitySearchService biologicalEntitySearchService;

    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public BiologicalEntityService getBiologicalEntityService() {
        return biologicalEntityService;
    }

    @Autowired
    public void setBiologicalEntityService(BiologicalEntityService biologicalEntityService) {
        this.biologicalEntityService = biologicalEntityService;
    }

    public BiologicalEntitySearchService getBiologicalEntitySearchService() {
        return biologicalEntitySearchService;
    }

    @Autowired
    public void setBiologicalEntitySearchService(BiologicalEntitySearchService biologicalEntitySearchService) {
        this.biologicalEntitySearchService = biologicalEntitySearchService;
    }

    @RequestMapping(method = RequestMethod.GET)
    public @ResponseBody Collection<BiologicalEntity> fetch(
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "start", required = false) Integer start) {
        if (start == null) {
            if (limit == null) {
                return getBiologicalEntityService().getBiologicalEntities(100, 0);
            }
            else {
                return getBiologicalEntityService().getBiologicalEntities(limit, 0);
            }
        }
        else {
            if (limit == null) {
                return getBiologicalEntityService().getBiologicalEntities(100, start);
            }
            else {
                return getBiologicalEntityService().getBiologicalEntities(limit, start);
            }
        }
    }

    /**
     * Retrieves a biological entity with the given URI.
     *
     * @param shortBiologicalEntityURI the shortened form of the URI of the biologicalEntity to fetch
     * @return the biologicalEntity with the given URI
     */
    @RequestMapping(value = "/{shortBiologicalEntityURI}", method = RequestMethod.GET)
    public @ResponseBody BiologicalEntity fetch(@PathVariable String shortBiologicalEntityURI) {
        URI biologicalEntityURI = URIUtils.getURI(shortBiologicalEntityURI);
        getLog().debug("Fetching " + biologicalEntityURI);
        return getBiologicalEntityService().getBiologicalEntity(biologicalEntityURI);
    }

    /**
     * Returns a collection of biological entities that annotate to the supplied semantic tags
     *
     * @param semanticTagURIs the set of semantic tags
     * @param useInference    whether to use inference to expand possible hits
     * @return the collection of biological entities that annotate to the supplied semantic tags
     */
    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public @ResponseBody Collection<BiologicalEntity> search(@RequestParam String[] semanticTagURIs,
                                                             @RequestParam(value = "useInference",
                                                                           required = false,
                                                                           defaultValue = "false")
                                                             boolean useInference) {
        getLog().trace("Request contains " + semanticTagURIs.length + " semantic tag parameters");
        URI[] deArray = new URI[semanticTagURIs.length];
        for (int i = 0; i < deArray.length; i++) {
            String s = semanticTagURIs[i];
            URI uri = URIUtils.getURI(s);
            getLog().trace("Next request element: " + s + " (type = " + s.getClass() + ") -> URI '" + uri + "'");
            deArray[i] = uri;
        }

        getLog().trace("Searching for biological entities with the supplied combination of " +
                               deArray.length + " entities");
        Collection<BiologicalEntity> results =
                getBiologicalEntitySearchService().searchBySemanticTags(useInference, deArray);
        getLog().trace("Acquired " + results.size() + " biological entities");
        return results;
    }
}
