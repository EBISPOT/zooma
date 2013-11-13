package uk.ac.ebi.fgpt.zooma.datasource;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.lode.exception.LodeException;
import uk.ac.ebi.fgpt.lode.service.JenaQueryExecutionService;
import uk.ac.ebi.fgpt.zooma.exception.NoSuchResourceException;
import uk.ac.ebi.fgpt.zooma.exception.ResourceAlreadyExistsException;
import uk.ac.ebi.fgpt.zooma.exception.SPARQLQueryException;
import uk.ac.ebi.fgpt.zooma.exception.TooManyResultsException;
import uk.ac.ebi.fgpt.zooma.exception.ZoomaSerializationException;
import uk.ac.ebi.fgpt.zooma.io.ZoomaSerializer;
import uk.ac.ebi.fgpt.zooma.model.Property;
import uk.ac.ebi.fgpt.zooma.model.SimpleTypedProperty;
import uk.ac.ebi.fgpt.zooma.model.SimpleUntypedProperty;
import uk.ac.ebi.fgpt.zooma.model.TypedProperty;
import uk.ac.ebi.fgpt.zooma.service.QueryManager;
import uk.ac.ebi.fgpt.zooma.service.QueryVariables;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Simon Jupp
 * @date 06/08/2013 Functional Genomics Group EMBL-EBI
 */
public class SparqlPropertyDAO implements PropertyDAO {

    private Logger log = LoggerFactory.getLogger(getClass());
    private QueryManager queryManager;
    private JenaQueryExecutionService queryService;

    private ZoomaSerializer<Property, OWLOntology, OWLNamedIndividual> propertyZoomaSerializer;

    protected Logger getLog() {
        return log;
    }

    public JenaQueryExecutionService getQueryService() {
        return queryService;
    }

    public void setQueryService(JenaQueryExecutionService queryService) {
        this.queryService = queryService;
    }

    public ZoomaSerializer<Property, OWLOntology, OWLNamedIndividual> getPropertyZoomaSerializer() {
        return propertyZoomaSerializer;
    }

    public void setPropertyZoomaSerializer(ZoomaSerializer<Property, OWLOntology, OWLNamedIndividual> propertyZoomaSerializer) {
        this.propertyZoomaSerializer = propertyZoomaSerializer;
    }

    public QueryManager getQueryManager() {
        return queryManager;
    }

    public void setQueryManager(QueryManager queryManager) {
        this.queryManager = queryManager;
    }

    @Override public String getDatasourceName() {
        return "zooma.properties";
    }

    @Override public int count() {
        String query = getQueryManager().getSparqlCountQuery("Property.read");
        Graph g = getQueryService().getDefaultGraph();
        Query q1 = QueryFactory.create(query, Syntax.syntaxARQ);
        QueryExecution execute = null;
        int c = 0;
        try {
            execute = getQueryService().getQueryExecution(g, q1, false);
            ResultSet results = execute.execSelect();

            while (results.hasNext()) {
                QuerySolution sol = results.next();
                String cv = sol.getLiteral("count").getLexicalForm();
                c = Integer.parseInt(cv);
            }

        }
        catch (LodeException e) {
            throw new SPARQLQueryException("Failed to retrieve count for properties", e);
        }
        finally {
            if (execute != null) {
                execute.close();
                if (g != null) {
                    g.close();
                }
            }
        }
        return c;
    }

    @Override public void create(Property property) throws ResourceAlreadyExistsException {
        getLog().debug("Triggered property create request...\n\n" + property.toString());

        if (read(property.getURI()) != null) {
            throw new ResourceAlreadyExistsException(
                    "Can't create new property entity with " + property.getURI() + ", URI already exists");
        }

        try {
            final PipedInputStream pis = new PipedInputStream();
            PipedOutputStream pos = new PipedOutputStream(pis);

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Graph g = getQueryService().getDefaultGraph();
                    Model m = ModelFactory.createModelForGraph(g);
                    m.read(pis, "http://www.ebi.ac.uk/fgpt/zooma/create");
                }
            });
            thread.start();
            getPropertyZoomaSerializer().serialize(getDatasourceName(), Collections.singleton(property), pos);
        }
        catch (IOException e) {
            log.error("Couldn't create property entity " + property.toString(), e);
        }
        catch (ZoomaSerializationException e) {
            log.error("Couldn't create property entity " + property.toString(), e);
        }
    }

    @Override public void update(Property property) throws NoSuchResourceException {
        getLog().debug("Triggered property entity update request...\n\n" + property.toString());
        if (read(property.getURI()) == null) {
            throw new NoSuchResourceException(
                    "Can't update property entity with URI " + property.getURI() + " no such property entity exists");
        }

        Graph g = getQueryService().getDefaultGraph();
        Model m = ModelFactory.createModelForGraph(g);
        m.remove(new ResourceImpl(property.getURI().toString()), null, null);

        try {
            final PipedInputStream pis = new PipedInputStream();
            PipedOutputStream pos = new PipedOutputStream(pis);

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Graph g = getQueryService().getDefaultGraph();
                    Model m = ModelFactory.createModelForGraph(g);
                    m.read(pis, "http://www.ebi.ac.uk/fgpt/zooma/update");
                }
            });
            thread.start();
            getPropertyZoomaSerializer().serialize(getDatasourceName(), Collections.singleton(property), pos);
        }
        catch (IOException e) {
            log.error("Couldn't create property entity " + property.toString(), e);
        }
        catch (ZoomaSerializationException e) {
            log.error("Couldn't create property entity " + property.toString(), e);
        }

    }

    @Override public void delete(Property property) throws NoSuchResourceException {
        if (read(property.getURI()) == null) {
            throw new NoSuchResourceException(
                    "Can't delete property entity with URI " + property.getURI() + " no such property entity exists");
        }

        getLog().debug("Triggered property entity delete request...\n\n" + property.toString());
        Graph g = getQueryService().getDefaultGraph();
        Model m = ModelFactory.createModelForGraph(g);
        m.removeAll(new ResourceImpl(property.getURI().toString()), null, null);
    }

    @Override public Collection<Property> read() {
        return read(-1, -1);
    }

    @Override public List<Property> read(int size, int start) {
        String query = getQueryManager().getSparqlQuery("Property.read");
        Graph g = getQueryService().getDefaultGraph();
        Query q1 = QueryFactory.create(query, Syntax.syntaxARQ);
        if (size > -1) {
            q1.setLimit(size);
        }
        if (start > -1) {
            q1.setOffset(start);
        }
        QueryExecution execute = null;
        try {
            execute = getQueryService().getQueryExecution(g, q1, false);
            ResultSet results = execute.execSelect();
            return evaluateQueryResults(results);
        }
        catch (LodeException e) {
            throw new SPARQLQueryException("Failed to retrieve annotation", e);
        }
        finally {
            if (execute != null) {
                execute.close();
                if (g != null) {
                    g.close();
                }
            }
        }
    }

    @Override public Collection<String> readTypes() {
        return readTypes(-1, -1);

    }

    @Override public List<String> readTypes(int size, int start) {
        String query = getQueryManager().getSparqlQuery("Property.types");
        List<String> types = new ArrayList<>();

        Graph g = getQueryService().getDefaultGraph();
        Query q1 = QueryFactory.create(query, Syntax.syntaxARQ);
        if (size > -1) {
            q1.setLimit(size);
        }
        if (start > -1) {
            q1.setOffset(start);
        }
        QueryExecution execute = null;
        try {
            execute = getQueryService().getQueryExecution(g, q1, false);
            ResultSet results = execute.execSelect();
            while (results.hasNext()) {
                QuerySolution solution = (QuerySolution) results.next();
                Literal propertyName = solution.getLiteral(QueryVariables.PROPERTY_NAME.toString());
                if (propertyName != null) {
                    types.add(propertyName.getLexicalForm());
                }
            }
        }
        catch (LodeException e) {
            throw new SPARQLQueryException("Failed to retrieve annotation", e);
        }
        finally {
            if (execute != null) {
                execute.close();
                if (g != null) {
                    g.close();
                }
            }
        }
        return types;
    }


    @Override public Property read(URI uri) {
        String query = getQueryManager().getSparqlQuery("Property.read");
        Graph g = getQueryService().getDefaultGraph();
        Query q1 = QueryFactory.create(query, Syntax.syntaxARQ);
        QuerySolutionMap initialBinding = new QuerySolutionMap();
        initialBinding.add(QueryVariables.PROPERTY_VALUE_ID.toString(), new ResourceImpl(uri.toString()));
        QueryExecution execute = null;
        try {
            execute = getQueryService().getQueryExecution(g, q1.toString(), initialBinding, false);
            ResultSet results = execute.execSelect();
            List<Property> ps = evaluateQueryResults(results);
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
        catch (LodeException e) {
            throw new SPARQLQueryException("Failed to retrieve annotation", e);
        }
        finally {
            if (execute != null) {
                execute.close();
                if (g != null) {
                    g.close();
                }
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
        String query = getQueryManager().getSparqlQuery("Property.read");
        Graph g = getQueryService().getDefaultGraph();
        Query q1 = QueryFactory.create(query, Syntax.syntaxARQ);
        QuerySolutionMap initialBinding = new QuerySolutionMap();
        Model m = ModelFactory.createDefaultModel();

        initialBinding.add(QueryVariables.PROPERTY_NAME.toString(), m.createLiteral(type));
        initialBinding.add(QueryVariables.PROPERTY_VALUE.toString(), m.createLiteral(value));

        QueryExecution execute = null;
        try {
            execute = getQueryService().getQueryExecution(g, q1.toString(), initialBinding, false);
            ResultSet results = execute.execSelect();
            List<Property> ps = evaluateQueryResults(results);
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
        catch (LodeException e) {
            throw new SPARQLQueryException("Failed to retrieve annotation", e);
        }
        finally {
            m.close();
            if (execute != null) {
                execute.close();
                if (g != null) {
                    g.close();
                }
            }
        }
    }

    @Override public Property readByValue(String value) {
        String query = getQueryManager().getSparqlQuery("Property.readNoType");
        Graph g = getQueryService().getDefaultGraph();
        Query q1 = QueryFactory.create(query, Syntax.syntaxARQ);
        QuerySolutionMap initialBinding = new QuerySolutionMap();
        Model m = ModelFactory.createDefaultModel();

        initialBinding.add(QueryVariables.PROPERTY_VALUE.toString(), m.createLiteral(value));
        QueryExecution execute = null;
        try {
            execute = getQueryService().getQueryExecution(g, q1.toString(), initialBinding, false);
            ResultSet results = execute.execSelect();
            List<Property> ps = evaluateQueryResults(results);

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
        catch (LodeException e) {
            throw new SPARQLQueryException("Failed to retrieve annotation", e);
        }
        finally {
            m.close();
            if (execute != null) {
                execute.close();
                if (g != null) {
                    g.close();
                }
            }
        }

    }

    @Override
    public Collection<Property> readByType(String type) {
        String query = getQueryManager().getSparqlQuery("Property.read");
        Graph g = getQueryService().getDefaultGraph();
        Query q1 = QueryFactory.create(query, Syntax.syntaxARQ);
        QuerySolutionMap initialBinding = new QuerySolutionMap();
        Model m = ModelFactory.createDefaultModel();

        initialBinding.add(QueryVariables.PROPERTY_NAME.toString(), m.createLiteral(type));
        QueryExecution execute = null;
        try {
            execute = getQueryService().getQueryExecution(g, q1.toString(), initialBinding, false);
            ResultSet results = execute.execSelect();
            return evaluateQueryResults(results);
        }
        catch (LodeException e) {
            throw new SPARQLQueryException("Failed to retrieve annotation", e);
        }
        finally {
            m.close();
            if (execute != null) {
                execute.close();
                if (g != null) {
                    g.close();
                }
            }
        }
    }

    public List<Property> evaluateQueryResults(ResultSet result) {
        List<Property> ps = new ArrayList<>();
        while (result.hasNext()) {
            QuerySolution solution = (QuerySolution) result.next();
            Property p = getPropertyFromBindingSet(solution);
            ps.add(p);
        }
        return ps;

    }

    final static String underscore = "_";


    public Property getPropertyFromBindingSet(QuerySolution solution) {
        Resource propertyUri = solution.getResource(underscore + QueryVariables.PROPERTY_VALUE_ID.toString());
        URI uri = URI.create(propertyUri.getURI());
        Literal propertyName = solution.getLiteral(underscore + QueryVariables.PROPERTY_NAME.toString());
        Literal propertyValue = solution.getLiteral(underscore + QueryVariables.PROPERTY_VALUE.toString());

        Property p;
        if (propertyName == null) {
            p = new SimpleUntypedProperty(uri, propertyValue.getLexicalForm());
        }
        else {
            p = new SimpleTypedProperty(uri, propertyName.getLexicalForm(), propertyValue.getLexicalForm());
        }
        return p;
    }

}
