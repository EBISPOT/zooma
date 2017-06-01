package uk.ac.ebi.spot.zooma.service.predictor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.zooma.engine.PredictionSearch;
import uk.ac.ebi.spot.zooma.engine.decorators.PredictionSearchBoostOriginDecorator;
import uk.ac.ebi.spot.zooma.model.predictor.AnnotationPrediction;
import uk.ac.ebi.spot.zooma.utils.predictor.PredictorConfidenceCalculator;
import uk.ac.ebi.spot.zooma.utils.predictor.PredictorUtils;

import java.net.URISyntaxException;
import java.util.*;

/**
 * Created by olgavrou on 31/10/2016.
 */
@Service
public class AnnotationPredictionService {

    private float cutoffScore;
    private float cutoffPercentage;

    private PredictionSearch simplePredictionSearch;
    private PredictionSearch olsPredictionSearch;
    private PredictorConfidenceCalculator confidenceCalculator;



    @Autowired
    public AnnotationPredictionService(@Value("${cutoff.score}") float cutoffScore,
                                       @Value("${cutoff.percentage}") float cutoffPercentage,
                                       @Qualifier("simple.delegate") PredictionSearch simplePredictionSearch,
                                       @Qualifier("ols.delegate") PredictionSearch olsPredictionSearch,
                                       PredictorConfidenceCalculator confidenceCalculator) {
        this.cutoffScore = cutoffScore;
        this.cutoffPercentage = cutoffPercentage;
        this.simplePredictionSearch = new PredictionSearchBoostOriginDecorator(
                        simplePredictionSearch);

        this.olsPredictionSearch = new PredictionSearchBoostOriginDecorator(
                        olsPredictionSearch);

        this.confidenceCalculator = confidenceCalculator;
    }

    public float getCutoffScore() {
        return cutoffScore;
    }
    public float getCutoffPercentage() {
        return cutoffPercentage;
    }
    private final Logger log = LoggerFactory.getLogger(getClass());
    protected Logger getLog() {
        return log;
    }


    public List<AnnotationPrediction> predictByPropertyValue(String propertyValue, List<String> ontologies, boolean filter) throws URISyntaxException {
        populateOntologies(ontologies);
        List<AnnotationPrediction> results = this.simplePredictionSearch.search(propertyValue);
        if(results.isEmpty() && PredictorUtils.shouldSearch(ontologies)){
            results = this.olsPredictionSearch.searchWithOrigin(propertyValue, ontologies, filter);
        }

        return this.confidenceCalculator.calculateConfidence(results, propertyValue, getCutoffPercentage(), getCutoffScore());
    }

    public List<AnnotationPrediction> predictByPropertyValueOrigins(String propertyValue, List<String> origins, List<String> ontologies, boolean filter) throws URISyntaxException {
        ontologies = populateOntologies(ontologies);
        List<AnnotationPrediction> results = this.simplePredictionSearch.searchWithOrigin(propertyValue, origins, filter);
        if(results.isEmpty() && PredictorUtils.shouldSearch(ontologies)){
            results = this.olsPredictionSearch.searchWithOrigin(propertyValue, ontologies, filter);
        }

        return this.confidenceCalculator.calculateConfidence(results, propertyValue, getCutoffPercentage(), getCutoffScore());
    }

    public List<AnnotationPrediction> predictByPropertyTypeAndValue(String propertyType, String propertyValue, List<String> ontologies, boolean filter) throws URISyntaxException {
        populateOntologies(ontologies);
        List<AnnotationPrediction> results = this.simplePredictionSearch.search(propertyType, propertyValue);
        if(results.isEmpty() && PredictorUtils.shouldSearch(ontologies)){
            results = this.olsPredictionSearch.searchWithOrigin(propertyType, propertyValue, ontologies, filter);
        }

        return this.confidenceCalculator.calculateConfidence(results, propertyValue, getCutoffPercentage(), getCutoffScore());
    }

    public List<AnnotationPrediction> predictByPropertyTypeAndValueOrigins(String propertyType, String propertyValue, List<String> origins, List<String> ontologies, boolean filter) throws URISyntaxException {
        ontologies = populateOntologies(ontologies);
        List<AnnotationPrediction> results = this.simplePredictionSearch.searchWithOrigin(propertyType, propertyValue, origins, filter);
        if(results.isEmpty() && PredictorUtils.shouldSearch(ontologies)){
            results = this.olsPredictionSearch.searchWithOrigin(propertyType, propertyValue, ontologies, filter);
        }
        return this.confidenceCalculator.calculateConfidence(results, propertyValue, getCutoffPercentage(), getCutoffScore());
    }

    private List<String> populateOntologies(List<String> ontologies) {
        if (ontologies == null){
            ontologies = new ArrayList<>();
        }
        return ontologies;
    }

}
