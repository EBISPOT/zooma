package uk.ac.ebi.spot.zooma.service.predictor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.zooma.engine.PredictionSearch;
import uk.ac.ebi.spot.zooma.engine.decorators.PredictionSearchBoostOriginDecorator;
import uk.ac.ebi.spot.zooma.model.predictor.Prediction;
import uk.ac.ebi.spot.zooma.scorers.AbstractConfidenceCalculator;
import uk.ac.ebi.spot.zooma.utils.predictor.PredictorUtils;

import java.net.URISyntaxException;
import java.util.*;

/**
 * Created by olgavrou on 31/10/2016.
 */
@Service
public class AnnotationPredictionService {

    private PredictionSearch simplePredictionSearch;
    private PredictionSearch olsPredictionSearch;
    private AbstractConfidenceCalculator confidenceCalculator;


    @Autowired
    public AnnotationPredictionService(@Qualifier("simple.delegate") PredictionSearch simplePredictionSearch,
                                       @Qualifier("ols.delegate") PredictionSearch olsPredictionSearch,
                                       AbstractConfidenceCalculator confidenceCalculator) {
        this.simplePredictionSearch = new PredictionSearchBoostOriginDecorator(
                        simplePredictionSearch);

        this.olsPredictionSearch = new PredictionSearchBoostOriginDecorator(
                        olsPredictionSearch);

        this.confidenceCalculator = confidenceCalculator;

    }

    private final Logger log = LoggerFactory.getLogger(getClass());
    protected Logger getLog() {
        return log;
    }


    public List<Prediction> predictByPropertyValue(String propertyValue, List<String> ontologies, boolean filter) throws URISyntaxException {
        populateOntologies(ontologies);
        List<Prediction> results = this.simplePredictionSearch.search(propertyValue);
        if(results.isEmpty() && PredictorUtils.shouldSearch(ontologies)){
            results = this.olsPredictionSearch.searchWithOrigin(propertyValue, ontologies, filter);
        }
        results = this.confidenceCalculator.calculateFinalScore(results, propertyValue);
        return this.confidenceCalculator.setConfidence(results);
    }

    public List<Prediction> predictByPropertyValueOrigins(String propertyValue, List<String> origins, List<String> ontologies, boolean filter) throws URISyntaxException {
        ontologies = populateOntologies(ontologies);
        List<Prediction> results = this.simplePredictionSearch.searchWithOrigin(propertyValue, origins, filter);
        if(results.isEmpty() && PredictorUtils.shouldSearch(ontologies)){
            results = this.olsPredictionSearch.searchWithOrigin(propertyValue, ontologies, filter);
        }

        results = this.confidenceCalculator.calculateFinalScore(results, propertyValue);
        return this.confidenceCalculator.setConfidence(results);
    }

    public List<Prediction> predictByPropertyTypeAndValue(String propertyType, String propertyValue, List<String> ontologies, boolean filter) throws URISyntaxException {
        populateOntologies(ontologies);
        List<Prediction> results = this.simplePredictionSearch.search(propertyType, propertyValue);
        if(results.isEmpty() && PredictorUtils.shouldSearch(ontologies)){
            results = this.olsPredictionSearch.searchWithOrigin(propertyType, propertyValue, ontologies, filter);
        }

        results = this.confidenceCalculator.calculateFinalScore(results, propertyValue);
        return this.confidenceCalculator.setConfidence(results);
    }

    public List<Prediction> predictByPropertyTypeAndValueOrigins(String propertyType, String propertyValue, List<String> origins, List<String> ontologies, boolean filter) throws URISyntaxException {
        ontologies = populateOntologies(ontologies);
        List<Prediction> results = this.simplePredictionSearch.searchWithOrigin(propertyType, propertyValue, origins, filter);
        if(results.isEmpty() && PredictorUtils.shouldSearch(ontologies)){
            results = this.olsPredictionSearch.searchWithOrigin(propertyType, propertyValue, ontologies, filter);
        }
        results = this.confidenceCalculator.calculateFinalScore(results, propertyValue);
        return this.confidenceCalculator.setConfidence(results);
    }

    private List<String> populateOntologies(List<String> ontologies) {
        if (ontologies == null){
            ontologies = new ArrayList<>();
        }
        return ontologies;
    }

}
