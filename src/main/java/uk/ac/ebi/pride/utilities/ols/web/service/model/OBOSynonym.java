package uk.ac.ebi.pride.utilities.ols.web.service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Yasset Perez-Riverol (ypriverol@gmail.com)
 * Creation date 08/03/2016
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
    Xref[] xrefs;

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

    public Xref[] getXrefs() {
        return xrefs;
    }

    public void setXrefs(Xref[] xrefs) {
        this.xrefs = xrefs;
    }
}
