package uk.ac.ebi.spot.zooma.exception;

import org.springframework.core.convert.ConversionException;

/**
 * Created by olgavrou on 28/03/2017.
 */
public class WrongDateFormatException extends ConversionException {
    public WrongDateFormatException(String date) {
        super("Wrong date/time format! Use: 'yyyy-MM-dd'T'HH:mm:ss' or 'yyyy-MM-dd HH:mm:ss' for your date: " + date);
    }
}
