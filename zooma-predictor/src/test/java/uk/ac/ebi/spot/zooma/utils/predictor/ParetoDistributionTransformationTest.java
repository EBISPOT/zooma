package uk.ac.ebi.spot.zooma.utils.predictor;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by olgavrou on 01/06/2017.
 */
public class ParetoDistributionTransformationTest {
    @Test
    public void transform() throws Exception {

        ParetoDistributionTransformation transformation = new ParetoDistributionTransformation(0);
        float value = transformation.transform(2);
        assertTrue(value == 0);

        value = transformation.transform(0);
        assertTrue(value == 0);

        transformation = new ParetoDistributionTransformation(2);
        value = transformation.transform(0);
        assertTrue(value == 0);

        value = transformation.transform(1000);
        assertTrue(value < 2);
        value = transformation.transform(1);
        assertTrue(value < 2);
        value = transformation.transform(2);
        assertTrue(value < 2);
        value = transformation.transform(3);
        assertTrue(value < 2);
        value = transformation.transform(4);
        assertTrue(value < 2);
        value = transformation.transform(5);
        assertTrue(value < 2);
        value = transformation.transform(6);
        assertTrue(value < 2);
        value = transformation.transform(7);
        assertTrue(value < 2);
        value = transformation.transform(8);
        assertTrue(value < 2);
        value = transformation.transform(9);
        assertTrue(value < 2);
        value = transformation.transform(10);
        assertTrue(value < 2);
        value = transformation.transform(100);
        assertTrue(value < 2);

    }

}