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
import uk.ac.ebi.fgpt.zooma.util.URIUtils;

import java.net.URI;

/**
 * Edits, replaces and deletes {@link uk.ac.ebi.fgpt.zooma.model.Annotation}s inside ZOOMA to support curator
 * operations.
 *
 * @author Tony Burdett
 * @author Drashtti Vasant
 * @date 12/07/13
 */
@Controller
@RequestMapping("/annotations")
public class ZoomaAnnotationEditor {
    private AnnotationService annotationService;

    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public ZoomaAnnotationEditor() {
        // default constructor - callers must set required properties
    }

    public ZoomaAnnotationEditor(AnnotationService annotationService) {
        this();
        setAnnotationService(annotationService);
    }

    public AnnotationService getAnnotationService() {
        return annotationService;
    }

    @Autowired
    public void setAnnotationService(AnnotationService annotationService) {
        this.annotationService = annotationService;
    }

    /**
     * Replaces the annotation with the given URI with the newly supplied one
     *
     * @param shortAnnotationURI the shortened URI of the annotation to replace
     * @param newAnnotation      the annotation to replace it with
     */
    @RequestMapping(value = "/{shortAnnotationURI}", method = RequestMethod.PUT)
    public @ResponseBody String replaceAnnotation(@PathVariable String shortAnnotationURI,
                                                  @RequestBody Annotation newAnnotation) throws ZoomaUpdateException {

        throw new UnsupportedOperationException("This operation is not supported.");

        // todo - need to think about what is supported for this operation
//        URI annotationURI = URIUtils.getURI(shortAnnotationURI);
//        Annotation oldAnnotation = getAnnotationService().getAnnotation(annotationURI);
//
//        // search for all instances of properties matching "oldAnnotationValue" - (so e.g. "cancer")
//        if (oldAnnotation != null) {
//            getAnnotationService().updateAnnotation(oldAnnotation, newAnnotation);
//            return "Successfully updated annotation '" + annotationURI + "'";
//        }
//        else {
//            throw new IllegalArgumentException(
//                    "Failed to update annotation: No annotation found with URI '" + annotationURI + "'.");
//        }
    }

    @RequestMapping(value = "/{shortAnnotationURI}", method = RequestMethod.DELETE)
    @ResponseBody String deleteAnnotation(@PathVariable String shortAnnotationURI) throws ZoomaUpdateException {
        URI annotationURI = URIUtils.getURI(shortAnnotationURI);
        Annotation annotation = getAnnotationService().getAnnotation(annotationURI);

        if (annotation != null) {
            getAnnotationService().deleteAnnotation(annotation);
            return "Successfully removed annotation '" + annotationURI + "'.";
        }
        else {
            throw new IllegalArgumentException(
                    "Failed to update annotation: No annotation found with URI '" + annotationURI + "'.");
        }
    }

    @RequestMapping(method = RequestMethod.PUT,
                    value = "/{studyAccession}/assays/{assayAccession}/properties")
    public @ResponseBody String addProperties(@RequestBody Property property,
                                              @PathVariable String studyAccession,
                                              @PathVariable String assayAccession) {
        getLog().debug("Request received to update property values for the following Study -" + studyAccession +
                               " and the following assay - " + assayAccession);
        throw new UnsupportedOperationException("This operation is not supported.");
    }

    @RequestMapping(method = RequestMethod.DELETE,
                    value = "/{studyAccession}/assays/{assayAccession}/properties")
    public @ResponseBody String deleteProperties(@RequestBody Property property,
                                                 @PathVariable String studyAccession,
                                                 @PathVariable String assayAccession) {
        getLog().debug("Request received to update property values for the following Study -" + studyAccession +
                               " and the following assay - " + assayAccession);
        throw new UnsupportedOperationException("This operation is not supported.");
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
