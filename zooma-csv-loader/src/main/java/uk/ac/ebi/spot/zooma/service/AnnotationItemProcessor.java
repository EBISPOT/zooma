package uk.ac.ebi.spot.zooma.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import uk.ac.ebi.spot.zooma.model.SimpleAnnotation;

/**
 * Created by olgavrou on 13/01/2017.
 */
public class AnnotationItemProcessor implements ItemProcessor<SimpleAnnotation, SimpleAnnotation> {
    private final static Logger log = LoggerFactory.getLogger(AnnotationItemProcessor.class);

    @Override
    public SimpleAnnotation process(SimpleAnnotation simpleAnnotation) throws Exception {
        if(simpleAnnotation.getAnnotationdate().equals("ANNOTATION_DATE")){
            return null;
        }
        String annotationdate = simpleAnnotation.getAnnotationdate();
        if(annotationdate.contains("/")){
            String[] splitD = annotationdate.split("/");
            String time = splitD[2].split(" ")[1];
            splitD[2] = splitD[2].split(" ")[0];
            StringBuilder fixedDate = new StringBuilder();
            if (splitD[splitD.length - 1].length() == 2) {
                fixedDate.append(splitD[2] + "-").append(splitD[1] + "-").append("20").append(splitD[0] + " ").append(time).append(":00");;
            } else {
                fixedDate.append(splitD[2] + "-").append(splitD[1] + "-").append(splitD[0] + " ").append(time).append(":00");
            }
            annotationdate = fixedDate.toString();
        }

        final SimpleAnnotation completeAnnotation = SimpleAnnotation.builder()
                .bioentity(simpleAnnotation.getBioentity())
                .study(simpleAnnotation.getStudy())
                .propertytype(simpleAnnotation.getPropertytype())
                .propertyvalue(simpleAnnotation.getPropertyvalue())
                .semantictag(simpleAnnotation.getSemantictag())
                .annotator(simpleAnnotation.getAnnotator())
                .annotationdate(annotationdate)
                .evidence("MANUAL_CURATED")
                .accuracy("PRECISE")
                .generator("ZOOMA")
                .name("name")
                .topic("topic")
                .uri("uri")
                .build();
        log.trace("Completeing provenance for ");
        return completeAnnotation;
    }
}
