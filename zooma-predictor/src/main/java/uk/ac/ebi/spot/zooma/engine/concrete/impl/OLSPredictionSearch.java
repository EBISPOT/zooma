package uk.ac.ebi.spot.zooma.engine.concrete.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.ac.ebi.pride.utilities.ols.web.service.model.Term;
import uk.ac.ebi.spot.zooma.engine.PredictionSearch;
import uk.ac.ebi.spot.zooma.engine.ols.OLS2PredictionTransformer;
import uk.ac.ebi.spot.zooma.engine.ols.OLSSearchService;
import uk.ac.ebi.spot.zooma.model.predictor.AnnotationPrediction;
import uk.ac.ebi.spot.zooma.model.predictor.Prediction;
import uk.ac.ebi.spot.zooma.utils.predictor.ParetoDistributionTransformation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by olgavrou on 18/05/2017.
 */
@Component("ols.delegate")
public class OLSPredictionSearch implements PredictionSearch {

    private OLSSearchService olsSearchService;
    private OLS2PredictionTransformer transformer;
    private ParetoDistributionTransformation transformation;

    @Autowired
    public OLSPredictionSearch(OLSSearchService olsSearchService, OLS2PredictionTransformer transformer) {
        this.olsSearchService = olsSearchService;
        this.transformer = transformer;
        this.transformation = new ParetoDistributionTransformation(2);
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
        return primaryMetaScore(predictions);
    }

    private List<Prediction> primaryMetaScore(List<Prediction> predictions){
        Optional<Prediction> maxAnn = predictions.stream().max((a1, a2) -> Float.compare(a1.getScore(), a2.getScore()));
        float maxSolrScore = maxAnn.isPresent() ? maxAnn.get().getScore() : 0.0f;

        predictions.stream().forEach(annotation -> {
            AnnotationPrediction prediction = (AnnotationPrediction) annotation;
            float sourceNumber = prediction.getSource().size();
            float numOfDocs = prediction.getVotes();
            float topQuality = (float) calculateAnnotationQuality();
            float normalizedSolrScore = 1.0f + prediction.getScore() / maxSolrScore;
            float pareto = this.transformation.transform(numOfDocs);
            float score = (topQuality + sourceNumber + pareto) * normalizedSolrScore;
            prediction.setScore(score);
        });

        return predictions;
    }

    private double calculateAnnotationQuality() throws IllegalArgumentException {

        // evidence is most important factor, invert so ordinal 0 gets highest score
        int evidenceScore = 6;
        // creation time should then work backwards from most recent to oldest
        long age = LocalDateTime.now().toLocalTime().toNanoOfDay();

        return (float) (evidenceScore + Math.log10(age));

    }
}
