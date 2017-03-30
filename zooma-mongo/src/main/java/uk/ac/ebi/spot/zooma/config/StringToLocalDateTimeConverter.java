package uk.ac.ebi.spot.zooma.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.zooma.exception.WrongDateFormatException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Created by olgavrou on 23/03/2017.
 */
@Component
public class StringToLocalDateTimeConverter implements Converter<String, LocalDateTime> {

    @Override
    public LocalDateTime convert(String source) {
        if(source == null
                || source.isEmpty()
                || source.equals("null")){
            return  LocalDateTime.now();
        }
        DateTimeFormatter dashedDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        if (correctFormatter(dashedDateFormatter, source)){
            return normalizedDateTime(dashedDateFormatter, LocalDateTime.parse(source, dashedDateFormatter));
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        if (correctFormatter(formatter, source)) {
            return normalizedDateTime(dashedDateFormatter, LocalDateTime.parse(source, formatter));
        }
        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        if (correctFormatter(formatter, source)) {
            return normalizedDateTime(dashedDateFormatter, LocalDateTime.parse(source, formatter));
        }
        formatter = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");
        if (correctFormatter(formatter, source)) {
            return normalizedDateTime(dashedDateFormatter, LocalDateTime.parse(source, formatter));
        }
        formatter = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm:ss");
        if (correctFormatter(formatter, source)) {
            return normalizedDateTime(dashedDateFormatter, LocalDateTime.parse(source, formatter));
        }
        formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        if (correctFormatter(formatter, source)) {
            return normalizedDateTime(dashedDateFormatter, LocalDateTime.parse(source, formatter));
        }
        formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        if (correctFormatter(formatter, source)) {
            return normalizedDateTime(dashedDateFormatter, LocalDateTime.parse(source, formatter));
        }
        throw new WrongDateFormatException(source);
    }

    private LocalDateTime normalizedDateTime(DateTimeFormatter formatter, LocalDateTime dateTime) {
        dateTime = dateTime.withSecond(1);
        dateTime.format(formatter);
        return dateTime;
    }

    private boolean correctFormatter(DateTimeFormatter formatter, String source){
        try {
            LocalDateTime.parse(source, formatter);
        } catch (DateTimeParseException e){
            return false;
        }
        return true;
    }
}
