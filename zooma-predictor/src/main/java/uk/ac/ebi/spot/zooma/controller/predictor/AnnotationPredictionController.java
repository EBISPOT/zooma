package uk.ac.ebi.spot.zooma.controller.predictor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.web.PagedResourcesAssembler;
import uk.ac.ebi.spot.zooma.model.predictor.AnnotationPrediction;
import uk.ac.ebi.spot.zooma.service.predictor.AnnotationPredictionService;

import java.net.URISyntaxException;
import java.util.List;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

/**
 * Created by olgavrou on 26/04/2017.
 */
@RestController
public class AnnotationPredictionController {

    private AnnotationPredictionService predictionService;
    private String olsTermLocation;
    private String zoomaNeo4jLocation;

    @Autowired
    public AnnotationPredictionController(AnnotationPredictionService predictionService, @Value("${ols.term.location}") String olsTermLocation,
                                          @Value("${zooma.neo4j.location}") String zoomaNeo4jLocation) {
        this.predictionService = predictionService;
        this.olsTermLocation = olsTermLocation;
        this.zoomaNeo4jLocation = zoomaNeo4jLocation;
    }

    @RequestMapping(value = "/predictions/annotate", params = {"propertyValue"}, method = RequestMethod.GET, produces="application/hal+json")
    public HttpEntity predictValue(@RequestParam(value = "propertyValue") String propertyValue,
                              PagedResourcesAssembler assembler) throws URISyntaxException {
        List<AnnotationPrediction> predictions = predictionService.predictByPropertyValue(propertyValue);
        for(AnnotationPrediction prediction : predictions){
            prediction.add(linkTo(methodOn(AnnotationPredictionController.class).predictValue(propertyValue, assembler)).withSelfRel());
            addLinks(prediction);
        }

        int end = (20) > predictions.size() ? predictions.size() : (20);
        Page<AnnotationPrediction> page = new PageImpl<>(predictions.subList(0, end), new PageRequest(0, 20), predictions.size());

        return new ResponseEntity<>(assembler.toResource(page), HttpStatus.OK);
    }

    @RequestMapping(value = "/predictions/annotate", params = {"propertyValue", "filter"}, method = RequestMethod.GET, produces="application/hal+json")
    public HttpEntity predictValue(@RequestParam(value = "propertyValue") String propertyValue,
                                             @RequestParam(value = "filter") List<String> sources,
                                   PagedResourcesAssembler assembler) throws URISyntaxException {
        List<AnnotationPrediction> predictions = predictionService.predictByPropertyValueWithFilter(propertyValue, sources);
        for(AnnotationPrediction prediction : predictions){
            prediction.add(linkTo(methodOn(AnnotationPredictionController.class).predictValue(propertyValue, sources, assembler)).withSelfRel());
            addLinks(prediction);
        }

        int end = (20) > predictions.size() ? predictions.size() : (20);
        Page<AnnotationPrediction> page = new PageImpl<>(predictions.subList(0, end), new PageRequest(0, 20), predictions.size());

        return new ResponseEntity<>(assembler.toResource(page), HttpStatus.OK);
    }

    @RequestMapping(value = "/predictions/annotate", params = {"propertyType", "propertyValue"}, method = RequestMethod.GET, produces="application/hal+json")
    public HttpEntity predictTypeAndValue(@RequestParam(value = "propertyType") String propertyType,
                                          @RequestParam(value = "propertyValue") String propertyValue,
                                          PagedResourcesAssembler assembler) throws URISyntaxException {
        List<AnnotationPrediction> predictions = predictionService.predictByPropertyTypeAndValue(propertyType, propertyValue);
        if (predictions.isEmpty()){
            predictions = predictionService.predictByPropertyValue(propertyValue);
        }
        for(AnnotationPrediction prediction : predictions){
            prediction.add(linkTo(methodOn(AnnotationPredictionController.class).predictTypeAndValue(propertyType, propertyValue, assembler)).withSelfRel());
            addLinks(prediction);
        }

        int end = (20) > predictions.size() ? predictions.size() : (20);
        Page<AnnotationPrediction> page = new PageImpl<>(predictions.subList(0, end), new PageRequest(0, 20), predictions.size());

        return new ResponseEntity<>(assembler.toResource(page), HttpStatus.OK);
    }

    @RequestMapping(value = "/predictions/annotate", params = {"propertyType", "propertyValue", "filter"}, method = RequestMethod.GET, produces="application/hal+json")
    public HttpEntity predictTypeAndValue(@RequestParam(value = "propertyType") String propertyType,
                                          @RequestParam(value = "propertyValue") String propertyValue,
                                          @RequestParam(value = "filter") List<String> sources,
                              PagedResourcesAssembler assembler) throws URISyntaxException {
        List<AnnotationPrediction> predictions = predictionService.predictByPropertyTypeAndValueWithFilter(propertyType, propertyValue, sources);
        if (predictions.isEmpty()){
            predictions = predictionService.predictByPropertyValueWithFilter(propertyValue, sources);
        }
        for(AnnotationPrediction prediction : predictions){
            prediction.add(linkTo(methodOn(AnnotationPredictionController.class).predictTypeAndValue(propertyType, propertyValue, sources, assembler)).withSelfRel());
            addLinks(prediction);
        }

        int end = (20) > predictions.size() ? predictions.size() : (20);
        Page<AnnotationPrediction> page = new PageImpl<>(predictions.subList(0, end), new PageRequest(0, 20), predictions.size());

        return new ResponseEntity<>(assembler.toResource(page), HttpStatus.OK);
    }

    private void addLinks(AnnotationPrediction prediction){
        for (String semTag : prediction.getSemanticTag()){
            Link link = new Link(olsTermLocation + semTag).withRel("ols");
            prediction.add(link);
        }
        prediction.add(new Link(zoomaNeo4jLocation + "/annotations/search/findByMongoid?mongoid=" + prediction.getStrongestMongoid()).withRel("provenance"));

        prediction.add(new Link(zoomaNeo4jLocation + "/annotations/search/findByMongoid?mongoid=" + prediction.getStrongestMongoid()).withRel("derived.from"));
        for (String mongoid : prediction.getMongoid()){
            prediction.add(new Link(zoomaNeo4jLocation + "/annotations/search/findByMongoid?mongoid=" + mongoid).withRel("derived.from"));
        }
    }

}
