package uk.ac.ebi.spot.zooma.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.file.FlatFileItemWriter;
import uk.ac.ebi.spot.zooma.model.SimpleAnnotation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by olgavrou on 20/01/2017.
 */
public class CustomItemWriter extends FlatFileItemWriter<SimpleAnnotation> {

    private AnnotationHandler annotationHandler;

    private final Logger log = LoggerFactory.getLogger(getClass());
    protected Logger getLog() {
        return log;
    }

    public CustomItemWriter(AnnotationHandler annotationHandler) {
        this.annotationHandler = annotationHandler;
    }

    @Override
    public void write(List<? extends SimpleAnnotation> items) throws Exception {
        getLog().info("Loading " + items.size() + " annotations");
        List<SimpleAnnotation> newItems = new ArrayList<>();
        for (SimpleAnnotation simpleAnnotation : items) {
            String annotationid = simpleAnnotation.getAnnotationid();
            if(annotationid.isEmpty()){
                annotationHandler.postNewAnnotation(simpleAnnotation);
            } else {
                annotationHandler.updateOldAnnotation(simpleAnnotation);
            }
            Optional<String> id = annotationHandler.getAnnotationId(simpleAnnotation);
            if(id.isPresent()) {
                simpleAnnotation.setAnnotationid(id.get());
            }
            newItems.add(simpleAnnotation);
        }
        super.write(newItems);
        getLog().info("done!");
    }

}
