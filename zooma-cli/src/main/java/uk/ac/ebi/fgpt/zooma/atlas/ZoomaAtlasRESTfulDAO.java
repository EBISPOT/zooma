package uk.ac.ebi.fgpt.zooma.atlas;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.zooma.model.Property;

import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * An implementation of ZoomaAtlasDAO that obtains data from the atlas by communicating with the Atlas Curators REST
 * API.  You should configure this instance by supply the URL to the REST API root
 *
 * @author Tony Burdett
 * @date 24/10/11
 */
public class ZoomaAtlasRESTfulDAO implements ZoomaAtlasDAO {
    private URL restApiRoot;

    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public URL getRestApiRoot() {
        return restApiRoot;
    }

    public void setRestApiRoot(URL restApiRoot) {
        this.restApiRoot = restApiRoot;
    }

    @Override public List<Property> getUnmappedProperties() {
        throw new UnsupportedOperationException("REST API support is not yet implemented");
    }

    @Override public Map<Property, List<String>> getUnmappedPropertiesWithStudyAccessions(
            List<Property> referenceProperties) {
        throw new UnsupportedOperationException("REST API support is not yet implemented");
    }

    @Override public List<String> getStudiesByUnmappedProperty(String property, String propertyValue) {
        throw new UnsupportedOperationException("REST API support is not yet implemented");
    }

    @Override public boolean ontologyTermExists(String accession) {
        throw new UnsupportedOperationException("REST API support is not yet implemented");
    }
}
