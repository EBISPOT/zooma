package uk.ac.ebi.spot.zooma.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.RepositoryLinksResource;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.spot.zooma.model.*;

/**
 * Created by olgavrou on 20/01/2017.
 */
@RestController
@RequestMapping("/annotations")
@ExposesResourceFor(BaseAnnotation.class)
public class AnnotationController implements
        ResourceProcessor<RepositoryLinksResource> {

    @Autowired
    RestTemplate restTemplate;

    public static class AnnotationPagedResources extends PagedResources<BaseAnnotation> {

    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<?> add(@RequestBody BaseAnnotation annotation){
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity entity = new HttpEntity(annotation.toString(), httpHeaders);
        ResponseEntity responseEntity = restTemplate.postForEntity("http://localhost:8080/annotations", entity, BaseAnnotation.class);
        return ResponseEntity.created(responseEntity.getHeaders().getLocation()).build();
    }


    @RequestMapping(method = RequestMethod.GET)
    public PagedResources<BaseAnnotation> findAll(){
        PagedResources<BaseAnnotation> resources = restTemplate.getForObject("http://localhost:8080/annotations", AnnotationPagedResources.class);
        return resources;
    }
//
//    @RequestMapping(path = "", produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.GET)
//    PagedResources<BaseAnnotation> getAnnotations(
//            @PageableDefault(size = 20, page = 0) Pageable pageable,
//            PagedResourcesAssembler assembler
//    ) throws ResourceNotFoundException {
//        PagedResources<BaseAnnotation> document = restTemplate.getForObject("http://localhost:8080/annotations", AnnotationPagedResources.class);
//        return document;
//    }

//    @RequestMapping(method = RequestMethod.GET)
//    public List<BaseAnnotation> readAnnotations(){
//        AnnotationResponse responseEntity = restTemplate.getForObject("http://localhost:8080/annotations", AnnotationResponse.class);
//        return Arrays.asList(responseEntity.getEmbeddedAnnotations().getAnnotations());
//    }


    @RequestMapping(method = RequestMethod.GET, value = "/{mongoId}")
    public BaseAnnotation getAnnotation(@PathVariable String mongoId){
        BaseAnnotation annotation = restTemplate.getForObject("http://localhost:8080/annotations/" + mongoId, BaseAnnotation.class);
        return annotation;
    }

    @Override
    public RepositoryLinksResource process(RepositoryLinksResource resource) {
        resource.add(ControllerLinkBuilder.linkTo(BaseAnnotation.class).withRel("annotations"));
        return resource;
    }

}
