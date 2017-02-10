package uk.ac.ebi.spot.zooma.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryLinksResource;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.spot.zooma.model.*;

import java.util.*;

/**
 * Created by olgavrou on 20/01/2017.
 */
@RestController
@RequestMapping("/annotations")
@ExposesResourceFor(Annotation.class)
public class AnnotationController implements
        ResourceProcessor<RepositoryLinksResource> {

    @Autowired
    RestTemplate restTemplate;

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<?> add(@RequestBody Annotation annotation){
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity entity = new HttpEntity(annotation.toString(), httpHeaders);
        ResponseEntity responseEntity = restTemplate.postForEntity("http://localhost:8080/annotations", entity, Annotation.class);
        return ResponseEntity.created(responseEntity.getHeaders().getLocation()).build();
    }


    @RequestMapping(method = RequestMethod.GET)
    public List<Annotation> readAnnotations(){
        AnnotationResponse responseEntity = restTemplate.getForObject("http://localhost:8080/annotations", AnnotationResponse.class);
        return Arrays.asList(responseEntity.getEmbeddedAnnotations().getAnnotations());
    }

    @Override
    public RepositoryLinksResource process(RepositoryLinksResource resource) {
        resource.add(ControllerLinkBuilder.linkTo(Annotation.class).withRel("annotations"));
        return resource;
    }

}
