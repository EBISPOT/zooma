package uk.ac.ebi.pride.utilities.ols.web.service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Maximilian Koch (mkoch@ebi.ac.uk).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OboDefinitionCitation {

    @JsonProperty("oboXrefs")
    private OBOXRef[] oboXrefs;

    public OboDefinitionCitation() {
    }

    public OboDefinitionCitation(OBOXRef[] oboXrefs) {
        this.oboXrefs = oboXrefs;
    }

    public OBOXRef[] getOboXrefs() {
        return oboXrefs;
    }

    public void setOboXrefs(OBOXRef[] oboXrefs) {
        this.oboXrefs = oboXrefs;
    }
}
