package uk.ac.ebi.fgpt.zooma.access;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.ac.ebi.fgpt.zooma.model.Annotation;
import uk.ac.ebi.fgpt.zooma.model.AnnotationPattern;
import uk.ac.ebi.fgpt.zooma.model.AnnotationSummary;
import uk.ac.ebi.fgpt.zooma.model.Property;
import uk.ac.ebi.fgpt.zooma.service.AnnotationPatternService;
import uk.ac.ebi.fgpt.zooma.service.PropertyService;

import java.net.URI;
import java.util.Collection;
import java.util.HashSet;

/**
 *
 * Search zooma for unique combinations of AnnotationPattern objects
 *
 * @author Simon Jupp
 * @date 28/01/2014
 * Functional Genomics Group EMBL-EBI
 */
@Controller
@RequestMapping("/patterns")
public class ZoomaAnnotationPatterns {

    private AnnotationPatternService annotationPatternService;
    private PropertyService propertyService;

    public AnnotationPatternService getAnnotationPatternService() {
        return annotationPatternService;
    }

    @Autowired
    public void setAnnotationPatternService(AnnotationPatternService annotationPatternService) {
        this.annotationPatternService = annotationPatternService;
    }

    public PropertyService getPropertyService() {
        return propertyService;
    }

    @Autowired
    public void setPropertyService(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    @RequestMapping(method = RequestMethod.GET)
    public @ResponseBody
    Collection<AnnotationPattern> search(
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "exact", required = false, defaultValue = "false") boolean exact,
            @RequestParam(value = "semanticTag", required = false) String semanticTag,
            @RequestParam(value = "latest", required = false, defaultValue = "false")  boolean latestOnly,
            @RequestParam(value = "source", required = false) Collection<String> sources) {

        Collection<URI> sourcesURI = new HashSet<>();
        for (String s : sources) {
            sourcesURI.add(URI.create(s));
        }

        if (semanticTag != null) {
            return  filterLatest(latestOnly, getAnnotationPatternService().readBySemanticTag(URI.create(semanticTag), sourcesURI.toArray(new URI[sourcesURI.size()])));
        }

        if (query == null && type == null) {
            return  filterLatest(latestOnly, getAnnotationPatternService().read(sourcesURI.toArray(new URI[sourcesURI.size()])));
        }

        if (exact) {
               Collection<AnnotationPattern> patterns = new HashSet<>();
            for (Property property : getPropertyService().getMatchedTypedProperty(type, query)) {
                patterns.addAll(filterLatest(latestOnly, getAnnotationPatternService().readByProperty(property, sourcesURI.toArray(new URI[sourcesURI.size()]))));
            }
            return patterns;
        }
        return  filterLatest(latestOnly, getAnnotationPatternService().search(type, query, sourcesURI.toArray(new URI[sourcesURI.size()])));
    }

    private Collection<AnnotationPattern> filterLatest(boolean filter,Collection<AnnotationPattern> patterns ) {
        if (filter) {
            HashSet<AnnotationPattern> returned = new HashSet<>();
            for (AnnotationPattern pattern : patterns) {
                if (!pattern.isReplaced()) {
                    returned.add(pattern);
                }
            }
            return returned;
        }
        return patterns;
    }


}
