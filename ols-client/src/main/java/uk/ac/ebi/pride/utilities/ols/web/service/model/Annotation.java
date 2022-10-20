package uk.ac.ebi.pride.utilities.ols.web.service.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import uk.ac.ebi.pride.utilities.ols.web.service.utils.Constants;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Yasset Perez-Riverol (ypriverol@gmail.com)
 * Creation date 01/03/2016
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

    public Map<String, List<String>> annotation = new HashMap<>();

    @JsonAnyGetter
    public Map<String, List<String>> any() {
        return annotation;
    }

    @JsonAnySetter
    public void set(String name, String[] value) {
        annotation.put(name, Arrays.asList(value));
    }

    public boolean hasUnknownProperties() {
        return !annotation.isEmpty();
    }

    public boolean containsAnnotation(String annotationType) {
        return !(annotationType == null || annotation.size() == 0) && annotation.containsKey(annotationType);
    }

    public List<String> getAnnotation(String annotationType) {
        if(annotationType == null || annotation.size() == 0 || !annotation.containsKey(annotationType) )
            return null;
        return annotation.get(annotationType);
    }

    public boolean containsCrossReference(String crossReferenceType){
        return !(crossReferenceType == null || annotation.size() == 0 || !containsAnnotation(AnnotationType.DATABASE_CROSS_REFERENCE.getValue())
                || !containsCrossReference(crossReferenceType, annotation.get(AnnotationType.DATABASE_CROSS_REFERENCE.getValue())));
    }

    public String getCrossReferenceValue(String crossReferenceType){
        if(containsCrossReference(crossReferenceType))
            for(String annotation: this.annotation.get(AnnotationType.DATABASE_CROSS_REFERENCE.getValue()))
                if(annotation.toUpperCase().contains(crossReferenceType.toUpperCase()))
                    if(annotation.split(Constants.REFERENCE_SEPARATOR).length == 2)
                        return annotation.split(Constants.REFERENCE_SEPARATOR)[1];

        return null;
    }

    private boolean containsCrossReference(String crossReferenceType, List<String> annotations){
        for(String annotation: annotations)
            if(annotation.toUpperCase().contains(crossReferenceType.toUpperCase()))
                return true;
        return false;
    }

    public Map<String, List<String>> getAnnotation() {
        return annotation;
    }
}
