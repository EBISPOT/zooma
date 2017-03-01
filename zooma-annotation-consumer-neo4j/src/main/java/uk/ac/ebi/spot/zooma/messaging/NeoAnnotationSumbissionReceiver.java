package uk.ac.ebi.spot.zooma.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.zooma.model.mongo.Annotation;
import uk.ac.ebi.spot.zooma.model.neo4j.*;

import java.util.ArrayList;
import java.util.Collection;


/**
 * Created by olgavrou on 24/02/2017.
 */
@Component
public class NeoAnnotationSumbissionReceiver {

    @Autowired
    uk.ac.ebi.spot.zooma.repository.neo4j.AnnotationRepository annotationRepository;

    @Autowired
    uk.ac.ebi.spot.zooma.service.neo4j.AnnotationService annotationService;

    private final Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    @RabbitListener(queues = "annotation.save.neo.queue")
    public void handleAnnotationSubmission(Annotation annotation) {
        getLog().info("Neo4j Queue: We have detected that a new message was received! " + annotation.getId());

        Collection<SemanticTag> semanticTags = new ArrayList();
        for (String s : annotation.getSemanticTag()) {
            SemanticTag semanticTag = new SemanticTag();
            semanticTag.setSemanticTag(s);
            semanticTags.add(semanticTag);
        }

        Study stdy = new Study();
        stdy.setStudy(annotation.getBiologicalEntities().getStudies().getStudy());
        BiologicalEntity biologicalEntity = new BiologicalEntity();
        biologicalEntity.setBioEntity(annotation.getBiologicalEntities().getBioEntity());
        biologicalEntity.setStudy(stdy);

        Property property = new Property();
        property.setPropertyType(annotation.getProperty().getPropertyType());
        property.setPropertyValue(annotation.getProperty().getPropertyValue());
        property.setBiologicalEntity(biologicalEntity);

        Source db = new Source();
        db.setName(annotation.getProvenance().getSource().getName());
        db.setTopic(annotation.getProvenance().getSource().getTopic());
        db.setType(annotation.getProvenance().getSource().getType().toString());
        db.setUri(annotation.getProvenance().getSource().getUri());

        AnnotationProvenance provenance = new AnnotationProvenance();
        provenance.setAccuracy(annotation.getProvenance().getAccuracy().toString());
        provenance.setEvidence(annotation.getProvenance().getEvidence().toString());
        provenance.setAnnotator(annotation.getProvenance().getAnnotator());
        provenance.setAnnotationDate(annotation.getProvenance().getAnnotationDate().toString());
        provenance.setGeneratedDate(annotation.getProvenance().getGeneratedDate().toString());
        provenance.setGenerator(annotation.getProvenance().getGenerator());
        provenance.setSource(db);

        uk.ac.ebi.spot.zooma.model.neo4j.Annotation neoAnnotation = new uk.ac.ebi.spot.zooma.model.neo4j.Annotation();
        neoAnnotation.setBiologicalEntity(biologicalEntity);
        neoAnnotation.setProperty(property);
        neoAnnotation.setSemanticTag(semanticTags);
        neoAnnotation.setProvenance(provenance);
        neoAnnotation.setBatchLoad(annotation.isBatchLoad());
        neoAnnotation.setQuality(annotation.getQuality());
        neoAnnotation.setMongoId(annotation.getId());

        annotationService.save(neoAnnotation);

        getLog().info("Neo4j Queue: We have saved the annotation into Neo4j! " + annotation.getId());
    }
}
