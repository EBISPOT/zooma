package uk.ac.ebi.fgpt.zooma.access;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.fgpt.zooma.datasource.AnnotationDAO;
import uk.ac.ebi.fgpt.zooma.datasource.ZoomaDAO;
import uk.ac.ebi.fgpt.zooma.exception.AmbiguousResourceException;
import uk.ac.ebi.fgpt.zooma.exception.NoSuchResourceException;
import uk.ac.ebi.fgpt.zooma.exception.ZoomaUpdateException;
import uk.ac.ebi.fgpt.zooma.model.*;
import uk.ac.ebi.fgpt.zooma.service.AnnotationService;
import uk.ac.ebi.fgpt.zooma.service.DataLoadingService;
import uk.ac.ebi.fgpt.zooma.service.PropertyService;
import uk.ac.ebi.fgpt.zooma.view.AnnotationBatchUpdateRequest;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

/**
 * Load data into ZOOMA from available datasources.
 * <p/>
 * This class is a high level convenience implementation for loading data.  It will work readily out of the box, but
 * requires configuration with underlying service implementations.  It is also a controller 'stereotype' that can be
 * used to construct a REST API.
 *
 * @author Tony Burdett
 * @date 12/06/13
 */
@Controller
public class ZoomaAnnotationLoader {
    private DataLoadingService<Annotation> dataLoadingService;

    private AnnotationService annotationService;

    private PropertyService propertyService;

    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public AnnotationService getAnnotationService() {
        return annotationService;
    }

    @Autowired
    public void setAnnotationService(AnnotationService annotationService) {
        this.annotationService = annotationService;
    }

    public PropertyService getPropertyService() {
        return propertyService;
    }

    @Autowired
    public void setPropertyService(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    public DataLoadingService<Annotation> getDataLoadingService() {
        return dataLoadingService;
    }

    @Autowired
    public void setDataLoadingService(DataLoadingService<Annotation> dataLoadingService) {
        this.dataLoadingService = dataLoadingService;
    }

    @RequestMapping(value = "/annotations", method = RequestMethod.POST)
    public @ResponseBody DataLoadingService.Receipt loadAnnotations(@RequestBody Collection<Annotation> annotations) {
        return getDataLoadingService().load(annotations);
    }

    @RequestMapping(value = "/annotations/batchUpdate", method = RequestMethod.POST)
    public @ResponseBody DataLoadingService.Receipt loadBatchOfAnnotations(
            @RequestBody AnnotationBatchUpdateRequest annotations,
            @RequestParam(value = "oldPropertyUriFilter", required = false) String oldPropertyUri,
            @RequestParam(value = "semanticTagUriFilter", required = false) String semanticTagUri,
            @RequestParam(value = "studyUriFilter", required = false) String studyUri,
            @RequestParam(value = "dataSourceFilter", required = true) String datasoureUri
    )  throws ZoomaUpdateException {

        // todo - check datasource and that user is authorised to edit

        // check old property URI exists.
        Collection<Annotation> annotationsToUpdate = new HashSet<Annotation>();


        if (oldPropertyUri != null) {
            Property oldProperty = getPropertyService().getProperty(oldPropertyUri);

            if (oldProperty != null) {

                if (semanticTagUri != null) {
                    for (Annotation annoByStudy : getAnnotationService().getAnnotationsBySemanticTag(URI.create(semanticTagUri))) {
                        if (annoByStudy.getAnnotatedProperty().equals(oldProperty)) {
                            if (annoByStudy.getProvenance().getSource().getURI().toString().equals(datasoureUri)) {
                                annotationsToUpdate.add(annoByStudy);
                            }
                        }
                    }
                }
                else if (studyUri != null) {
                    for (Annotation annoByStudy : getAnnotationService().getAnnotationsByStudy(new SimpleStudy(URI.create(studyUri), null))) {
                        if (annoByStudy.getAnnotatedProperty().equals(oldProperty)) {
                            if (annoByStudy.getProvenance().getSource().getURI().toString().equals(datasoureUri)) {
                                annotationsToUpdate.add(annoByStudy);
                            }
                        }
                    }
                }
                else {
                    for (Annotation annoByProp : getAnnotationService().getAnnotationsByProperty(oldProperty)) {
                        if (annoByProp.getProvenance().getSource().getURI().toString().equals(datasoureUri)) {
                            annotationsToUpdate.add(annoByProp);
                        }
                    }
                }
            }
            else {
                throw new IllegalArgumentException(
                        "Failed to update property: No property found with URI '" + oldPropertyUri + "'.");
            }


        }
        else if (semanticTagUri != null) {
            for (Annotation annoByStudy : getAnnotationService().getAnnotationsBySemanticTag(URI.create(semanticTagUri))) {
                if (annoByStudy.getProvenance().getSource().getURI().toString().equals(datasoureUri)) {
                    annotationsToUpdate.add(annoByStudy);
                }
            }
        }

        return null;

//        Collection<Annotation> newAnnos = getAnnotationService().updateAnnotation(annotationsToUpdate, annotations);

    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(AmbiguousResourceException.class)
    private @ResponseBody String handleAmbiguousResourceException(AmbiguousResourceException e) {
        getLog().error("Ambiguous resource reference", e);
        return "Data loading failed - " + e.getMessage();
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoSuchResourceException.class)
    private @ResponseBody String handleNoSuchResourceException(NoSuchResourceException e) {
        getLog().error("Tried to access a resource that does not exist", e);
        return "Data loading failed - " + e.getMessage();
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
