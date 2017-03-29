package uk.ac.ebi.spot.zooma.config;


import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Created by olgavrou on 29/03/2017.
 */
@Component
public class StringToLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

    private StringToLocalDateTimeConverter converter;

    @Autowired
    public StringToLocalDateTimeDeserializer(StringToLocalDateTimeConverter converter) {
        this.converter = converter;
    }

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        return converter.convert(p.getText());
    }
}
