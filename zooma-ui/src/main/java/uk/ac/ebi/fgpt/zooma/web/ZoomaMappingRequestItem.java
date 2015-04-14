package uk.ac.ebi.fgpt.zooma.web;

import java.io.Serializable;

/**
 * Serializable items that make up a mapping request.  These are essentially equivalent to an abstraction around {@link
 * uk.ac.ebi.fgpt.zooma.model.Property} objects in ZOOMA that can be readily deserialized
 *
 * @author Tony Burdett
 * @date 03/04/13
 */
public class ZoomaMappingRequestItem implements Serializable {
    private static final long serialVersionUID = 3438748054922102327L;

    private String uri = null;
    private String propertyType = null;
    private String propertyValue = null;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }

    public String getPropertyValue() {
        return propertyValue;
    }

    public void setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
    }
}
