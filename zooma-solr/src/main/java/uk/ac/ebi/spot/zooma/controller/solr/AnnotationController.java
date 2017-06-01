package uk.ac.ebi.spot.zooma.controller.solr;

import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.data.rest.webmvc.RepositoryLinksResource;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.*;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ebi.spot.zooma.model.solr.Annotation;
import uk.ac.ebi.spot.zooma.service.solr.AnnotationRepositoryServiceRead;

import java.io.IOException;
import java.util.List;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

/**
 * Created by olgavrou on 26/04/2017.
 */
@RestController
@ExposesResourceFor(Annotation.class)
@RequestMapping("/annotations")
public class AnnotationController implements
        ResourceProcessor<RepositoryLinksResource> {

    private AnnotationRepositoryServiceRead repositoryServiceRead;

    @Autowired
    public AnnotationController(AnnotationRepositoryServiceRead repositoryServiceRead) {
        this.repositoryServiceRead = repositoryServiceRead;
    }

    @RestResource
    @RequestMapping(value = "/search", params = {"q"}, method = RequestMethod.GET, produces="application/hal+json")
    public HttpEntity<PagedResources<Annotation>> findByPropertyValue(@RequestParam(value = "q") String propertyValue,
                                                                                   PagedResourcesAssembler assembler, Pageable pageable) throws IOException, SolrServerException {
        Page<Annotation> annotations = repositoryServiceRead.findByPropertyValue(propertyValue, pageable);
//        for(Annotation annotation : annotations){
//            annotation.add(linkTo(methodOn(AnnotationController.class).findByPropertyValue(propertyValue, assembler, pageable)).withSelfRel());
//        }
        PagedResources<Annotation> resources = assembler.toResource(annotations, linkTo(methodOn(AnnotationController.class).findByPropertyValue(propertyValue, assembler, pageable)).withSelfRel());
        return new ResponseEntity<>(assembler.toResource(annotations), HttpStatus.OK);
    }

    @RestResource
    @RequestMapping(value = "/search", params = {"q", "origins"}, method = RequestMethod.GET, produces="application/hal+json")
    public HttpEntity<PagedResources<Annotation>> findByPropertyValueFilterSources(@RequestParam(value = "q") String propertyValue,
                                                                                   @RequestParam(value = "origins", required = false) List<String> origins,
                                                                      PagedResourcesAssembler assembler, Pageable pageable) throws IOException, SolrServerException {
        Page<Annotation> annotations = repositoryServiceRead.findByPropertyValue(propertyValue, origins, pageable);
//        for(Annotation annotation : annotations){
//            annotation.add(linkTo(methodOn(AnnotationController.class).findByPropertyValueFilterSources(propertyValue, sources, assembler, pageable)).withSelfRel());
//        }
        return new ResponseEntity<>(assembler.toResource(annotations), HttpStatus.OK);
    }


    @RestResource
    @RequestMapping(value = "/search", params = {"type", "q"}, method = RequestMethod.GET, produces="application/hal+json")
    public HttpEntity<PagedResources<Annotation>> findByPropertyTypeAndValue(@RequestParam(value = "type") String propertyType,
                                                                                          @RequestParam(value = "q") String propertyValue,
                                                                                          PagedResourcesAssembler assembler, Pageable pageable) throws IOException, SolrServerException {
        Page<Annotation> annotations = repositoryServiceRead.findByPropertyTypeAndPropertyValue(propertyType, propertyValue, pageable);
//        for(Annotation annotation : annotations){
//            annotation.add(linkTo(methodOn(AnnotationController.class).findByPropertyTypeAndValue(propertyType, propertyValue, assembler, pageable)).withSelfRel());
//        }
        return new ResponseEntity<>(assembler.toResource(annotations), HttpStatus.OK);
    }

    @RestResource
    @RequestMapping(value = "/search", params = {"type", "q", "origins"}, method = RequestMethod.GET, produces="application/hal+json")
    public HttpEntity<PagedResources<Annotation>> findByPropertyTypeAndValueFilterSources(@RequestParam(value = "type") String propertyType,
                                                                                          @RequestParam(value = "q") String propertyValue,
                                                                                   @RequestParam(value = "origins", required = false) List<String> origins,
                                                                                   PagedResourcesAssembler assembler, Pageable pageable) throws IOException, SolrServerException {
        Page<Annotation> annotations = repositoryServiceRead.findByPropertyTypeAndPropertyValue(propertyType, propertyValue, origins, pageable);
//        for(Annotation annotation : annotations){
//            annotation.add(linkTo(methodOn(AnnotationController.class).findByPropertyTypeAndValueFilterSources(propertyType, propertyValue, sources, assembler, pageable)).withSelfRel());
//        }
        PagedResources<Annotation> resources = assembler.toResource(annotations);



        return new ResponseEntity<>(resources, HttpStatus.OK);
    }


    @Override
    public RepositoryLinksResource process(RepositoryLinksResource resource) {
        resource.add(ControllerLinkBuilder.linkTo(AnnotationController.class).withRel("annotations"));

        return resource;
    }
}
