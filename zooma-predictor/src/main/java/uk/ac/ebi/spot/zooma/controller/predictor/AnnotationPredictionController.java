package uk.ac.ebi.spot.zooma.controller.predictor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageImpl;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedResources;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.web.PagedResourcesAssembler;
import uk.ac.ebi.spot.zooma.model.predictor.AnnotationPrediction;
import uk.ac.ebi.spot.zooma.model.predictor.Prediction;
import uk.ac.ebi.spot.zooma.service.predictor.AnnotationPredictionService;
import uk.ac.ebi.spot.zooma.utils.predictor.PredictorUtils;

import java.net.URISyntaxException;
import java.util.ArrayList;
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
    private String olsLocation;
    private List<String> dontSearchOrigin;

    @Autowired
    public AnnotationPredictionController(AnnotationPredictionService predictionService, @Value("${ols.term.location}") String olsTermLocation,
                                          @Value("${zooma.neo4j.location}") String zoomaNeo4jLocation,
                                          @Value("${ols.location}") String olsLocation) {
        this.predictionService = predictionService;
        this.olsTermLocation = olsTermLocation;
        this.zoomaNeo4jLocation = zoomaNeo4jLocation;
        this.olsLocation = olsLocation;

        this.dontSearchOrigin = new ArrayList<>();
        this.dontSearchOrigin.add("none");
    }

    @RequestMapping(value = "/predictions/annotate", params = {"q"}, method = RequestMethod.GET, produces="application/hal+json")
    public HttpEntity<PagedResources<AnnotationPrediction>> predictValue(@RequestParam(value = "q") String propertyValue,
                                                                         @RequestParam(value = "ontologies", required = false) List<String> ontologies,
                                                                         @RequestParam(value = "filter", required = false) boolean filter,
                                                                         @RequestParam(value = "onto_as_equals", required = false) boolean ontologiesAsEquals,
                                                                         PagedResourcesAssembler assembler) throws URISyntaxException {

        List<Prediction> predictions = predictionService.predictByPropertyValue(propertyValue, ontologies, filter);

        if(ontologiesAsEquals && PredictorUtils.shouldSearch(ontologies) && !undergoneOntologySearch(predictions)){
            predictions.addAll(predictionService.predictByPropertyValueOrigins(propertyValue, this.dontSearchOrigin, ontologies, filter));
        }

        for(Prediction prediction : predictions){
            AnnotationPrediction annotationPrediction = (AnnotationPrediction) prediction;
            annotationPrediction.add(linkTo(methodOn(AnnotationPredictionController.class).predictValue(propertyValue, ontologies, filter, ontologiesAsEquals, assembler)).withSelfRel());
            addLinks(annotationPrediction);
        }

        return new ResponseEntity<>(assembler.toResource(new PageImpl<>(predictions)), HttpStatus.OK);
    }

    @RequestMapping(value = "/predictions/annotate", params = {"q", "origins"}, method = RequestMethod.GET, produces="application/hal+json")
    public HttpEntity<PagedResources<AnnotationPrediction>> predictValueBoostSources(@RequestParam(value = "q") String propertyValue,
                                               @RequestParam(value = "origins") List<String> origins,
                                               @RequestParam(value = "ontologies", required = false) List<String> ontologies,
                                               @RequestParam(value = "filter", required = false) boolean filter,
                                               @RequestParam(value = "onto_as_equals", required = false) boolean ontologiesAsEquals,
                                               PagedResourcesAssembler assembler) throws URISyntaxException {

        List<Prediction> predictions = predictionService.predictByPropertyValueOrigins(propertyValue, origins, ontologies, filter);

        if(ontologiesAsEquals && PredictorUtils.shouldSearch(ontologies) && !undergoneOntologySearch(predictions)){
            predictions.addAll(predictionService.predictByPropertyValueOrigins(propertyValue, this.dontSearchOrigin, ontologies, filter));
        }

        Link link = linkTo(methodOn(AnnotationPredictionController.class).predictValueBoostSources(propertyValue, origins, ontologies, filter, ontologiesAsEquals, assembler)).withSelfRel();

        for(Prediction prediction : predictions){
            AnnotationPrediction annotationPrediction = (AnnotationPrediction) prediction;
            annotationPrediction.add(link);
            addLinks(annotationPrediction);
        }


        return new ResponseEntity<>(assembler.toResource(new PageImpl<>(predictions)), HttpStatus.OK);
    }

    @RequestMapping(value = "/predictions/annotate", params = {"type", "q"}, method = RequestMethod.GET, produces="application/hal+json")
    public HttpEntity<PagedResources<AnnotationPrediction>> predictTypeAndValue(@RequestParam(value = "type") String propertyType,
                                          @RequestParam(value = "q") String propertyValue,
                                          @RequestParam(value = "ontologies", required = false) List<String> ontologies,
                                          @RequestParam(value = "filter", required = false) boolean filter,
                                          @RequestParam(value = "onto_as_equals", required = false) boolean ontologiesAsEquals,
                                          PagedResourcesAssembler assembler) throws URISyntaxException {
        List<Prediction> predictions = predictionService.predictByPropertyTypeAndValue(propertyType, propertyValue, ontologies, filter);
        if (predictions.isEmpty()) {
            predictions = predictionService.predictByPropertyValue(propertyValue, ontologies, filter);
        }

        if (ontologiesAsEquals && PredictorUtils.shouldSearch(ontologies) && !undergoneOntologySearch(predictions)) {
            List<Prediction> olsPredictions;
            olsPredictions = predictionService.predictByPropertyTypeAndValueOrigins(propertyType, propertyValue, this.dontSearchOrigin, ontologies, filter);
            if (olsPredictions.isEmpty()) {
                olsPredictions = predictionService.predictByPropertyValueOrigins(propertyValue, this.dontSearchOrigin, ontologies, filter);
            }
            predictions.addAll(olsPredictions);
        }

        for (Prediction prediction : predictions) {
            AnnotationPrediction annotationPrediction = (AnnotationPrediction) prediction;
            annotationPrediction.add(linkTo(methodOn(AnnotationPredictionController.class).predictTypeAndValue(propertyType, propertyValue, ontologies, filter, ontologiesAsEquals, assembler)).withSelfRel());
            addLinks(annotationPrediction);
        }

        return new ResponseEntity<>(assembler.toResource(new PageImpl<>(predictions)), HttpStatus.OK);
    }

    @RequestMapping(value = "/predictions/annotate", params = {"type", "q", "origins"}, method = RequestMethod.GET, produces="application/hal+json")
    public HttpEntity<PagedResources<AnnotationPrediction>> predictTypeAndValueBoostSources(@RequestParam(value = "type") String propertyType,
                                                      @RequestParam(value = "q") String propertyValue,
                                                      @RequestParam(value = "origins") List<String> origins,
                                                      @RequestParam(value = "ontologies", required = false) List<String> ontologies,
                                                      @RequestParam(value = "filter", required = false) boolean filter,
                                                      @RequestParam(value = "onto_as_equals", required = false) boolean ontologiesAsEquals,
                                                      PagedResourcesAssembler assembler) throws URISyntaxException {

        List<Prediction> predictions = predictionService.predictByPropertyTypeAndValueOrigins(propertyType, propertyValue, origins, ontologies, filter);
        if (predictions.isEmpty()){
            predictions = predictionService.predictByPropertyValueOrigins(propertyValue, origins, ontologies, filter);
        }

        if(ontologiesAsEquals && PredictorUtils.shouldSearch(ontologies) && !undergoneOntologySearch(predictions)){
            List<Prediction> olsPredictions;
            olsPredictions = predictionService.predictByPropertyTypeAndValueOrigins(propertyType, propertyValue, this.dontSearchOrigin, ontologies, filter);
            if (olsPredictions.isEmpty()){
                olsPredictions = predictionService.predictByPropertyValueOrigins(propertyValue, this.dontSearchOrigin, ontologies, filter);
            }
            predictions.addAll(olsPredictions);
        }

        for(Prediction prediction : predictions){
            AnnotationPrediction annotationPrediction = (AnnotationPrediction) prediction;
            annotationPrediction.add(linkTo(methodOn(AnnotationPredictionController.class).predictTypeAndValueBoostSources(propertyType, propertyValue, origins, ontologies, filter, ontologiesAsEquals, assembler)).withSelfRel());
            addLinks(annotationPrediction);
        }

        return new ResponseEntity<>(assembler.toResource(new PageImpl<>(predictions)), HttpStatus.OK);
    }

//    @RequestMapping(value = "/predictions/upvote", params = {"prediction"}, method = RequestMethod.POST, produces = "application/hal+json")
//    public HttpEntity upvotePrediction(@RequestParam("prediction") AnnotationPrediction prediction,
//                                       PagedResourcesAssembler assembler) throws URISyntaxException {
//
//        return new ResponseEntity(HttpStatus.OK);
//    }


    private void addLinks(AnnotationPrediction prediction){
        for (String semTag : prediction.getSemanticTag()){
            Link link = new Link(olsTermLocation + semTag).withRel("ols");
            prediction.add(link);
        }
        if(!prediction.getStrongestMongoid().equals("ols")) {
            prediction.add(new Link(zoomaNeo4jLocation + "/annotations/search/findByMongoid?mongoid=" + prediction.getStrongestMongoid()).withRel("provenance"));

            prediction.add(new Link(zoomaNeo4jLocation + "/annotations/search/findByMongoid?mongoid=" + prediction.getStrongestMongoid()).withRel("derived.from"));
            for (String mongoid : prediction.getMongoid()) {
                prediction.add(new Link(zoomaNeo4jLocation + "/annotations/search/findByMongoid?mongoid=" + mongoid).withRel("derived.from"));
            }
        } else {
            prediction.add(new Link("http://" + olsLocation).withRel("provenance"));
            for (String semTag : prediction.getSemanticTag()){
                Link link = new Link(olsTermLocation + semTag).withRel("derived.from");
                prediction.add(link);
            }
        }
    }

    private boolean undergoneOntologySearch(List<Prediction> predictions) {
        for (Prediction prediction : predictions) {
            if (((AnnotationPrediction) prediction).getStrongestMongoid().equals("ols")) {
                return true;
            }
        }
        return false;
    }

}
