package uk.ac.ebi.spot.zooma.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by olgavrou on 17/02/2017.
 */
public class SummaryUtils {

    public static float normaliseScore(float score, float max){
        float min = minScore();
        if ((score - min) < 0) {
            return 50;
        }
        else {
            float n = 50 + (50 * (score - min)/(max - min));
            return n;
        }
    }

    private static float minScore() {
        // expected minimum score
        Date y2k;
        try {
            //a really old date would make the quality not so good
            y2k = new SimpleDateFormat("YYYY").parse("2000");
        }
        catch (ParseException e) {
            throw new InstantiationError("Could not parse date '2000' (YYYY)");
        }
        float bottomQuality = (float) (1.0 + Math.log10(y2k.getTime()));
        int sourceNumber = 1;
        int numOfDocs = 1;
//        float normalizedFreq = 1.0f + (2 > 0 ? (numOfDocs / 2) : 0);
        return  (bottomQuality + sourceNumber);
    }
}
