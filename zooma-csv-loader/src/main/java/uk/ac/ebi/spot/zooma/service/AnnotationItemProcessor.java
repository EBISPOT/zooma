package uk.ac.ebi.spot.zooma.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import uk.ac.ebi.spot.zooma.model.SimpleAnnotation;

/**
 * Created by olgavrou on 13/01/2017.
 */
public class AnnotationItemProcessor implements ItemProcessor<SimpleAnnotation, SimpleAnnotation> {

    private final static Logger log = LoggerFactory.getLogger(AnnotationItemProcessor.class);

    @Value("${name}")
    private String name;

    @Value("${uri}")
    private String uri;

    @Value("${topic}")
    private String topic;

    @Override
    public SimpleAnnotation process(SimpleAnnotation simpleAnnotation) throws Exception {
        if(simpleAnnotation.getSemantictag().equals("SEMANTIC_TAG")){
            return null;
        }

        if(simpleAnnotation.getAnnotationid() == null){
            simpleAnnotation.setAnnotationid("");
        }
        String evidence = "MANUAL_CURATED";
        if(simpleAnnotation.getStudy() == null || simpleAnnotation.getStudy().isEmpty()
                || simpleAnnotation.getBioentity() == null || simpleAnnotation.getBioentity().isEmpty()){
            if(simpleAnnotation.getAnnotator() == null || simpleAnnotation.getAnnotator().isEmpty()) {
                evidence = "NO_EVIDENCE";
            }
        } else if (simpleAnnotation.getAnnotator() == null || simpleAnnotation.getAnnotator().isEmpty()){
            evidence = "SUBMITTER_PROVIDED";
        }
        final SimpleAnnotation completeAnnotation = SimpleAnnotation.builder()
                .bioentity(simpleAnnotation.getBioentity())
                .study(simpleAnnotation.getStudy())
                .propertytype(simpleAnnotation.getPropertytype())
                .propertyvalue(simpleAnnotation.getPropertyvalue())
                .semantictag(simpleAnnotation.getSemantictag())
                .annotator(simpleAnnotation.getAnnotator())
                .annotationdate(simpleAnnotation.getAnnotationdate())
                .evidence(evidence)
                .accuracy("PRECISE")
                .generator("ZOOMA")
                .name(name)
                .topic(topic)
                .uri(uri)
                .annotationid(simpleAnnotation.getAnnotationid())
                .build();
        log.trace("Completeing provenance for ");
        return completeAnnotation;
    }
}
