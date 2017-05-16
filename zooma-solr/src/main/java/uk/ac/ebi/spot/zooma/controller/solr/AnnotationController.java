package uk.ac.ebi.spot.zooma.controller.solr;

import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
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

/**
 * Created by olgavrou on 26/04/2017.
 */
@RestController
public class AnnotationController {

    private AnnotationRepositoryServiceRead repositoryServiceRead;

    @Autowired
    public AnnotationController(AnnotationRepositoryServiceRead repositoryServiceRead) {
        this.repositoryServiceRead = repositoryServiceRead;
    }

    @RestResource
    @RequestMapping(value = "/annotations/search", params = {"q"}, method = RequestMethod.GET, produces="application/hal+json")
    public HttpEntity<PagedResources<Annotation>> findByPropertyValue(@RequestParam(value = "q") String propertyValue,
                                                                                   PagedResourcesAssembler assembler, Pageable pageable) throws IOException, SolrServerException {
        Page<Annotation> annotations = repositoryServiceRead.findByPropertyValue(propertyValue, pageable);
        return new ResponseEntity<>(assembler.toResource(annotations), HttpStatus.OK);
    }

    @RestResource
    @RequestMapping(value = "/annotations/search", params = {"q", "sources"}, method = RequestMethod.GET, produces="application/hal+json")
    public HttpEntity<PagedResources<Annotation>> findByPropertyValueFilterSources(@RequestParam(value = "q") String propertyValue,
                                                                                   @RequestParam(value = "sources", required = false) List<String> sources,
                                                                      PagedResourcesAssembler assembler, Pageable pageable) throws IOException, SolrServerException {
        Page<Annotation> annotations = repositoryServiceRead.findByPropertyValue(propertyValue, sources, "source", pageable);
        return new ResponseEntity<>(assembler.toResource(annotations), HttpStatus.OK);
    }

    @RequestMapping(value = "/annotations/search", params = {"q", "topics"}, method = RequestMethod.GET, produces="application/hal+json")
    public HttpEntity<PagedResources<Annotation>> findByPropertyValueFilterTopics(@RequestParam(value = "q") String propertyValue,
                                                                                   @RequestParam(value = "topics", required = false) List<String> topics,
                                                                                   PagedResourcesAssembler assembler, Pageable pageable) throws IOException, SolrServerException {
        Page<Annotation> annotations = repositoryServiceRead.findByPropertyValue(propertyValue, topics, "topic", pageable);
        return new ResponseEntity<>(assembler.toResource(annotations), HttpStatus.OK);
    }

    @RestResource
    @RequestMapping(value = "/annotations/search", params = {"type", "q"}, method = RequestMethod.GET, produces="application/hal+json")
    public HttpEntity<PagedResources<Annotation>> findByPropertyTypeAndValue(@RequestParam(value = "type") String propertyType,
                                                                                          @RequestParam(value = "q") String propertyValue,
                                                                                          PagedResourcesAssembler assembler, Pageable pageable) throws IOException, SolrServerException {
        Page<Annotation> annotations = repositoryServiceRead.findByPropertyTypeAndPropertyValue(propertyType, propertyValue, pageable);
        return new ResponseEntity<>(assembler.toResource(annotations), HttpStatus.OK);
    }

    @RestResource
    @RequestMapping(value = "/annotations/search", params = {"type", "q", "sources"}, method = RequestMethod.GET, produces="application/hal+json")
    public HttpEntity<PagedResources<Annotation>> findByPropertyTypeAndValueFilterSources(@RequestParam(value = "type") String propertyType,
                                                                                          @RequestParam(value = "q") String propertyValue,
                                                                                   @RequestParam(value = "sources", required = false) List<String> sources,
                                                                                   PagedResourcesAssembler assembler, Pageable pageable) throws IOException, SolrServerException {
        Page<Annotation> annotations = repositoryServiceRead.findByPropertyTypeAndPropertyValue(propertyType, propertyValue, sources, "source", pageable);
        return new ResponseEntity<>(assembler.toResource(annotations), HttpStatus.OK);
    }

    @RestResource
    @RequestMapping(value = "/annotations/search", params = {"type", "q", "topics"}, method = RequestMethod.GET, produces="application/hal+json")
    public HttpEntity<PagedResources<Annotation>> findByPropertyTypeAndValueFilterTopics(@RequestParam(value = "type") String propertyType,
                                                                                          @RequestParam(value = "q") String propertyValue,
                                                                                          @RequestParam(value = "topics", required = false) List<String> topics,
                                                                                          PagedResourcesAssembler assembler, Pageable pageable) throws IOException, SolrServerException {
        Page<Annotation> annotations = repositoryServiceRead.findByPropertyTypeAndPropertyValue(propertyType, propertyValue, topics, "topic", pageable);
        return new ResponseEntity<>(assembler.toResource(annotations), HttpStatus.OK);
    }
}
