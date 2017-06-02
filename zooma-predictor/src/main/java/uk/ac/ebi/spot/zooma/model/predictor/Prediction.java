package uk.ac.ebi.spot.zooma.model.predictor;

/**
 * Created by olgavrou on 02/06/2017.
 */
public interface Prediction extends Scorable, Confident{

    String getPropertyValue();

    void setPropertyValue(String propertyValue);
}
