package uk.ac.ebi.spot.zooma.engine.ols;

import org.springframework.stereotype.Component;
import uk.ac.ebi.pride.utilities.ols.web.service.model.Term;
import uk.ac.ebi.spot.zooma.model.predictor.AnnotationPrediction;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

/**
 * Maps a returned Term from the ols-client to a AnnotationPrediction.
 *
 * Created by olgavrou on 19/05/2016.
 */
@Component
public class OLS2PredictionTransformer {

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    public AnnotationPrediction olsToPrediction(Term term){

        Collection<String> semanticTags = new ArrayList<>();
        semanticTags.add(term.getIri().getIdentifier().toString());

        Collection<String> source = new ArrayList<>();
        source.add(term.getOntologyName());

        float score = Float.valueOf(term.getScore());

        AnnotationPrediction prediction = new AnnotationPrediction();
        prediction.setPropertyType("");
        prediction.setPropertyValue(term.getLabel());
        prediction.setSemanticTag(semanticTags);
        prediction.setSource(source);
        prediction.setScore(score);
        prediction.setSourceNum(1);
        prediction.setVotes(1);

        //TODO: set OLS topic
        Collection<String> topics = new ArrayList<>();
        topics.add("Ontology");
        prediction.setTopic(topics);
        prediction.setQuality(score);
        prediction.setStrongestMongoid("ols");

        LocalDateTime dateTime = LocalDateTime.now();
        Date d = Date.from(dateTime.atZone(ZoneId.of("UTC")).toInstant());
        prediction.setLastModified(d);

        return prediction;

    }


}
