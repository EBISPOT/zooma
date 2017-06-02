package uk.ac.ebi.spot.zooma.model.predictor;

/**
 * Created by olgavrou on 02/06/2017.
 */
public interface Confident {

    Confidence getConfidence();

    void setConfidence(Confidence confidence);

    enum Confidence {
        HIGH,
        GOOD,
        MEDIUM,
        LOW
    }
}
