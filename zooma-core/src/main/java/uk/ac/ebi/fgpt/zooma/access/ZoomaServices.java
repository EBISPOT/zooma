package uk.ac.ebi.fgpt.zooma.access;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.ac.ebi.fgpt.zooma.service.OntologyService;
import uk.ac.ebi.fgpt.zooma.service.StatusService;
import uk.ac.ebi.fgpt.zooma.util.PropertiesMapAdapter;
import uk.ac.ebi.fgpt.zooma.util.URIUtils;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A service class that allows common interactions with the ZOOMA services.  This includes common requests like
 * rebuilding or updating indexes.
 * <p/>
 * This class is a controller 'stereotype' that allows these actions to be triggered directly from a REST-like request.
 *
 * @author Tony Burdett
 * @date 31/05/12
 */
@Controller
@RequestMapping("/services")
public class ZoomaServices {
    private OntologyService ontologyService;
    private StatusService statusService;
    private PropertiesMapAdapter propertiesMapAdapter;

    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public OntologyService getOntologyService() {
        return ontologyService;
    }

    @Autowired
    public void setOntologyService(OntologyService ontologyService) {
        this.ontologyService = ontologyService;
    }

    public StatusService getStatusService() {
        return statusService;
    }

    @Autowired
    public void setStatusService(StatusService statusService) {
        this.statusService = statusService;
    }

    public PropertiesMapAdapter getPropertiesMapAdapter() {
        return propertiesMapAdapter;
    }

    @Autowired
    public void setPropertiesMapAdapter(PropertiesMapAdapter propertiesMapAdapter) {
        this.propertiesMapAdapter = propertiesMapAdapter;
    }

    @RequestMapping(value = "/check-status", method = RequestMethod.GET)
    public @ResponseBody boolean checkStatus() {
        return getStatusService().checkStatus();
    }

    @RequestMapping(value = "/reinitialize", method = RequestMethod.GET)
    public @ResponseBody String reinitialize() {
        return getStatusService().reinitialize();
    }

    @RequestMapping(value = "/labels/{shortURI}", method = RequestMethod.GET)
    public @ResponseBody Map<String, Set<String>> getLabels(@PathVariable String shortURI) {
        URI uri = URIUtils.getURI(shortURI);
        String label = getOntologyService().getLabel(uri);
        Set<String> synonyms = getOntologyService().getSynonyms(uri);
        Map<String, Set<String>> result = new HashMap<>();
        result.put("label", Collections.singleton(label));
        result.put("synonyms", synonyms);
        return result;
    }

    @RequestMapping(value = "/expand/{shortURI}", method = RequestMethod.GET)
    public @ResponseBody Map<String, String> getFullURI(@PathVariable String shortURI) {
        URI uri = URIUtils.getURI(shortURI);
        Map<String, String> result = new HashMap<>();
        result.put("uri", uri.toString());
        result.put("shortname", shortURI);
        result.put("namespace", URIUtils.getNamespace(shortURI).toString());
        result.put("fragment", URIUtils.getFragment(shortURI));
        return result;
    }

    @RequestMapping(value = "/collapse", method = RequestMethod.POST)
    public @ResponseBody Map<String, String> getShortURI(@RequestBody Map<String, String> uriMap) {
        URI uri = URI.create(uriMap.get("uri"));
        String shortURI = URIUtils.getShortform(uri);
        Map<String, String> result = new HashMap<>();
        result.put("uri", uri.toString());
        result.put("shortname", shortURI);
        result.put("namespace", URIUtils.getNamespace(shortURI).toString());
        result.put("fragment", URIUtils.getFragment(shortURI));
        return result;
    }

    @RequestMapping(value = "/prefixMappings", method = RequestMethod.GET)
    public @ResponseBody Map<String, String> getPrefixMappings() {
        return URIUtils.getPrefixMappings();
    }
}
