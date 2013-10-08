package uk.ac.ebi.fgpt.zooma.service;

import uk.ac.ebi.fgpt.zooma.util.PropertiesMapAdapter;
import uk.ac.ebi.fgpt.zooma.util.URIUtils;

import java.net.URI;

/**
 * An abstract resolver class that performs URI lookups using properties available from the classpath (see {@link
 * PropertiesMapAdapter}) to resolve shortnames and maps them to URI prefixes.
 *
 * @author Tony Burdett
 * @date 19/09/12
 */
public abstract class AbstractShortnameResolver {
    private PropertiesMapAdapter propertiesMapAdapter;

    public PropertiesMapAdapter getPropertiesMapAdapter() {
        return propertiesMapAdapter;
    }

    public void setPropertiesMapAdapter(PropertiesMapAdapter propertiesMapAdapter) {
        this.propertiesMapAdapter = propertiesMapAdapter;
    }

    /**
     * Resolve the given shortname to a full URI using prefixes set in a properties file on the classpath
     *
     * @param shortname the shortname to resolve
     * @return the full URI this shortname represents
     */
    public URI getURIFromShortname(String shortname) {
        return URIUtils.getURI(getPropertiesMapAdapter().getPropertyMap(), shortname);
    }
}
