package uk.ac.pride.ols.web.service.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sun.javafx.collections.MappingChange;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Yasset Perez-Riverol (ypriverol@gmail.com)
 * @date 01/03/2016
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Annotation {

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

}
