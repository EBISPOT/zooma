package uk.ac.ebi.spot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.support.GenericConversionService;

import javax.annotation.Resource;

/**
 * Created by olgavrou on 09/09/2016.

    This configuration class adds all the "String to mongodb object" converters that are defined
    It adds them to springs GenericConversionService
 */
@Configuration
public class MongoConvertersConfiguration {

    @Resource(name = "defaultConversionService")
    private GenericConversionService genericConversionService;

    @Bean
    public String2MongoPropertyConverter string2MongoPropertyConverter(){
        String2MongoPropertyConverter string2MongoPropertyConverter = new String2MongoPropertyConverter();
        genericConversionService.addConverter(string2MongoPropertyConverter);
        return string2MongoPropertyConverter;
    }

    @Bean
    public String2MongoSourceConverter string2MongoSourceConverter(){
        String2MongoSourceConverter string2MongoSourceConverter = new String2MongoSourceConverter();
        genericConversionService.addConverter(string2MongoSourceConverter);
        return string2MongoSourceConverter;
    }

}
