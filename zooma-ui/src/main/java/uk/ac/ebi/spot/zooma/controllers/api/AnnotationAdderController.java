package uk.ac.ebi.spot.zooma.controllers.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryLinksResource;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.ac.ebi.spot.zooma.model.*;
import uk.ac.ebi.spot.zooma.model.api.AnnotationProvenance;
import uk.ac.ebi.spot.zooma.model.mongo.MongoAnnotation;
import uk.ac.ebi.spot.zooma.repository.mongo.AnnotationRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;


/**
 * Created by olgavrou on 03/01/2017.
 */
@Controller
@RequestMapping("/api/mongoannotations")
@ExposesResourceFor(MongoAnnotation.class)
public class AnnotationAdderController implements ResourceProcessor<RepositoryLinksResource> {

    @Autowired
    private AnnotationRepository annotationRepository;

    @Override
    public RepositoryLinksResource process(RepositoryLinksResource resource) {
        resource.add(ControllerLinkBuilder.linkTo(AnnotationAdderController.class).withRel("addannotation"));
        return resource;
    }

    @RequestMapping(path = "/find", produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.GET)
    HttpEntity<PagedResources<MongoAnnotation>> findAnnotations(
            @RequestParam(value = "propertyValue") String propertyValue,
            PagedResourcesAssembler assembler
    ) throws ResourceNotFoundException {

      return null;
    }


    @RequestMapping(path = "/add", produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.POST)
    @ResponseBody
    HttpEntity<String> addAnnotationToMongo(
            @RequestParam(value = "propertyValue") String propertyValue,
            @RequestParam(value = "propertyType") String propertyType,
            @RequestParam(value = "semanticTag") String semanticTags,
            @RequestParam(value = "beName") String beName,
            @RequestParam(value = "beURI", required = false) String beURI,
            @RequestParam(value = "study") String accession,
            @RequestParam(value = "studyURI", required = false) String studyURI,
            @RequestParam(value = "sourceName") String source,
            @RequestParam(value = "sourceURI") String uri,
            @RequestParam(value = "annotator") String annotator,
            PagedResourcesAssembler assembler
    ) throws ResourceNotFoundException {

        Study study = new Study(accession);
        if (studyURI != null){
            study.setStudyUri(studyURI);
        }
        BiologicalEntity biologicalEntity = new BiologicalEntity(beName, study);
        if (beURI != null){
            biologicalEntity.setBioEntityUri(beURI);
        }

        TypedProperty property;
        if(propertyType != null) {
            property = new TypedProperty(propertyType, propertyValue);
        } else {
            property = new TypedProperty("", propertyValue);
        }
        Collection<String> semTags = new ArrayList<>();
        if(semanticTags.contains(",")){
            for(String semTag : semanticTags.split(",")){
                semTags.add(semTag);
            }
        }
        else {
            semTags.add(semanticTags);
        }

        LocalDateTime generatedDate = LocalDateTime.now();
        MongoAnnotationProvenance annotationProvenance = new MongoAnnotationProvenance(new DatabaseAnnotationSource(uri, source, ""),
                AnnotationProvenance.Evidence.MANUAL_CURATED,
                AnnotationProvenance.Accuracy.PRECISE,
                "ZOOMA",
                annotator, generatedDate);
        MongoAnnotation mongoAnnotation = new MongoAnnotation(biologicalEntity, property, semTags, annotationProvenance, true);

        annotationRepository.save(mongoAnnotation);

        return new ResponseEntity<>( "New annotation saved successfully into mongodb", HttpStatus.OK);
    }
}
