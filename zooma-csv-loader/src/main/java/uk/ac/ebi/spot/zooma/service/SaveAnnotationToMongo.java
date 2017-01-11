package uk.ac.ebi.spot.zooma.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.spot.zooma.model.mongo.Annotation;
import uk.ac.ebi.spot.zooma.repository.mongo.AnnotationRepository;

/**
 * Created by olgavrou on 19/09/2016.
 */
@Controller
public class SaveAnnotationToMongo implements AnnotationSavingService<Annotation> {

    @Autowired
    private AnnotationRepository repositoryService;

    public void save(Annotation annotation){

        Annotation mongoAnnotation = (Annotation) annotation;
        repositoryService.save(mongoAnnotation);
    }

}
