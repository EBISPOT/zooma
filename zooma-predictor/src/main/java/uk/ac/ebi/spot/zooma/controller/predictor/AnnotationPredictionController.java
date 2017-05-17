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

    @RequestMapping(value = "/predictions/annotate", params = {"q"}, method = RequestMethod.GET, produces="application/hal+json")
    public HttpEntity predictValue(@RequestParam(value = "q") String propertyValue,
                              PagedResourcesAssembler assembler) throws URISyntaxException {
        List<AnnotationPrediction> predictions = predictionService.predictByPropertyValue(propertyValue);
        for(AnnotationPrediction prediction : predictions){
            prediction.add(linkTo(methodOn(AnnotationPredictionController.class).predictValue(propertyValue, assembler)).withSelfRel());
            addLinks(prediction);
        }

        return new ResponseEntity<>(assembler.toResource(listToPage(predictions)), HttpStatus.OK);
    }

    @RequestMapping(value = "/predictions/annotate", params = {"q", "sources"}, method = RequestMethod.GET, produces="application/hal+json")
    public HttpEntity predictValueBoostSources(@RequestParam(value = "q") String propertyValue,
                                               @RequestParam(value = "sources") List<String> sources,
                                               PagedResourcesAssembler assembler) throws URISyntaxException {
        List<AnnotationPrediction> predictions = predictionService.predictByPropertyValueBoostSources(propertyValue, sources);
        for(AnnotationPrediction prediction : predictions){
            prediction.add(linkTo(methodOn(AnnotationPredictionController.class).predictValueBoostSources(propertyValue, sources, assembler)).withSelfRel());
            addLinks(prediction);
        }
        return new ResponseEntity<>(assembler.toResource(listToPage(predictions)), HttpStatus.OK);
    }


    @RequestMapping(value = "/predictions/annotate", params = {"q", "topics"}, method = RequestMethod.GET, produces="application/hal+json")
    public HttpEntity predictValueBoostTopics(@RequestParam(value = "q") String propertyValue,
                                              @RequestParam(value = "topics") List<String> topics,
                                              PagedResourcesAssembler assembler) throws URISyntaxException {
        List<AnnotationPrediction> predictions = predictionService.predictByPropertyValueBoostTopics(propertyValue, topics);
        for(AnnotationPrediction prediction : predictions){
            prediction.add(linkTo(methodOn(AnnotationPredictionController.class).predictValueBoostTopics(propertyValue, topics, assembler)).withSelfRel());
            addLinks(prediction);
        }
        return new ResponseEntity<>(assembler.toResource(listToPage(predictions)), HttpStatus.OK);
    }

    @RequestMapping(value = "/predictions/annotate", params = {"q", "sources", "filter"}, method = RequestMethod.GET, produces="application/hal+json")
    public HttpEntity predictValueFilterSources(@RequestParam(value = "q") String propertyValue,
                                                @RequestParam(value = "sources") List<String> sources,
                                                @RequestParam(value = "filter") boolean filter,
                                                PagedResourcesAssembler assembler) throws URISyntaxException {
        List<AnnotationPrediction> predictions;
        if(filter) {
            predictions = predictionService.predictByPropertyValueFilterSources(propertyValue, sources);
        } else {
            predictions = predictionService.predictByPropertyValueBoostSources(propertyValue, sources);
        }
        for(AnnotationPrediction prediction : predictions){
            prediction.add(linkTo(methodOn(AnnotationPredictionController.class).predictValueFilterSources(propertyValue, sources, filter, assembler)).withSelfRel());
            addLinks(prediction);
        }
        return new ResponseEntity<>(assembler.toResource(listToPage(predictions)), HttpStatus.OK);
    }

    @RequestMapping(value = "/predictions/annotate", params = {"q", "topics", "filter"}, method = RequestMethod.GET, produces="application/hal+json")
    public HttpEntity predictValueFilterTopics(@RequestParam(value = "q") String propertyValue,
                                               @RequestParam(value = "topics") List<String> topics,
                                               @RequestParam(value = "filter") boolean filter,
                                               PagedResourcesAssembler assembler) throws URISyntaxException {
        List<AnnotationPrediction> predictions;
        if(filter) {
            predictions = predictionService.predictByPropertyValueFilterTopics(propertyValue, topics);
        } else {
            predictions = predictionService.predictByPropertyValueBoostTopics(propertyValue, topics);
        }
        for(AnnotationPrediction prediction : predictions){
            prediction.add(linkTo(methodOn(AnnotationPredictionController.class).predictValueFilterTopics(propertyValue, topics, filter, assembler)).withSelfRel());
            addLinks(prediction);
        }
        return new ResponseEntity<>(assembler.toResource(listToPage(predictions)), HttpStatus.OK);
    }

    @RequestMapping(value = "/predictions/annotate", params = {"type", "q"}, method = RequestMethod.GET, produces="application/hal+json")
    public HttpEntity predictTypeAndValue(@RequestParam(value = "type") String propertyType,
                                          @RequestParam(value = "q") String propertyValue,
                                          PagedResourcesAssembler assembler) throws URISyntaxException {
        List<AnnotationPrediction> predictions = predictionService.predictByPropertyTypeAndValue(propertyType, propertyValue);
        if (predictions.isEmpty()){
            predictions = predictionService.predictByPropertyValue(propertyValue);
        }
        for(AnnotationPrediction prediction : predictions){
            prediction.add(linkTo(methodOn(AnnotationPredictionController.class).predictTypeAndValue(propertyType, propertyValue, assembler)).withSelfRel());
            addLinks(prediction);
        }
        return new ResponseEntity<>(assembler.toResource(listToPage(predictions)), HttpStatus.OK);
    }

    @RequestMapping(value = "/predictions/annotate", params = {"type", "q", "sources"}, method = RequestMethod.GET, produces="application/hal+json")
    public HttpEntity predictTypeAndValueBoostSources(@RequestParam(value = "type") String propertyType,
                                                      @RequestParam(value = "q") String propertyValue,
                                                      @RequestParam(value = "sources") List<String> sources,
                                                      PagedResourcesAssembler assembler) throws URISyntaxException {
        List<AnnotationPrediction> predictions = predictionService.predictByPropertyTypeAndValueBoostSources(propertyType, propertyValue, sources);
        if (predictions.isEmpty()){
            predictions = predictionService.predictByPropertyValueBoostSources(propertyValue, sources);
        }
        for(AnnotationPrediction prediction : predictions){
            prediction.add(linkTo(methodOn(AnnotationPredictionController.class).predictTypeAndValueBoostSources(propertyType, propertyValue, sources, assembler)).withSelfRel());
            addLinks(prediction);
        }
        return new ResponseEntity<>(assembler.toResource(listToPage(predictions)), HttpStatus.OK);
    }

    @RequestMapping(value = "/predictions/annotate", params = {"type", "q", "topics"}, method = RequestMethod.GET, produces="application/hal+json")
    public HttpEntity predictTypeAndValueBoostTopics(@RequestParam(value = "type") String propertyType,
                                                     @RequestParam(value = "q") String propertyValue,
                                                     @RequestParam(value = "topics") List<String> topics,
                                                     PagedResourcesAssembler assembler) throws URISyntaxException {
        List<AnnotationPrediction> predictions = predictionService.predictByPropertyTypeAndValueBoostTopics(propertyType, propertyValue, topics);
        if (predictions.isEmpty()){
            predictions = predictionService.predictByPropertyValueBoostTopics(propertyValue, topics);
        }
        for(AnnotationPrediction prediction : predictions){
            prediction.add(linkTo(methodOn(AnnotationPredictionController.class).predictTypeAndValueBoostTopics(propertyType, propertyValue, topics, assembler)).withSelfRel());
            addLinks(prediction);
        }
        return new ResponseEntity<>(assembler.toResource(listToPage(predictions)), HttpStatus.OK);
    }

    @RequestMapping(value = "/predictions/annotate", params = {"type", "q", "sources", "filter"}, method = RequestMethod.GET, produces="application/hal+json")
    public HttpEntity predictTypeAndValueFilterSources(@RequestParam(value = "type") String propertyType,
                                                       @RequestParam(value = "q") String propertyValue,
                                                       @RequestParam(value = "sources") List<String> sources,
                                                       @RequestParam(value = "filter") boolean filter,
                                                       PagedResourcesAssembler assembler) throws URISyntaxException {
        List<AnnotationPrediction> predictions;
        if(filter) {
            predictions = predictionService.predictByPropertyTypeAndValueFilterSources(propertyType, propertyValue, sources);
            if (predictions.isEmpty()) {
                predictions = predictionService.predictByPropertyValueFilterSources(propertyValue, sources);
            }
        } else {
            predictions = predictionService.predictByPropertyTypeAndValueBoostSources(propertyType, propertyValue, sources);
            if (predictions.isEmpty()) {
                predictions = predictionService.predictByPropertyValueBoostSources(propertyValue, sources);
            }
        }

        for(AnnotationPrediction prediction : predictions){
            prediction.add(linkTo(methodOn(AnnotationPredictionController.class).predictTypeAndValueFilterSources(propertyType, propertyValue, sources, filter, assembler)).withSelfRel());
            addLinks(prediction);
        }
        return new ResponseEntity<>(assembler.toResource(listToPage(predictions)), HttpStatus.OK);
    }

    @RequestMapping(value = "/predictions/annotate", params = {"type", "q", "topics", "filter"}, method = RequestMethod.GET, produces="application/hal+json")
    public HttpEntity predictTypeAndValueFilterTopics(@RequestParam(value = "type") String propertyType,
                                                      @RequestParam(value = "q") String propertyValue,
                                                      @RequestParam(value = "topics") List<String> topics,
                                                      @RequestParam(value = "filter") boolean filter,
                                                      PagedResourcesAssembler assembler) throws URISyntaxException {
        List<AnnotationPrediction> predictions;
        if(filter) {
            predictions = predictionService.predictByPropertyTypeAndValueFilterTopics(propertyType, propertyValue, topics);
            if (predictions.isEmpty()) {
                predictions = predictionService.predictByPropertyValueFilterTopics(propertyValue, topics);
            }
        } else {
            predictions = predictionService.predictByPropertyTypeAndValueBoostTopics(propertyType, propertyValue, topics);
            if (predictions.isEmpty()) {
                predictions = predictionService.predictByPropertyValueBoostTopics(propertyValue, topics);
            }
        }

        for(AnnotationPrediction prediction : predictions){
            prediction.add(linkTo(methodOn(AnnotationPredictionController.class).predictTypeAndValueFilterTopics(propertyType, propertyValue, topics, filter, assembler)).withSelfRel());
            addLinks(prediction);
        }
        return new ResponseEntity<>(assembler.toResource(listToPage(predictions)), HttpStatus.OK);
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

    private Page<AnnotationPrediction> listToPage(List<AnnotationPrediction> predictions) {
        int end = (20) > predictions.size() ? predictions.size() : (20);
        return new PageImpl<>(predictions.subList(0, end), new PageRequest(0, 20), predictions.size());
    }

}
