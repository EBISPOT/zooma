package uk.ac.ebi.spot.zooma.config;


import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Created by olgavrou on 23/03/2017.
 */
@Component
public class StringToLocalDateTimeConverter implements Converter<String, LocalDateTime> {

    @Override
    public LocalDateTime convert(String source) {
        DateTimeFormatter formatter;
        if (source.contains("T")) {
            formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

        } else {
            formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        }
        LocalDateTime dateTime = LocalDateTime.parse(source, formatter);
        return dateTime;
    }
}
