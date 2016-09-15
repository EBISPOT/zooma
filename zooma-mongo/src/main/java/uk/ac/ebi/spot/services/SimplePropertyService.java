package uk.ac.ebi.spot.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.model.Property;
import uk.ac.ebi.spot.repositories.PropertyRepository;

import java.util.List;


/**
 * Created by olgavrou on 09/08/2016.
 */
@Service
public class SimplePropertyService {

    @Autowired
    PropertyRepository propertyRepository;

    public List<Property> getDistinctAnnotatedProperties(){
        return propertyRepository.findDistinctAnnotatedProperties();
    }

    public List<String> getAllPropertyTypes(){
        return propertyRepository.findAllPropertyTypes();
    }

    public List<Property> getPropertyFromPropertyType(String type){
        return propertyRepository.findPropertyFromPropertyType(type);
    }

    public List<Property> getPropertyFromPropertyValue(String value){
        return propertyRepository.findPropertyFromPropertyValue(value);
    }

    public List<Property> getPropertyFromPropertyTypeAndPropertyValue(String type, String value){
        return propertyRepository.findPropertyFromPropertyTypeAndPropertyValue(type, value);
    }

}
