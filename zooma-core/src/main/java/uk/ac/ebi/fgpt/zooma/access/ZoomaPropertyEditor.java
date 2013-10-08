package uk.ac.ebi.fgpt.zooma.access;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import uk.ac.ebi.fgpt.zooma.exception.ZoomaUpdateException;
import uk.ac.ebi.fgpt.zooma.model.Annotation;
import uk.ac.ebi.fgpt.zooma.model.Property;
import uk.ac.ebi.fgpt.zooma.service.AnnotationService;
import uk.ac.ebi.fgpt.zooma.service.PropertyService;
import uk.ac.ebi.fgpt.zooma.util.URIUtils;

import java.net.URI;
import java.util.Collection;

/**
 * Edits, replaces and deletes {@link uk.ac.ebi.fgpt.zooma.model.Property}s inside ZOOMA to support curator operations.
 *
 * @author Tony Burdett
 * @author Drashtti Vasant
 * @date 24/05/13
 */
@Controller
@RequestMapping("/properties")
public class ZoomaPropertyEditor {
    private PropertyService propertyService;
    private AnnotationService annotationService;

    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public ZoomaPropertyEditor() {
        // default constructor - callers must set required properties
    }

    public PropertyService getPropertyService() {
        return propertyService;
    }

    @Autowired
    public void setPropertyService(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    public AnnotationService getAnnotationService() {
        return annotationService;
    }

    @Autowired
    public void setAnnotationService(AnnotationService annotationService) {
        this.annotationService = annotationService;
    }

    /**
     * Replaces the property with the given URI with the newly supplied one
     *
     * @param shortPropertyURI the shortened URI of the property to replace
     * @param newProperty      the property to replace it with
     */
    @RequestMapping(value = "/{shortPropertyURI}", method = RequestMethod.PUT)
    public @ResponseBody String replaceProperty(@PathVariable String shortPropertyURI,
                                                @RequestBody Property newProperty) throws ZoomaUpdateException {
        URI propertyURI = URIUtils.getURI(shortPropertyURI);
        Property oldProperty = getPropertyService().getProperty(propertyURI);

        // search for all instances of properties matching "oldPropertyValue" - (so e.g. "cancer")
        if (oldProperty != null) {
            getLog().debug("Request received to replace property '" + oldProperty + "' with '" + newProperty + "'");
            int count = getAnnotationService().getAnnotationsByProperty(oldProperty).size();
            getPropertyService().updateProperty(oldProperty, newProperty);
            return "Successfully updated property '" + propertyURI + "', affecting " + count + " annotations";
        }
        else {
            throw new IllegalArgumentException(
                    "Failed to update property: No property found with URI '" + propertyURI + "'.");
        }
    }

    @RequestMapping(value = "/{shortPropertyURI}", method = RequestMethod.DELETE)
    @ResponseBody String deleteProperty(@PathVariable String shortPropertyURI) throws ZoomaUpdateException {
        URI propertyURI = URIUtils.getURI(shortPropertyURI);
        Property property = getPropertyService().getProperty(propertyURI);

        if (property != null) {
            getLog().debug("Request received to delete property '" + property + "'.");
            getPropertyService().deleteProperty(property);
            return "Successfully removed property '" + propertyURI + "'.";
        }
        else {
            throw new IllegalArgumentException(
                    "Failed to update property: No property found with URI '" + propertyURI + "'.");

        }
    }

    @RequestMapping(method = RequestMethod.DELETE, params = "unused")
    @ResponseBody String deleteUnusedProperties() throws ZoomaUpdateException {
        getLog().debug("Request received to delete unused properties");
        int propertyCount = 0;
        Collection<Property> properties = getPropertyService().getProperties();
        //now loop through all the properties and check for annotations. If no annotations associated you can delete that property
        for (Property property : properties) {
            Collection<Annotation> annotations = getAnnotationService().getAnnotationsByProperty(property);
            if (annotations.isEmpty()) {
                //that is it has no annotations for that property
                getPropertyService().deleteProperty(property);
                propertyCount++;
            }
        }
        return propertyCount + " unused properties were deleted.";
    }

    @ExceptionHandler(ZoomaUpdateException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public @ResponseBody String handleException(ZoomaUpdateException exception) {
        getLog().error("Failed zooma update", exception);
        return exception.getMessage();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public @ResponseBody String handleException(IllegalArgumentException exception) {
        getLog().error("Caught illegal argument exception", exception);
        return exception.getMessage();
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public @ResponseBody String handleException(UnsupportedOperationException exception) {
        getLog().error("Caught unsupported operation exception", exception);
        return exception.getMessage();
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    private @ResponseBody String handleException(Exception e) {
        getLog().error("Uncaught exception!", e);
        return "The server encountered an error it could not recover from - " + e.getMessage();
    }
}
