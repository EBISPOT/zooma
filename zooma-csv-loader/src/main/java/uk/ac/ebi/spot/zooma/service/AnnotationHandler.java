package uk.ac.ebi.spot.zooma.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
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

    private final String location = "http://localhost:8080/annotations/";
    // private final String location = "http://scrappy:8080/annotations/";
    private ObjectMapper objectMapper;

    @Autowired
    public AnnotationHandler(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.httpHeaders = new HttpHeaders();
        this.httpHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
        this.objectMapper = new ObjectMapper();
    }


    void postNewAnnotation(SimpleAnnotation simpleAnnotation) {
        HttpEntity entity = new HttpEntity(simpleAnnotation.toString(), httpHeaders);
        ResponseEntity<String> responseEntity;
        try{
            responseEntity = restTemplate.postForEntity(location, entity, String.class);
            if (responseEntity.getStatusCode().equals(HttpStatus.CREATED)) {
                simpleAnnotation.setAction(SimpleAnnotation.Action.CREATED);
            }
        } catch (RestClientException e){
            if (((HttpClientErrorException) e).getStatusCode().equals(HttpStatus.CONFLICT)) {
                simpleAnnotation.setAction(SimpleAnnotation.Action.ALREADY_EXISTS);
            }
            getLog().debug(e.getMessage());
        }
        if(simpleAnnotation.getAction() == null){
            simpleAnnotation.setAction(SimpleAnnotation.Action.UNKNOWN);
        }
    }

    void updateOldAnnotation(SimpleAnnotation simpleAnnotation) {
        if(annotationByIdExists(simpleAnnotation.getAnnotationid())) {
            try {
                HttpEntity entity = new HttpEntity(simpleAnnotation.toString(), httpHeaders);
                String annotationid = simpleAnnotation.getAnnotationid();
                restTemplate.put(location + annotationid, entity);
                simpleAnnotation.setAction(SimpleAnnotation.Action.REPLACED);
            } catch (RestClientException e){
                if (((HttpClientErrorException) e).getStatusCode().equals(HttpStatus.CONFLICT)){
                    simpleAnnotation.setAction(SimpleAnnotation.Action.ALREADY_EXISTS);
                }
                getLog().debug(e.getMessage());
            }
        }
        if(simpleAnnotation.getAction() == null){
            simpleAnnotation.setAction(SimpleAnnotation.Action.UNKNOWN);
        }
    }

    boolean annotationByIdExists(String id){
        try {
            ResponseEntity<String> responseEntity = restTemplate.getForEntity(location + id, String.class);
            if (responseEntity.getStatusCode().equals(HttpStatus.OK)
                    && responseEntity.getBody() != null) {
                return true;
            }
        } catch (RestClientException e){

        }
        return false;
    }


    public Optional<String> getAnnotationId(SimpleAnnotation simpleAnnotation) {
        try {
            ResponseEntity<String> responseEntity = queryForAnnotation(simpleAnnotation);
            if (responseEntity.getStatusCode().equals(HttpStatus.OK)
                    && responseEntity.getBody() != null) {
                JSONObject object = this.objectMapper.convertValue(responseEntity.getBody(), JSONObject.class);
                String id = (String) object.get("mongoid");
                if (id != null) {
                    return Optional.of(id);
                }
            } else {
                getLog().debug("Response not OK! Status Code: " + responseEntity.getStatusCode() + " , Response Body: " + responseEntity.getBody());
            }
        } catch (JSONException e) {
            getLog().debug("mongoid field doesn't exist: " + simpleAnnotation);
        } catch (RestClientException e){
            getLog().debug(e.getMessage());
            return Optional.empty();
        }
        return Optional.empty();
    }

    ResponseEntity<String> queryForAnnotation(SimpleAnnotation simpleAnnotation){

        String semTag = simpleAnnotation.getSemantictag();
        semTag = semTag.replace("|",",");

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(location + "search/findByAnnotation")
                .queryParam("bioEntity", simpleAnnotation.getBioentity())
                .queryParam("study", simpleAnnotation.getStudy())
                .queryParam("propertyType", simpleAnnotation.getPropertytype())
                .queryParam("propertyValue", simpleAnnotation.getPropertyvalue())
                .queryParam("semanticTag", semTag)
                .queryParam("annotator", simpleAnnotation.getAnnotator())
                .queryParam("annotatedDate", simpleAnnotation.getAnnotationdate())
                .queryParam("evidence", simpleAnnotation.getEvidence())
                .queryParam("sourceUri", simpleAnnotation.getUri());
        HttpEntity getEntity = new HttpEntity(httpHeaders);

        return restTemplate.exchange(builder.build().encode().toUri(),
                HttpMethod.GET, getEntity, String.class);
    }

}
