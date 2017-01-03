package uk.ac.ebi.spot.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.spot.model.Annotation;
import uk.ac.ebi.spot.model.MongoAnnotation;
import uk.ac.ebi.spot.service.AnnotationSavingService;

/**
 * Created by olgavrou on 19/09/2016.
 */
@Controller
public class SaveAnnotationToMongo implements AnnotationSavingService<Annotation> {

    @Autowired
    private MongoAnnotationRepositoryService repositoryService;

    public void save(Annotation annotation){

        MongoAnnotation mongoAnnotation = (MongoAnnotation) annotation;
        repositoryService.save(mongoAnnotation);
    }

}
