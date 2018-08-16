package uk.ac.ebi.spot.zooma.engine.ols;

import org.junit.Test;
import uk.ac.ebi.pride.utilities.ols.web.service.client.OLSClient;
import uk.ac.ebi.pride.utilities.ols.web.service.config.OLSWsConfig;
import uk.ac.ebi.pride.utilities.ols.web.service.model.Term;
import uk.ac.ebi.spot.zooma.model.predictor.AnnotationPrediction;

import java.util.Collection;

import static org.junit.Assert.*;

/**
 * Created by olgavrou on 05/06/2017.
 */
public class OLS2PredictionTransformerIT {
    @Test
    public void olsToPrediction() throws Exception {
        OLSClient olsClient = new OLSClient(new OLSWsConfig());
        String termValue = "liver";
        String ontology = "efo";
        Term term = olsClient.getExactTermByName(termValue, ontology);
        OLS2PredictionTransformer transformer = new OLS2PredictionTransformer();

        AnnotationPrediction prediction = transformer.olsToPrediction(term);
        assertTrue(prediction.getType().equals(AnnotationPrediction.Type.OLS));

        Collection<String> source = prediction.getSource();
        assertTrue(source.size() == 1);
        assertTrue(source.iterator().next().equals(ontology));

        assertTrue(prediction.getPropertyValue().equals(termValue));
        assertTrue(prediction.getScore() == Float.valueOf(term.getScore()));
        assertTrue(prediction.getSourceNum() == 1);
        assertTrue(prediction.getVotes() == 1);
        assertNull(prediction.getConfidence());
        assertTrue(prediction.getSemanticTag().size() == 1);
        assertTrue(prediction.getSemanticTag().iterator().next().equals(term.getIri().getIdentifier().toString()));
        assertNull(prediction.getStrongestMongoid());
        assertNull(prediction.getMongoid());
    }

}