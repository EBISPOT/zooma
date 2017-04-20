package uk.ac.ebi.spot.zooma.messaging.solr;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ebi.spot.zooma.model.solr.Annotation;
import uk.ac.ebi.spot.zooma.service.solr.AnnotationRepositoryServiceRead;
import uk.ac.ebi.spot.zooma.service.solr.AnnotationRepositoryServiceWrite;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 09/01/17
 */
public class AnnotationSubmissionReceiver {

    AnnotationRepositoryServiceWrite summaryRepositoryService;

    ObjectMapper objectMapper;

    private final Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    @Autowired
    public AnnotationSubmissionReceiver(AnnotationRepositoryServiceWrite summaryRepositoryService, ObjectMapper objectMapper) {
        this.summaryRepositoryService = summaryRepositoryService;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = "annotation.save.solr.queue")
    public void handleAnnotationSubmission(Message message) throws IOException {

        Annotation summary = convertToAnnotationSummary(message);

        summaryRepositoryService.save(summary);

//        getLog().info("Solr Queue: We have saved the annotation into Solr! " + propertiesMap.get("id"));
    }

    @RabbitListener(queues = "annotation.replace.solr.queue")
    public void handleAnnotationReplacement(Message message) throws IOException {

        Annotation summary = convertToAnnotationSummary(message);
        Map<String, Object> propertiesMap = objectMapper.readValue(message.getBody(), new TypeReference<HashMap<String,Object>>() {});
        String replaces = (String) propertiesMap.get("replaces");

        summaryRepositoryService.replace(summary, replaces);
    }

    private Annotation convertToAnnotationSummary(Message message) throws IOException {
        //read the message byte stream and convert to a HashMap
        Map<String, Object> propertiesMap = objectMapper.readValue(message.getBody(), new TypeReference<HashMap<String,Object>>() {});

        //source name field in Solr Annotation class, is source
        Collection<String> source = new ArrayList<>();
        source.add((String) propertiesMap.get("sourceName"));
        propertiesMap.put("source", source);

        //need to set the mongoid field
        Collection<String> mongoid = new ArrayList<>();
        mongoid.add((String) propertiesMap.get("id"));
        propertiesMap.put("mongoid", mongoid);

        propertiesMap.put("strongestMongoid", propertiesMap.get("id"));
        propertiesMap.put("votes", 1);
        propertiesMap.put("sourceNum", 1);
        propertiesMap.put("id", null);

        propertiesMap.put("lastModified", propertiesMap.get("generatedDate"));

        return objectMapper.convertValue(propertiesMap, Annotation.class);
    }

}
