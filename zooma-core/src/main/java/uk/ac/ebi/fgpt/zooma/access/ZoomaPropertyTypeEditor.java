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
import uk.ac.ebi.fgpt.zooma.model.SimpleTypedProperty;
import uk.ac.ebi.fgpt.zooma.service.AnnotationService;
import uk.ac.ebi.fgpt.zooma.service.PropertyService;

import java.util.Collection;

/**
 * Edit property types in ZOOMA.
 * <p/>
 * This class is a high level convenience implementation for editing property types.  It will work out of the box, but
 * requires configuration with underlying service implementations. It is also a controller 'stereotype' that can be used
 * to construct a REST API.
 *
 * @author Drashtti Vasant
 * @author Tony Burdett
 * @date 28/05/13
 */
@Controller
@RequestMapping("/properties/types")
public class ZoomaPropertyTypeEditor {
    private PropertyService propertyService;
    private AnnotationService annotationService;

    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public ZoomaPropertyTypeEditor() {
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
     * Replaces a property type with a new value in all contexts in which old value occurs.
     *
     * @param propertyType    the property type to replace
     * @param newPropertyType the new property type to replace the old type with
     */
    @RequestMapping(method = RequestMethod.PUT, value = "/{propertyType}")
    public @ResponseBody String replacePropertyType(@PathVariable String propertyType,
                                                    @RequestBody String newPropertyType) throws ZoomaUpdateException {
        getLog().debug("Request received to replace all properties with type '" + propertyType + "' " +
                               "' to type '" + newPropertyType + "'");
        // search for all instances of properties matching "oldPropertyType" - (so e.g. "DiseaseState")
        Collection<Property> oldProperties = getPropertyService().getMatchedTypedProperties(propertyType);
        Property newProperty;
        int count = 0;
        if (oldProperties != null && !oldProperties.isEmpty()) {
            for (Property oldProperty : oldProperties) {
                //get the oldPropertyType's value
                String newPropertyValue = oldProperty.getPropertyValue();
                newProperty = new SimpleTypedProperty(newPropertyType, newPropertyValue);

                count += getAnnotationService().getAnnotationsByProperty(oldProperty).size();
                getPropertyService().updateProperty(oldProperty, newProperty);
            }
            return "Successfully updated " + count + "annotations";
        }
        else {
            return "Failed to update property: No property found matching type '" + propertyType + "'";
        }
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
