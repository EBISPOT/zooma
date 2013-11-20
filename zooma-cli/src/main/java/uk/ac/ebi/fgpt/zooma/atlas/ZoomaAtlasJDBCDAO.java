package uk.ac.ebi.fgpt.zooma.atlas;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import uk.ac.ebi.fgpt.zooma.model.Property;
import uk.ac.ebi.fgpt.zooma.model.SimpleTypedProperty;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A DAO implementation that extends the core functionality in the AtlasDAO class with a couple of additional queries
 * for retrieving ontology mappings.
 *
 * @author Tony Burdett
 * @date 03/06/11
 */
public class ZoomaAtlasJDBCDAO implements ZoomaAtlasDAO {
    public static final String PROPERTIES_UNMAPPED =
            "SELECT DISTINCT property, value FROM (" +
                    "SELECT 0 as propertyid, " +
                    "p.property, " +
                    "0 as propertyvalueid, " +
                    "p.value " +
                    "FROM cur_assayproperty p " +
                    "WHERE NOT EXISTS ( " +
                    "  SELECT ontologyterm " +
                    "  FROM cur_ontologymapping m " +
                    "  WHERE m.experiment = p.experiment " +
                    "  AND m.property=p.property " +
                    "  AND m.value = p.value) " +
                    "UNION ALL " +
                    "SELECT 0 as propertyid, " +
                    "p.property, " +
                    "0 as propertyvalueid, " +
                    "p.value " +
                    "FROM cur_sampleproperty p " +
                    "WHERE NOT EXISTS ( " +
                    "  SELECT ontologyterm " +
                    "  FROM cur_ontologymapping m " +
                    "  WHERE m.experiment = p.experiment " +
                    "  AND m.property=p.property " +
                    "  AND m.value = p.value)) ORDER BY property, value";
    public static final String PROPERTIES_UNMAPPED_WITH_EXPERIMENTS =
            "SELECT DISTINCT property, value, experiment FROM (" +
                    "SELECT experiment, " +
                    "0 as propertyid, " +
                    "p.property, " +
                    "0 as propertyvalueid, " +
                    "p.value " +
                    "FROM cur_assayproperty p " +
                    "WHERE NOT EXISTS ( " +
                    "  SELECT ontologyterm " +
                    "  FROM cur_ontologymapping m " +
                    "  WHERE m.experiment = p.experiment " +
                    "  AND m.property=p.property " +
                    "  AND m.value = p.value) " +
                    "UNION ALL " +
                    "SELECT experiment," +
                    "0 as propertyid, " +
                    "p.property, " +
                    "0 as propertyvalueid, " +
                    "p.value " +
                    "FROM cur_sampleproperty p " +
                    "WHERE NOT EXISTS ( " +
                    "  SELECT ontologyterm " +
                    "  FROM cur_ontologymapping m " +
                    "  WHERE m.experiment = p.experiment " +
                    "  AND m.property=p.property " +
                    "  AND m.value = p.value)) ORDER BY property, value, experiment";
    public static final String EXPERIMENTS_BY_UNMAPPED_PROPERTY_SELECT =
            "SELECT accession " +
                    "FROM a2_experiment " +
                    "WHERE accession IN (" +
                    "  SELECT p.experiment" +
                    "  FROM cur_assayproperty p " +
                    "  WHERE NOT EXISTS (" +
                    "    SELECT ontologyterm " +
                    "    FROM cur_ontologymapping m " +
                    "    WHERE m.experiment = p.experiment " +
                    "    AND m.property=p.property " +
                    "    AND m.value = p.value)" +
                    "  AND p.property=?" +
                    "  AND p.value=?" +
                    "  UNION ALL " +
                    "  SELECT p.experiment" +
                    "  FROM cur_sampleproperty p " +
                    "  WHERE NOT EXISTS ( " +
                    "    SELECT ontologyterm " +
                    "    FROM cur_ontologymapping m " +
                    "    WHERE m.experiment = p.experiment " +
                    "    AND m.property=p.property " +
                    "    AND m.value = p.value)" +
                    "  AND p.property=?" +
                    "  AND p.value=?)";
    /**
     * SQL to lookup whether an ontology term accession exists
     */
    public static final String ONTOLOGY_TERM_SELECT =
            "SELECT accession FROM cur_ontology WHERE accession=?";

    /**
     * SQL to write curator ontology terms back to the atlas DB
     */
    public static final String ONTOLOGY_TERM_INSERT =
            "INSERT INTO cur_ontologymapping (experiment, property, value, ontologyterm) " +
                    "VALUES (?, ?, ?, ?)";

    /**
     * SQL to write curator ontology mappings back to the atlas DB
     */
    public static final String ONTOLOGY_MAPPINGS_INSERT =
            "INSERT INTO cur_ontology (accession) " +
                    "VALUES (?)";

    private JdbcTemplate template;

    public void setJdbcTemplate(JdbcTemplate template) {
        this.template = template;
    }

    public List<Property> getUnmappedProperties() {
        return template.query(PROPERTIES_UNMAPPED, new PropertyMapper());
    }

    @Override public Map<Property, List<String>> getUnmappedPropertiesWithStudyAccessions(
            List<Property> referenceProperties) {
        return template.query(PROPERTIES_UNMAPPED_WITH_EXPERIMENTS,
                              new PropertyAndStudyAccessionMapper(referenceProperties));
    }

    @Override public List<String> getStudiesByUnmappedProperty(String property, String propertyValue) {
        return template.query(EXPERIMENTS_BY_UNMAPPED_PROPERTY_SELECT,
                              new Object[]{
                                      property,
                                      propertyValue,
                                      property,
                                      propertyValue},
                              new ExperimentAccessionMapper());
    }

    public boolean ontologyTermExists(String accession) {
        return template.query(ONTOLOGY_TERM_SELECT,
                              new Object[]{accession},
                              new AccessionMapper());
    }

    private static class PropertyMapper implements RowMapper<Property> {
        public Property mapRow(ResultSet resultSet, int i) throws SQLException {
            String pt = resultSet.getString(1);
            String pv = resultSet.getString(2);

            return new SimpleTypedProperty(pt, pv);
        }
    }

    private static class ExperimentAccessionMapper implements RowMapper<String> {
        public String mapRow(ResultSet resultSet, int i) throws SQLException {
            return resultSet.getString(1);
        }
    }

    private class AccessionMapper implements ResultSetExtractor<Boolean> {
        public Boolean extractData(ResultSet resultSet)
                throws SQLException, DataAccessException {
            return resultSet.next();
        }
    }

    private class PropertyAndStudyAccessionMapper implements ResultSetExtractor<Map<Property, List<String>>> {
        private List<Property> referenceProperties;
        private String lastPropertyType;
        private String lastPropertyValue;

        private PropertyAndStudyAccessionMapper(List<Property> referenceProperties) {
            this.referenceProperties = referenceProperties;
            this.lastPropertyType = "";
            this.lastPropertyValue = "";
        }

        @Override public Map<Property, List<String>> extractData(ResultSet resultSet)
                throws SQLException, DataAccessException {
            Map<Property, List<String>> results = new HashMap<>();
            Property p = null;
            List<String> accessionList = null;
            while (resultSet.next()) {
                String pt = resultSet.getString(1);
                String pv = resultSet.getString(2);
                String expt = resultSet.getString(3);

                if (!pt.equals(lastPropertyType) || !pv.equals(lastPropertyValue)) {
                    // completely new property, so add the last one to results
                    if (p != null) {
                        results.put(p, accessionList);
                    }
                    // and create new property and new list
                    p = new SimpleTypedProperty(pt, pv);
                    lastPropertyType = pt;
                    lastPropertyValue = pv;
                    for (Property refProp : referenceProperties) {
                        // test with matches() - equals is never true, because no URIs
                        if (p.matches(refProp)) {
                            // got  a reference version of this property: not
                            p = refProp;
                            break;
                        }
                    }
                    accessionList = new ArrayList<>();
                }

                // update accession list for the current property
                if (p != null) {
                    accessionList.add(expt);
                }
                else {
                    throw new RuntimeException("Unexpected empty property - this should never happen!  " +
                                                       "If you got here, some seriously weird shit went down.");
                }

            }
            return results;
        }
    }
}
