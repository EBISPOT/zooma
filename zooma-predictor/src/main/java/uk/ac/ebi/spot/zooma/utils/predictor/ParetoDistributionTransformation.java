package uk.ac.ebi.spot.zooma.utils.predictor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Created by olgavrou on 17/05/2017.
 */
@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class ParetoDistributionTransformation {
    @NonNull
    private float maxValue;
    private float minValue = 0;
    private float alpha = 1.5f;

    
    public float transform(float value){
        return new Double(this.maxValue * (1 - Math.pow((this.minValue / value), this.alpha))).floatValue();
    }
}
