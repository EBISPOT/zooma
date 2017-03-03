package uk.ac.ebi.spot.zooma.messaging.solr;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.spot.zooma.model.solr.Annotation;
import uk.ac.ebi.spot.zooma.repository.solr.AnnotationRepository;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 09/01/17
 */
@Component
public class AnnotationSubmissionReceiver {

    @Autowired
    AnnotationRepository annotationRepository;

    @Autowired
    ObjectMapper objectMapper;

    private final Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    @RabbitListener(queues = "annotation.save.solr.queue")
    public void handleAnnotationSubmission(Message message) throws IOException {

        //read the message byte stream and convert to a HashMap
        Map<String, Object> propertiesMap = objectMapper.readValue(message.getBody(), new TypeReference<HashMap<String,Object>>() {});

        //source name field in Solr Annotation class is source
        propertiesMap.put("source", propertiesMap.get("sourceName"));
        //need to set the mongoid field
        propertiesMap.put("mongoid", propertiesMap.get("id"));

        Annotation annotation = objectMapper.convertValue(propertiesMap, Annotation.class);

        Annotation savedAnn = annotationRepository.save(annotation);

        getLog().info("Solr Queue: We have saved the annotation into Solr! " + propertiesMap.get("id"));
    }

}
