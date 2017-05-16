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

        int end = (20) > predictions.size() ? predictions.size() : (20);
        Page<AnnotationPrediction> page = new PageImpl<>(predictions.subList(0, end), new PageRequest(0, 20), predictions.size());

        return new ResponseEntity<>(assembler.toResource(page), HttpStatus.OK);
    }

    @RequestMapping(value = "/predictions/annotate", params = {"q", "sources"}, method = RequestMethod.GET, produces="application/hal+json")
    public HttpEntity predictValueSources(@RequestParam(value = "q") String propertyValue,
                                             @RequestParam(value = "sources") List<String> sources,
                                   PagedResourcesAssembler assembler) throws URISyntaxException {
        List<AnnotationPrediction> predictions = predictionService.predictByPropertyValueWithFilter(propertyValue, sources, "sources", false);
        for(AnnotationPrediction prediction : predictions){
            prediction.add(linkTo(methodOn(AnnotationPredictionController.class).predictValueSources(propertyValue, sources, assembler)).withSelfRel());
            addLinks(prediction);
        }

        int end = (20) > predictions.size() ? predictions.size() : (20);
        Page<AnnotationPrediction> page = new PageImpl<>(predictions.subList(0, end), new PageRequest(0, 20), predictions.size());

        return new ResponseEntity<>(assembler.toResource(page), HttpStatus.OK);
    }

    @RequestMapping(value = "/predictions/annotate", params = {"q", "topics"}, method = RequestMethod.GET, produces="application/hal+json")
    public HttpEntity predictValueTopics(@RequestParam(value = "q") String propertyValue,
                                   @RequestParam(value = "topics") List<String> topics,
                                   PagedResourcesAssembler assembler) throws URISyntaxException {
        List<AnnotationPrediction> predictions = predictionService.predictByPropertyValueWithFilter(propertyValue, topics, "topics", false);
        for(AnnotationPrediction prediction : predictions){
            prediction.add(linkTo(methodOn(AnnotationPredictionController.class).predictValueTopics(propertyValue, topics, assembler)).withSelfRel());
            addLinks(prediction);
        }

        int end = (20) > predictions.size() ? predictions.size() : (20);
        Page<AnnotationPrediction> page = new PageImpl<>(predictions.subList(0, end), new PageRequest(0, 20), predictions.size());

        return new ResponseEntity<>(assembler.toResource(page), HttpStatus.OK);
    }

    @RequestMapping(value = "/predictions/annotate", params = {"q", "sources", "exclusive"}, method = RequestMethod.GET, produces="application/hal+json")
    public HttpEntity predictValueSourcesExclusive(@RequestParam(value = "q") String propertyValue,
                                   @RequestParam(value = "sources") List<String> sources,
                                   @RequestParam(value = "exclusive") boolean exclusive,
                                   PagedResourcesAssembler assembler) throws URISyntaxException {

        List<AnnotationPrediction> predictions = predictionService.predictByPropertyValueWithFilter(propertyValue, sources, "sources", exclusive);
        for(AnnotationPrediction prediction : predictions){
            prediction.add(linkTo(methodOn(AnnotationPredictionController.class).predictValueSourcesExclusive(propertyValue, sources, exclusive, assembler)).withSelfRel());
            addLinks(prediction);
        }

        int end = (20) > predictions.size() ? predictions.size() : (20);
        Page<AnnotationPrediction> page = new PageImpl<>(predictions.subList(0, end), new PageRequest(0, 20), predictions.size());

        return new ResponseEntity<>(assembler.toResource(page), HttpStatus.OK);
    }

    @RequestMapping(value = "/predictions/annotate", params = {"q", "topics", "exclusive"}, method = RequestMethod.GET, produces="application/hal+json")
    public HttpEntity predictValueTopicsExclusive(@RequestParam(value = "q") String propertyValue,
                                                   @RequestParam(value = "topics") List<String> topics,
                                                   @RequestParam(value = "exclusive") boolean exclusive,
                                                   PagedResourcesAssembler assembler) throws URISyntaxException {

        List<AnnotationPrediction> predictions = predictionService.predictByPropertyValueWithFilter(propertyValue, topics, "topics", exclusive);
        for(AnnotationPrediction prediction : predictions){
            prediction.add(linkTo(methodOn(AnnotationPredictionController.class).predictValueTopicsExclusive(propertyValue, topics, exclusive, assembler)).withSelfRel());
            addLinks(prediction);
        }

        int end = (20) > predictions.size() ? predictions.size() : (20);
        Page<AnnotationPrediction> page = new PageImpl<>(predictions.subList(0, end), new PageRequest(0, 20), predictions.size());

        return new ResponseEntity<>(assembler.toResource(page), HttpStatus.OK);
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

        int end = (20) > predictions.size() ? predictions.size() : (20);
        Page<AnnotationPrediction> page = new PageImpl<>(predictions.subList(0, end), new PageRequest(0, 20), predictions.size());

        return new ResponseEntity<>(assembler.toResource(page), HttpStatus.OK);
    }

    @RequestMapping(value = "/predictions/annotate", params = {"type", "q", "sources"}, method = RequestMethod.GET, produces="application/hal+json")
    public HttpEntity predictTypeAndValueSources(@RequestParam(value = "type") String propertyType,
                                          @RequestParam(value = "q") String propertyValue,
                                          @RequestParam(value = "sources") List<String> sources,
                              PagedResourcesAssembler assembler) throws URISyntaxException {
        List<AnnotationPrediction> predictions = predictionService.predictByPropertyTypeAndValueWithFilter(propertyType, propertyValue, sources, "sources", false);
        if (predictions.isEmpty()){
            predictions = predictionService.predictByPropertyValueWithFilter(propertyValue, sources, "sources",false);
        }
        for(AnnotationPrediction prediction : predictions){
            prediction.add(linkTo(methodOn(AnnotationPredictionController.class).predictTypeAndValueSources(propertyType, propertyValue, sources, assembler)).withSelfRel());
            addLinks(prediction);
        }

        int end = (20) > predictions.size() ? predictions.size() : (20);
        Page<AnnotationPrediction> page = new PageImpl<>(predictions.subList(0, end), new PageRequest(0, 20), predictions.size());

        return new ResponseEntity<>(assembler.toResource(page), HttpStatus.OK);
    }

    @RequestMapping(value = "/predictions/annotate", params = {"type", "q", "topics"}, method = RequestMethod.GET, produces="application/hal+json")
    public HttpEntity predictTypeAndValueTopics(@RequestParam(value = "type") String propertyType,
                                                 @RequestParam(value = "q") String propertyValue,
                                                 @RequestParam(value = "topics") List<String> topics,
                                                 PagedResourcesAssembler assembler) throws URISyntaxException {
        List<AnnotationPrediction> predictions = predictionService.predictByPropertyTypeAndValueWithFilter(propertyType, propertyValue, topics, "sources", false);
        if (predictions.isEmpty()){
            predictions = predictionService.predictByPropertyValueWithFilter(propertyValue, topics, "topics",false);
        }
        for(AnnotationPrediction prediction : predictions){
            prediction.add(linkTo(methodOn(AnnotationPredictionController.class).predictTypeAndValueTopics(propertyType, propertyValue, topics, assembler)).withSelfRel());
            addLinks(prediction);
        }

        int end = (20) > predictions.size() ? predictions.size() : (20);
        Page<AnnotationPrediction> page = new PageImpl<>(predictions.subList(0, end), new PageRequest(0, 20), predictions.size());

        return new ResponseEntity<>(assembler.toResource(page), HttpStatus.OK);
    }

    @RequestMapping(value = "/predictions/annotate", params = {"type", "q", "sources", "exclusive"}, method = RequestMethod.GET, produces="application/hal+json")
    public HttpEntity predictTypeAndValueSourcesExclusive(@RequestParam(value = "type") String propertyType,
                                          @RequestParam(value = "q") String propertyValue,
                                          @RequestParam(value = "sources") List<String> sources,
                                          @RequestParam(value = "exclusive") boolean exclusive,
                                          PagedResourcesAssembler assembler) throws URISyntaxException {
        List<AnnotationPrediction> predictions = predictionService.predictByPropertyTypeAndValueWithFilter(propertyType, propertyValue, sources, "sources", exclusive);
        if (predictions.isEmpty()){
            predictions = predictionService.predictByPropertyValueWithFilter(propertyValue, sources, "sources", false);
        }
        for(AnnotationPrediction prediction : predictions){
            prediction.add(linkTo(methodOn(AnnotationPredictionController.class).predictTypeAndValueSourcesExclusive(propertyType, propertyValue, sources, exclusive, assembler)).withSelfRel());
            addLinks(prediction);
        }

        int end = (20) > predictions.size() ? predictions.size() : (20);
        Page<AnnotationPrediction> page = new PageImpl<>(predictions.subList(0, end), new PageRequest(0, 20), predictions.size());

        return new ResponseEntity<>(assembler.toResource(page), HttpStatus.OK);
    }

    @RequestMapping(value = "/predictions/annotate", params = {"type", "q", "topics", "exclusive"}, method = RequestMethod.GET, produces="application/hal+json")
    public HttpEntity predictTypeAndValueTopicsExclusive(@RequestParam(value = "type") String propertyType,
                                                          @RequestParam(value = "q") String propertyValue,
                                                          @RequestParam(value = "topics") List<String> topics,
                                                          @RequestParam(value = "exclusive") boolean exclusive,
                                                          PagedResourcesAssembler assembler) throws URISyntaxException {
        List<AnnotationPrediction> predictions = predictionService.predictByPropertyTypeAndValueWithFilter(propertyType, propertyValue, topics, "topics", exclusive);
        if (predictions.isEmpty()){
            predictions = predictionService.predictByPropertyValueWithFilter(propertyValue, topics, "topics", false);
        }
        for(AnnotationPrediction prediction : predictions){
            prediction.add(linkTo(methodOn(AnnotationPredictionController.class).predictTypeAndValueTopicsExclusive(propertyType, propertyValue, topics, exclusive, assembler)).withSelfRel());
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
