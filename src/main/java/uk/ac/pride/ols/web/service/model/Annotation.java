package uk.ac.pride.ols.web.service.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sun.javafx.collections.MappingChange;
import uk.ac.pride.ols.web.service.utils.Constants;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Yasset Perez-Riverol (ypriverol@gmail.com)
 * @date 01/03/2016
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Annotation {

    public enum AnnotationType{
        DATABASE_CROSS_REFERENCE("database_cross_reference");

        String value;

        AnnotationType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public Map<String, String[]> annotation = new HashMap<String, String[]>();

    @JsonAnyGetter
    public Map<String, String[]> any() {
        return annotation;
    }

    @JsonAnySetter
    public void set(String name, String[] value) {
        annotation.put(name, value);
    }

    public boolean hasUnknowProperties() {
        return !annotation.isEmpty();
    }

    public boolean containsAnnotation(String annotationType) {
        if(annotationType == null || annotation.size() == 0 )
            return false;
        return annotation.containsKey(annotationType);
    }

    public String[] getAnnotation(String annotationType) {
        if(annotationType == null || annotation.size() == 0 || !annotation.containsKey(annotationType) )
            return null;
        return annotation.get(annotationType);
    }

    public boolean containsCrossReference(String crossReferenceType){
        if(crossReferenceType == null || annotation.size() == 0 || !containsAnnotation(AnnotationType.DATABASE_CROSS_REFERENCE.getValue())
                || !containsCrossReference(crossReferenceType, annotation.get(AnnotationType.DATABASE_CROSS_REFERENCE.getValue())))
            return false;
        return true;
    }

    public String getCrossReferenceValue(String crossReferenceType){
        if(containsCrossReference(crossReferenceType))
            for(String annotation: this.annotation.get(AnnotationType.DATABASE_CROSS_REFERENCE.getValue()))
                if(annotation.toUpperCase().contains(crossReferenceType.toUpperCase()))
                    if(annotation.split(Constants.REFERENCE_SEPARATOR).length == 2)
                        return annotation.split(Constants.REFERENCE_SEPARATOR)[1];

        return null;
    }

    private boolean containsCrossReference(String crossReferenceType, String[] annotations){
        for(String annotation: annotations)
            if(annotation.toUpperCase().contains(crossReferenceType.toUpperCase()))
                return true;
        return false;
    }

}
