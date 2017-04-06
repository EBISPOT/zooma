package uk.ac.ebi.spot.zooma.messaging.neo4j;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ebi.spot.zooma.model.neo4j.*;
import uk.ac.ebi.spot.zooma.service.neo4j.AnnotationService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * RabbitMQ queue listener
 * Reads general Annotation message and converts it into a Neo {@link Annotation}
 * The Annotation is saved through the {@link Provenance} relationship that connects the Annotation with
 * It's {@link Source}.
 *
 * Created by olgavrou on 24/02/2017.
 */
public class AnnotationSubmissionReceiver {

    AnnotationService annotationService;

    ObjectMapper objectMapper;

    private final Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    @Autowired
    public AnnotationSubmissionReceiver(AnnotationService annotationService, ObjectMapper objectMapper) {
        this.annotationService = annotationService;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = "annotation.save.neo.queue")
    public void handleAnnotationSubmission(Message message) throws IOException {

        //read the message byte stream and convert to a HashMap
        Map<String, Object> propertiesMap = objectMapper.readValue(message.getBody(), new TypeReference<HashMap<String,Object>>() {});

        Provenance provenance = createGraphElements(propertiesMap);
        //will merge the whole graph through the relationship
        //default depth -1
        annotationService.save(provenance);
//        annotationService.save(neoAnnotation);

        getLog().info("Neo4j Queue: We have saved the annotation into Neo4j! Mongoid: " + propertiesMap.get("id"));
    }

    @RabbitListener(queues = "annotation.replace.neo.queue")
    public void handleAnnotationReplacement(Message message) throws IOException {
        //read the message byte stream and convert to a HashMap
        Map<String, Object> propertiesMap = objectMapper.readValue(message.getBody(), new TypeReference<HashMap<String,Object>>() {});

        Provenance provenance = createGraphElements(propertiesMap);
        Annotation neoAnnotation = provenance.getAnnotation();

        String replaces = "";
        if(propertiesMap.get("replaces") != null) {
            replaces = (String ) propertiesMap.get("replaces");
            Annotation annBeinReplaced = annotationService.findByMongoid(replaces);
            if (annBeinReplaced != null){
                neoAnnotation.setReplaces(annBeinReplaced);
            } else {
                getLog().error("Annotation {} replaced mongoid that doesn't exist in Neo: {} ", propertiesMap.get("mongoid"), replaces);
                return;
            }
        }
        provenance.setAnnotation(neoAnnotation);
        //will merge the whole graph through the relationship
        //default depth -1
        annotationService.save(provenance);

        getLog().info("Neo4j Queue: Annotation with mongoid: {} replaces annotation with mongoid: {} ", propertiesMap.get("id"), replaces);
    }

    private Provenance createGraphElements(Map<String, Object> propertiesMap) {
        //collect the properties from the Map and create the graph elements

        Collection<SemanticTag> semanticTags = new ArrayList();
        ArrayList<String> smTags = (ArrayList<String>) propertiesMap.get("semanticTag");
        for (String st: smTags){
            SemanticTag semanticTag = new SemanticTag();
            semanticTag.setSemanticTag(st);
            semanticTags.add(semanticTag);
        }

        Study study = new Study();
        study.setStudy((String) propertiesMap.get("study"));

        BiologicalEntity biologicalEntity = new BiologicalEntity();
        biologicalEntity.setBioEntity((String) propertiesMap.get("bioEntity"));
        biologicalEntity.setStudy(study);

        Property property = new Property();
        property.setPropertyType((String) propertiesMap.get("propertyType"));
        property.setPropertyValue((String) propertiesMap.get("propertyValue"));
        property.setBiologicalEntity(biologicalEntity);

        Source source = new Source();
        source.setName((String) propertiesMap.get("sourceName"));
        source.setTopic((String) propertiesMap.get("sourceTopic"));
        source.setType((String) propertiesMap.get("sourceType"));
        source.setUri((String) propertiesMap.get("sourceUri"));


        Annotation neoAnnotation = new Annotation();
        neoAnnotation.setBiologicalEntity(biologicalEntity);
        neoAnnotation.setProperty(property);
        neoAnnotation.setSemanticTag(semanticTags);
        neoAnnotation.setQuality((float) ((double)propertiesMap.get("quality")));
        neoAnnotation.setMongoid((String) propertiesMap.get("id"));

        Provenance provenance = new Provenance();
        provenance.setAccuracy((String) propertiesMap.get("accuracy"));
        provenance.setEvidence((String) propertiesMap.get("evidence"));
        provenance.setAnnotator((String) propertiesMap.get("annotator"));
        provenance.setAnnotatedDate((String) propertiesMap.get("annotatedDate"));
        provenance.setGeneratedDate((String) propertiesMap.get("generatedDate"));
        provenance.setGenerator((String) propertiesMap.get("generator"));

        provenance.setAnnotation(neoAnnotation);
        provenance.setSource(source);

        return provenance;
    }

}
