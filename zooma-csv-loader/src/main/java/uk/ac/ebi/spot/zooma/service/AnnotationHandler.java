package uk.ac.ebi.spot.zooma.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.ac.ebi.spot.zooma.model.SimpleAnnotation;

import java.util.Optional;

/**
 * Created by olgavrou on 24/03/2017.
 */
@Component
public class AnnotationHandler {
    private RestTemplate restTemplate;

    private HttpHeaders httpHeaders;

    private final Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    private final String location = "http://localhost:8081/annotations/";
    private ObjectMapper objectMapper;

    @Autowired
    public AnnotationHandler(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.httpHeaders = new HttpHeaders();
        this.httpHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
        this.objectMapper = new ObjectMapper();
    }


    void postNewAnnotation(SimpleAnnotation simpleAnnotation) {
        if(!annotationExists(simpleAnnotation)) {
            HttpEntity entity = new HttpEntity(simpleAnnotation.toString(), httpHeaders);
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(location, entity, String.class);
            if (responseEntity.getStatusCode().equals(HttpStatus.CREATED)) {
                simpleAnnotation.setAction(SimpleAnnotation.Action.CREATED);
            } else {
                simpleAnnotation.setAction(SimpleAnnotation.Action.UNKNOWN);
                getLog().debug("Annotation {} could not be created: {}", simpleAnnotation, responseEntity.getStatusCode());
            }
        } else {
            simpleAnnotation.setAction(SimpleAnnotation.Action.ALREADY_EXISTS);
        }
    }

    void updateOldAnnotation(SimpleAnnotation simpleAnnotation) {
        if(!annotationExists(simpleAnnotation)) {
            HttpEntity entity = new HttpEntity(simpleAnnotation.toString(), httpHeaders);
            String annotationid = simpleAnnotation.getAnnotationid();
            if (annotationExists(annotationid)) {
                restTemplate.put(location + annotationid, entity);
                simpleAnnotation.setAction(SimpleAnnotation.Action.REPLACED);
            } else {
                simpleAnnotation.setAction(SimpleAnnotation.Action.UNKNOWN);
                getLog().info("Annotation can't be updated. Id: {} doesn't exist!", annotationid);
            }
        } else {
            simpleAnnotation.setAction(SimpleAnnotation.Action.ALREADY_EXISTS);
        }
    }

    boolean annotationExists(String id){
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(location + id, String.class);
        if (responseEntity.getStatusCode() == HttpStatus.NOT_FOUND
                || responseEntity.getBody() == null){
            return false;
        }
        return true;
    }

    boolean annotationExists(SimpleAnnotation simpleAnnotation){
        ResponseEntity<String> responseEntity = queryForAnnotation(simpleAnnotation);

        if (responseEntity.getStatusCode() != HttpStatus.OK){
            return false; //TODO: maybe throw error
        }

        if (responseEntity.getBody() == null){
            return false;
        }
        return true;
    }

    public Optional<String> getAnnotationId(SimpleAnnotation simpleAnnotation) {
        try {
            ResponseEntity<String> responseEntity = queryForAnnotation(simpleAnnotation);
            if (responseEntity.getBody() != null) {
                JSONObject object = this.objectMapper.convertValue(responseEntity.getBody(), JSONObject.class);
                String id = null;

                id = (String) object.get("mongoId");

                if (id != null) {
                    return Optional.of(id);
                }
            }
        } catch (JSONException e) {
            getLog().debug("mongoId field doesn't exist: " + simpleAnnotation);
            return Optional.empty();
        }
        return Optional.empty();
    }

    ResponseEntity<String> queryForAnnotation(SimpleAnnotation simpleAnnotation){
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(location + "search/findByAnnotation")
                .queryParam("bioEntity", simpleAnnotation.getBioentity())
                .queryParam("study", simpleAnnotation.getStudy())
                .queryParam("propertyType", simpleAnnotation.getPropertytype())
                .queryParam("propertyValue", simpleAnnotation.getPropertyvalue())
                .queryParam("semanticTag", simpleAnnotation.getSemantictag())
                .queryParam("annotator", simpleAnnotation.getAnnotator())
                .queryParam("annotatedDate", simpleAnnotation.getAnnotationdate())
                .queryParam("evidence", simpleAnnotation.getEvidence())
                .queryParam("sourceUri", simpleAnnotation.getUri());
        HttpEntity getEntity = new HttpEntity(httpHeaders);

        return restTemplate.exchange(builder.build().encode().toUri(),
                HttpMethod.GET, getEntity, String.class);
    }

}
