package uk.ac.ebi.spot.zooma.engine.ols;

import org.junit.Test;
import uk.ac.ebi.pride.utilities.ols.web.service.model.Term;
import uk.ac.ebi.spot.zooma.model.predictor.AnnotationPrediction;

import java.util.Collection;

import static org.junit.Assert.*;

/**
 * Created by olgavrou on 05/06/2017.
 */
public class OLS2PredictionTransformerTest {
    @Test
    public void olsToPrediction() throws Exception {
        Term term = new Term();
        OLS2PredictionTransformer transformer = new OLS2PredictionTransformer();
        String termValue = "liver";
        String ontology = "efo";
        String iri = "http://purl.obolibrary.org/obo/UBERON_0002107";
        term.setIri(iri);
        term.setLabel(termValue);
        term.setOntologyName(ontology);
        term.setScore("2.0");

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