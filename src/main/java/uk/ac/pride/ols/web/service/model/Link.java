package uk.ac.pride.ols.web.service.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Yasset Perez-Riverol (ypriverol@gmail.com)
 * @date 01/03/2016
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class Link {

    public enum LinkOption{

        SELF("self"),
        PARENTS("parents"),
        ANCESTORS("ancestors"),
        CHILDREN("children"),
        DESCENDANTS("descendants"),
        GRAPH("graph"),
        NEXT("next"),
        LAST("last"),
        FIRST("first"),
        PREV("prev");

        private String value;

        LinkOption(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "LinkOption{" +
                    "value='" + value + '\'' +
                    '}';
        }
    }

    public Map<String, Href> links = new HashMap<String, Href>();

    @JsonAnyGetter
    public Map<String, Href> any() {
        return links;
    }

    @JsonAnySetter
    public void set(String name, Href value) {
        links.put(name, value);
    }

    public boolean hasUnknowProperties() {
        return !links.isEmpty();
    }

    public Href getChildrenRef(){
        if(links.containsKey(LinkOption.CHILDREN.getValue()))
            return links.get(LinkOption.CHILDREN.getValue());
        return null;
    }

    public Href next(){
        if(links.containsKey(LinkOption.NEXT.getValue()))
            return links.get(LinkOption.NEXT.getValue());
        return null;
    }

    public Href previous(){
        if(links.containsKey(LinkOption.PREV.getValue()))
            return links.get(LinkOption.PREV.getValue());
        return null;
    }

}
