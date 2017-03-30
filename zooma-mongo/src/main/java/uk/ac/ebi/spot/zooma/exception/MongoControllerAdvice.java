package uk.ac.ebi.spot.zooma.exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionException;
import org.springframework.data.rest.webmvc.support.RepositoryEntityLinks;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.VndErrors;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import uk.ac.ebi.spot.zooma.repository.mongo.AnnotationRepository;


/**
 * Created by olgavrou on 15/03/2017.
 */
@ControllerAdvice
public class MongoControllerAdvice {

    private final Logger log = LoggerFactory.getLogger(getClass());
    protected Logger getLog() {
        return log;
    }

    @Autowired
    RepositoryEntityLinks entityLinks;

    @ResponseBody
    @ExceptionHandler(AnnotationAlreadyExiststException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    VndErrors annotationAlreadyExiststExceptionHandler(AnnotationAlreadyExiststException e){
        if(e.getMessage() != null){
            Link link = entityLinks.linkToSingleResource(AnnotationRepository.class, e.getMessage());
            getLog().debug("Annotation already exists! id: " + e.getMessage());
            return new VndErrors("error", "Annotation already exists!", link);
        }
        return new VndErrors("error", "Annotation already exists!");
    }

    @ResponseBody
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    VndErrors illegalArgumentExceptionHandler(IllegalArgumentException e){
        getLog().error(e.getMessage());
        return new VndErrors("error", e.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(ConversionException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    VndErrors conversionFailedExceptionHandler(ConversionException e){
        getLog().error("Conversion failed: " + e.getMessage());
        return new VndErrors("error", e.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(JsonMappingException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    VndErrors jsonMappingException(JsonMappingException e){
        getLog().error("Json Mapping failed: " + e.getMessage());
        return new VndErrors("error", e.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(AnnotationNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    VndErrors annotationNotFoundExceptionHandler(AnnotationNotFoundException e){
        return new VndErrors("error", e.getMessage());
    }


}
