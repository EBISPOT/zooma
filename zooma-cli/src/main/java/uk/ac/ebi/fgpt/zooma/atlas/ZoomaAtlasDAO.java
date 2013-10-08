package uk.ac.ebi.fgpt.zooma.atlas;

import uk.ac.ebi.fgpt.zooma.model.Property;

import java.util.List;
import java.util.Map;

/**
 * General DAO interface for obtaining data from the Atlas.  How this data is obtained (direct from the database or via
 * an API etc) is up to the concrete implementation.
 *
 * @author Tony Burdett
 * @date 24/10/11
 */
public interface ZoomaAtlasDAO {
    List<Property> getUnmappedProperties();

    Map<Property, List<String>> getUnmappedPropertiesWithStudyAccessions(List<Property> referenceProperties);

    List<String> getStudiesByUnmappedProperty(String property, String propertyValue);

    boolean ontologyTermExists(String accession);
}
