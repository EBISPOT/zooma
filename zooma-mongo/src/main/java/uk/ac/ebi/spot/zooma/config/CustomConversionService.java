package uk.ac.ebi.spot.zooma.config;

import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Created by olgavrou on 23/03/2017.
 */
@Component
public class CustomConversionService {

    @Resource(name = "defaultConversionService")
    private GenericConversionService genericConversionService;

    @Bean
    public StringToLocalDateTimeConverter stringToLocalDateTimeConverter(){
        StringToLocalDateTimeConverter stringToLocalDateTimeConverter = new StringToLocalDateTimeConverter();
        genericConversionService.addConverter(stringToLocalDateTimeConverter);
        return stringToLocalDateTimeConverter;
    }

}
