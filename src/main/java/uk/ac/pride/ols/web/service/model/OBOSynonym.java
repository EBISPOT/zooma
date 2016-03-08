package uk.ac.pride.ols.web.service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * @author Yasset Perez-Riverol (ypriverol@gmail.com)
 * @date 08/03/2016
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OBOSynonym {

    @JsonProperty("name")
    String name;

    @JsonProperty("scope")
    String scope;

    @JsonProperty("type")
    String type;

    @JsonProperty("xrefs")
    String[] xrefs;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String[] getXrefs() {
        return xrefs;
    }

    public void setXrefs(String[] xrefs) {
        this.xrefs = xrefs;
    }
}
