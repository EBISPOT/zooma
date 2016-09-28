package uk.ac.ebi.pride.utilities.ols.web.service.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Yasset Perez-Riverol (ypriverol@gmail.com)
 * Creation date 01/03/2016
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class Link {

    public enum LinkOption{

        SELF("self"),
        PARENTS("parents"),
        ALL_PARENTS("hierarchicalParents"),
        ANCESTORS("ancestors"),
        CHILDREN("children"),
        ALL_CHILDREN("hierarchicalChildren"),
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

    public Href getParentsRef(){
        if(links.containsKey(LinkOption.PARENTS.getValue()))
            return links.get(LinkOption.PARENTS.getValue());
        return null;
    }

    public Href getAllParentsRef(){
        if(links.containsKey(LinkOption.ALL_PARENTS.getValue()))
            return links.get(LinkOption.ALL_PARENTS.getValue());
        return null;
    }

    public Href getAllChildrenRef(){
        if(links.containsKey(LinkOption.ALL_CHILDREN.getValue()))
            return links.get(LinkOption.ALL_CHILDREN.getValue());
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
