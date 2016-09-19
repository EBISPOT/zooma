package uk.ac.ebi.spot.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.spot.model.Annotation;
import uk.ac.ebi.spot.model.SimpleAnnotation;
import uk.ac.ebi.spot.services.AnnotationRepositoryService;

/**
 * Created by olgavrou on 19/09/2016.
 */
@Controller
public class SaveToMongoController {

    @Autowired
    private AnnotationRepositoryService annotationRepositoryService;

    public void save(Annotation annotation){

        SimpleAnnotation simpleAnnotation = (SimpleAnnotation) annotation;
        annotationRepositoryService.save(simpleAnnotation);
    }

}
