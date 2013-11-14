package uk.ac.ebi.fgpt.zooma.datasource;

import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.zooma.exception.NoSuchResourceException;
import uk.ac.ebi.fgpt.zooma.exception.ResourceAlreadyExistsException;
import uk.ac.ebi.fgpt.zooma.exception.SPARQLQueryException;
import uk.ac.ebi.fgpt.zooma.exception.TooManyResultsException;
import uk.ac.ebi.fgpt.zooma.model.Property;
import uk.ac.ebi.fgpt.zooma.model.SimpleTypedProperty;
import uk.ac.ebi.fgpt.zooma.model.SimpleUntypedProperty;
import uk.ac.ebi.fgpt.zooma.model.TypedProperty;
import uk.ac.ebi.fgpt.zooma.service.QueryVariables;
import uk.ac.ebi.fgpt.zooma.service.SesameRepositoryManager;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A zooma property DAO implementation that uses a SPARQL endpoint to expose properties in response to canned queries.
 *
 * @author Simon Jupp
 * @author Tony Burdett
 * @date 03/04/12
 */
public class SparqlPropertyDAO implements PropertyDAO {
    private SesameRepositoryManager manager;

    private Logger log = LoggerFactory.getLogger(getClass());

    public SesameRepositoryManager getManager() {
        return manager;
    }

    public void setManager(SesameRepositoryManager manager) {
        this.manager = manager;
    }

    protected Logger getLog() {
        return log;
    }

    @Override public String getDatasourceName() {
        return "zooma.properties";
    }

    @Override public int count() {
        return read().size();
    }

    @Override public void create(Property property) throws ResourceAlreadyExistsException {
        throw new UnsupportedOperationException("Property creation is not supported in the current implementation");
    }

    @Override public void update(Property property) throws NoSuchResourceException {
        throw new UnsupportedOperationException("Property updating is not supported in the current implementation");
    }

    @Override public void delete(Property property) throws NoSuchResourceException {
        throw new UnsupportedOperationException("Property deletion is not supported in the current implementation");
    }

    @Override public Collection<Property> read() {
        String query = getManager().getQueryManager().getSparqlQuery("Property.read");
        TupleQueryResult result = manager.evaluateQuery(query);
        return evaluateQueryResults(result);
    }

    @Override public List<Property> read(int size, int start) {
        String query = getManager().getQueryManager().getSparqlQuery("Property.read");
        query += "\nLIMIT " + size + " OFFSET " + start;
        TupleQueryResult result = manager.evaluateQuery(query);
        return evaluateQueryResults(result);
    }

    @Override public Collection<String> readTypes() {
        String query = getManager().getQueryManager().getSparqlQuery("Property.types");
        Set<String> types = new HashSet<>();
        TupleQueryResult result = manager.evaluateQuery(query);

        try {
            while (result.hasNext()) {
                BindingSet bs = result.next();
                Value propertyName = bs.getValue(QueryVariables.PROPERTY_NAME.toString());
                if (propertyName != null) {
                    types.add(propertyName.stringValue());
                }
            }
            return types;
        }
        catch (QueryEvaluationException e) {
            throw new SPARQLQueryException("Failed to retrieve property types", e);
        }
    }

    @Override public List<String> readTypes(int size, int start) {
        String query = getManager().getQueryManager().getSparqlQuery("Property.types");
        query += "\nLIMIT " + size + " OFFSET " + start;

        List<String> types = new ArrayList<>();
        TupleQueryResult result = manager.evaluateQuery(query);

        try {
            while (result.hasNext()) {
                BindingSet bs = result.next();
                Value propertyName = bs.getValue(QueryVariables.PROPERTY_NAME.toString());
                if (propertyName != null) {
                    types.add(propertyName.stringValue());
                }
            }
            return types;
        }
        catch (QueryEvaluationException e) {
            throw new SPARQLQueryException("Failed to retrieve property types", e);
        }
    }

    @Override public Property read(URI uri) {
        String query = getManager().getQueryManager().getSparqlQuery("Property.read");

        Map<String, Value> bindingMap = new HashMap<>();
        ValueFactory factory = getManager().getValueFactory();
        bindingMap.put(QueryVariables.PROPERTY_VALUE_ID.toString(), factory.createURI(uri.toString()));

        TupleQueryResult result = manager.evaluateQuery(query, bindingMap);
        List<Property> ps = evaluateQueryResults(result);

        if (ps.size() > 1) {
            getLog().error("Too many results looking for property <" + uri.toString() + ">");
            throw new TooManyResultsException("Expected one result, got " + ps.size() + " for <" + uri + ">");
        }
        else {
            if (ps.size() == 0) {
                return null;
            }
            else {
                return ps.get(0);
            }
        }
    }

    @Override public String readType(URI uri) {
        Property p = read(uri);
        if (p instanceof TypedProperty) {
            return ((TypedProperty) p).getPropertyType();
        }
        return "";
    }

    @Override public Property readByTypeAndValue(String type, String value) {
        String query = getManager().getQueryManager().getSparqlQuery("Property.read");

        Map<String, Value> bindingMap = new HashMap<>();
        ValueFactory factory = getManager().getValueFactory();
        bindingMap.put(QueryVariables.PROPERTY_NAME.toString(), factory.createLiteral(type));
        bindingMap.put(QueryVariables.PROPERTY_VALUE.toString(), factory.createLiteral(value));

        TupleQueryResult result = manager.evaluateQuery(query, bindingMap);
        List<Property> ps = evaluateQueryResults(result);

        if (ps.size() > 1) {
            getLog().error("Too many results looking for property [" + type + ": " + value + "]");
            throw new TooManyResultsException(
                    "Expected one result, got " + ps.size() + " for property [" + type + ": " + value + "]");
        }
        else {
            if (ps.size() == 0) {
                return null;
            }
            else {
                return ps.get(0);
            }
        }
    }

    @Override public Property readByValue(String value) {
        String query = getManager().getQueryManager().getSparqlQuery("Property.read");

        Map<String, Value> bindingMap = new HashMap<>();
        ValueFactory factory = getManager().getValueFactory();

        bindingMap.put(QueryVariables.PROPERTY_NAME.toString(), factory.createLiteral(""));
        bindingMap.put(QueryVariables.PROPERTY_VALUE.toString(), factory.createLiteral(value));

        TupleQueryResult result = manager.evaluateQuery(query, bindingMap);
        List<Property> ps = evaluateQueryResults(result);

        if (ps.size() > 1) {
            getLog().error("Too many results looking for untyped property [" + value + "]");
            throw new TooManyResultsException(
                    "Expected one result, got " + ps.size() + " for untyped property [" + value + "]");
        }
        else {
            if (ps.size() == 0) {
                return null;
            }
            else {
                return ps.get(0);
            }
        }
    }

    @Override
    public Collection<Property> readByType(String type) {
        String query = getManager().getQueryManager().getSparqlQuery("Property.read");

        Map<String, Value> bindingMap = new HashMap<>();
        ValueFactory factory = getManager().getValueFactory();

        bindingMap.put(QueryVariables.PROPERTY_NAME.toString(), factory.createLiteral(""));

        TupleQueryResult result = manager.evaluateQuery(query, bindingMap);
        return evaluateQueryResults(result);
    }

    public List<Property> evaluateQueryResults(TupleQueryResult result) {
        try {
            List<Property> ps = new ArrayList<>();
            while (result.hasNext()) {
                BindingSet bindingSet = result.next();
                Property p = getPropertyFromBindingSet(bindingSet);
                ps.add(p);
            }
            return ps;
        }
        catch (QueryEvaluationException e) {
            throw new SPARQLQueryException("Failed to retrieve property", e);
        }
    }

    public Property getPropertyFromBindingSet(BindingSet bindingSet) {
        Value propertyUri = bindingSet.getValue(QueryVariables.PROPERTY_VALUE_ID.toString());
        URI uri = URI.create(propertyUri.stringValue());
        Value propertyName = bindingSet.getValue(QueryVariables.PROPERTY_NAME.toString());
        Value propertyValue = bindingSet.getValue(QueryVariables.PROPERTY_VALUE.toString());

        Property p;
        if (propertyName == null) {
            p = new SimpleUntypedProperty(uri, propertyValue.stringValue());
        }
        else {
            p = new SimpleTypedProperty(uri, propertyName.stringValue(), propertyValue.stringValue());
        }
        return p;
    }
}
