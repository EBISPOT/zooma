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
    @RequestMapping(value = "/annotations/search/findByPropertyValue", method = RequestMethod.GET, produces="application/hal+json")
    public HttpEntity<PagedResources<Annotation>> findByPropertyValueFilterSources(@RequestParam(value = "propertyValue") String propertyValue,
                                                                                   @RequestParam(value = "filter", required = false) List<String> sources,
                                                                      PagedResourcesAssembler assembler, Pageable pageable) throws IOException, SolrServerException {
        Page<Annotation> annotations = repositoryServiceRead.findByPropertyValue(propertyValue, sources, pageable);
        return new ResponseEntity<>(assembler.toResource(annotations), HttpStatus.OK);
    }

    @RestResource
    @RequestMapping(value = "/annotations/search/findByPropertyTypeAndValue", method = RequestMethod.GET, produces="application/hal+json")
    public HttpEntity<PagedResources<Annotation>> findByPropertyTypeAndValueFilterSources(@RequestParam(value = "propertyType") String propertyType,
                                                                                          @RequestParam(value = "propertyValue") String propertyValue,
                                                                                   @RequestParam(value = "filter", required = false) List<String> sources,
                                                                                   PagedResourcesAssembler assembler, Pageable pageable) throws IOException, SolrServerException {
        Page<Annotation> annotations = repositoryServiceRead.findByPropertyTypeAndPropertyValue(propertyType, propertyValue, sources, pageable);
        return new ResponseEntity<>(assembler.toResource(annotations), HttpStatus.OK);
    }
}
