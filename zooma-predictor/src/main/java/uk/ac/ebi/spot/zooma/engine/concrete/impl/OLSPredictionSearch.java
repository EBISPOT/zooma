package uk.ac.ebi.spot.zooma.engine.concrete.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.ac.ebi.pride.utilities.ols.web.service.model.Term;
import uk.ac.ebi.spot.zooma.engine.PredictionSearch;
import uk.ac.ebi.spot.zooma.engine.ols.OLS2PredictionTransformer;
import uk.ac.ebi.spot.zooma.engine.ols.OLSSearchService;
import uk.ac.ebi.spot.zooma.model.predictor.Prediction;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by olgavrou on 18/05/2017.
 */
@Component("ols.delegate")
public class OLSPredictionSearch implements PredictionSearch {

    private OLSSearchService olsSearchService;
    private OLS2PredictionTransformer transformer;

    @Autowired
    public OLSPredictionSearch(OLSSearchService olsSearchService, OLS2PredictionTransformer transformer) {
        this.olsSearchService = olsSearchService;
        this.transformer = transformer;
    }

    private final Logger log = LoggerFactory.getLogger(getClass());
    protected Logger getLog() {
        return log;
    }

    @Override
    public List<Prediction> search(String propertyValuePattern) {
        getLog().debug("OLS search for {} with ontologies {} ", propertyValuePattern);
        return this.searchWithOrigin(propertyValuePattern, new ArrayList<String>(), false);
    }

    @Override
    public List<Prediction> searchWithOrigin(String propertyValuePattern, List<String> origin, boolean filter) {
        List <Term> terms;
        //if filter == true we want to filter ontologies IF a list of them has been given
        if (filter && !origin.isEmpty()) {
            terms = olsSearchService.getTermsByName(propertyValuePattern, origin);
        } else {
            terms = olsSearchService.getTermsByName(propertyValuePattern);
        }
        return termsToPredictions(terms);
    }


    @Override
    public List<Prediction> search(String propertyType, String propertyValuePattern) {
       return this.searchWithOrigin(propertyType, propertyValuePattern, new ArrayList<String>(), false);
    }


    @Override
    public List<Prediction> searchWithOrigin(String propertyType, String propertyValuePattern, List<String> origin, boolean filter) {
        List <Term> terms = new ArrayList<>();

        getLog().debug("simple OLS search for {} with type {} ", propertyValuePattern, propertyType);
        //get the parent uris from propertyType
        if (filter && !origin.isEmpty()){
            getLog().debug("OLS search for {} with type {} and ontologies {} ", propertyValuePattern, propertyType, origin);

            //if filter == true we want to filter ontologies IF a list of them has been given
            if (!origin.isEmpty()) {
                //get the parent uris from propertyType
                List<Term> parentTerms = olsSearchService.getTermsByName(propertyType, origin);
                if (parentTerms != null && !parentTerms.isEmpty()) {
                    StringBuilder childrenOf = new StringBuilder();
                    for (Term parent : parentTerms) {
                        if (!childrenOf.toString().contains(parent.getIri().getIdentifier())) {
                            childrenOf.append(parent.getIri().getIdentifier());
                        }
                    }
                    terms = olsSearchService.getTermsByNameFromParent(propertyValuePattern, origin, childrenOf.toString());

                }
            }
        } else {
            List<Term> parentTerms = olsSearchService.getTermsByName(propertyType);
            if (parentTerms != null && !parentTerms.isEmpty()) {
                StringBuilder childrenOf = new StringBuilder();
                for (Term parent : parentTerms) {
                    if (!childrenOf.toString().contains(parent.getIri().getIdentifier())) {
                        childrenOf.append(parent.getIri().getIdentifier() + ",");
                    }
                }
                terms = olsSearchService.getTermsByNameFromParent(propertyValuePattern, childrenOf.toString());
            }
        }
        return termsToPredictions(terms);

    }

    private List<Prediction> termsToPredictions(List<Term> terms){
        List<Prediction> predictions = new ArrayList<>();
        for (Term term : terms) {
            predictions.add(transformer.olsToPrediction(term));
        }
        return predictions;
    }
}
