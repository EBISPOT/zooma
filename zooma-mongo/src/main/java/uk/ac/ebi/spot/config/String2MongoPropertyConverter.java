package uk.ac.ebi.spot.config;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import uk.ac.ebi.spot.model.*;
import uk.ac.ebi.spot.services.MongoAnnotationRepositoryService;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Created by olgavrou on 09/09/2016.
 */
public class String2MongoPropertyConverter implements Converter<String, Property> {

    @Autowired
    MongoAnnotationRepositoryService mongoAnnotationRepositoryService;

    @Override
    public Property convert(String source) {

        String id;
        String propertyType;
        String propertyValue;

        try {
            String sourceDecoded = java.net.URLDecoder.decode(source,"UTF-8");
            JSONObject jsonObject = new JSONObject(sourceDecoded);

            try {
                id = jsonObject.getString("_id");
            } catch (Exception e){
                id = null;
            }
            try {
                propertyType = jsonObject.getString("propertyType");
            } catch (Exception e ){
                propertyType = null;
            }
            try {
                propertyValue = jsonObject.getString("propertyValue");
            } catch (Exception e){
                propertyValue = null;
            }

        } catch (UnsupportedEncodingException e) {
            //empty property will not match with anything
            return new MongoProperty("");
        }

        if (id == null){
            //don't really expect user to provide an _id
            if (propertyValue != null) {
                List<MongoAnnotation> retrivedAnnotationsByProperty = mongoAnnotationRepositoryService.getByAnnotatedPropertyValue(propertyValue);
                if (retrivedAnnotationsByProperty != null && !retrivedAnnotationsByProperty.isEmpty()) {
                    //if there is a property type, find the property with the value-type match
                    if (propertyType != null) {
                        for (MongoAnnotation mongoAnnotation : retrivedAnnotationsByProperty) {
                            MongoTypedProperty property = (MongoTypedProperty) mongoAnnotation.getAnnotatedProperty();
                            if (property.toString().equals(propertyType)) {
                                return property;
                            }
                        }
                    }
                    //if there is no property type, then any SimpleAnnotations returned should have the same property object (id and value)
                    return retrivedAnnotationsByProperty.get(0).getAnnotatedProperty();
                }
            }
            //no property found
            return new MongoProperty("");
        } else {
            //id not null, either a typed or untyped property
            if (propertyType == null){
                return new MongoUntypedProperty(propertyValue);
            }
            return new MongoTypedProperty(propertyType, propertyValue);
        }
    }
}
