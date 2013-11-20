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
import uk.ac.ebi.fgpt.zooma.model.SimpleStudy;
import uk.ac.ebi.fgpt.zooma.model.Study;
import uk.ac.ebi.fgpt.zooma.service.QueryManager;
import uk.ac.ebi.fgpt.zooma.service.QueryVariables;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Simon Jupp
 * @date 06/08/2013 Functional Genomics Group EMBL-EBI
 */
public class SparqlStudyDAO implements StudyDAO {

    private JenaQueryExecutionService queryService;

    private Logger log = LoggerFactory.getLogger(getClass());

    private QueryManager queryManager;

    private ZoomaSerializer<Study, OWLOntology, OWLNamedIndividual> studyZoomaSerializer;

    protected Logger getLog() {
        return log;
    }

    public JenaQueryExecutionService getQueryService() {
        return queryService;
    }

    public void setQueryService(JenaQueryExecutionService queryService) {
        this.queryService = queryService;
    }

    public QueryManager getQueryManager() {
        return queryManager;
    }

    public void setQueryManager(QueryManager queryManager) {
        this.queryManager = queryManager;
    }

    public ZoomaSerializer<Study, OWLOntology, OWLNamedIndividual> getStudyZoomaSerializer() {
        return studyZoomaSerializer;
    }

    public void setStudyZoomaSerializer(ZoomaSerializer<Study, OWLOntology, OWLNamedIndividual> studyZoomaSerializer) {
        this.studyZoomaSerializer = studyZoomaSerializer;
    }

    @Override public String getDatasourceName() {
        return "zooma.studies";
    }

    @Override public int count() {
        String query = getQueryManager().getSparqlCountQuery("Study.read");
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

    @Override public void create(Study study) throws ResourceAlreadyExistsException {
        getLog().debug("Triggered study create request...\n\n" + study.toString());

        if (read(study.getURI()) != null) {
            throw new ResourceAlreadyExistsException(
                    "Can't create new study entity with " + study.getURI() + ", URI already exists");
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
            getStudyZoomaSerializer().serialize(getDatasourceName(), Collections.singleton(study), pos);
        }
        catch (IOException e) {
            log.error("Couldn't create study entity " + study.toString(), e);
        }
        catch (ZoomaSerializationException e) {
            log.error("Couldn't create study entity " + study.toString(), e);
        }
    }

    @Override public void update(Study study) throws NoSuchResourceException {
        getLog().debug("Triggered study entity update request...\n\n" + study.toString());
        if (read(study.getURI()) == null) {
            throw new NoSuchResourceException(
                    "Can't update study entity with URI " + study.getURI() + " no such study entity exists");
        }

        Graph g = getQueryService().getDefaultGraph();
        Model m = ModelFactory.createModelForGraph(g);
        m.remove(new ResourceImpl(study.getURI().toString()), null, null);

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
            getStudyZoomaSerializer().serialize(getDatasourceName(), Collections.singleton(study), pos);
        }
        catch (IOException e) {
            log.error("Couldn't create study entity " + study.toString(), e);
        }
        catch (ZoomaSerializationException e) {
            log.error("Couldn't create study entity " + study.toString(), e);
        }

    }

    @Override public void delete(Study study) throws NoSuchResourceException {
        if (read(study.getURI()) == null) {
            throw new NoSuchResourceException(
                    "Can't delete study entity with URI " + study.getURI() + " no such study entity exists");
        }

        getLog().debug("Triggered study entity delete request...\n\n" + study.toString());
        Graph g = getQueryService().getDefaultGraph();
        Model m = ModelFactory.createModelForGraph(g);
        m.removeAll(new ResourceImpl(study.getURI().toString()), null, null);
    }


    @Override public Collection<Study> read() {
        return read(-1, -1);
    }

    @Override public List<Study> read(int size, int start) {
        String query = getQueryManager().getSparqlQuery("Study.read");
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

    @Override public Study read(URI uri) {

        if (uri == null) {
            throw new RuntimeException("Can't read studies for URI null");
        }

        String query = getQueryManager().getSparqlQuery("Study.read");
        Graph g = getQueryService().getDefaultGraph();
        Query q1 = QueryFactory.create(query, Syntax.syntaxARQ);


        QuerySolutionMap initialBinding = new QuerySolutionMap();
        initialBinding.add(QueryVariables.STUDY_ID.toString(), new ResourceImpl(uri.toString()));
        QueryExecution execute = null;
        try {
            execute = getQueryService().getQueryExecution(g, q1.toString(), initialBinding, false);
            ResultSet results = execute.execSelect();
            List<Study> studies = evaluateQueryResults(results);

            if (studies.size() > 1) {
                getLog().error("Too many results looking for study <" + uri.toString() + ">");
                throw new TooManyResultsException("Expected one result, got " + studies.size() + " for <" + uri + ">");
            }
            else {
                if (studies.size() == 0) {
                    return null;
                }
                else {
                    return studies.get(0);
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

    private List<Study> evaluateQueryResults(ResultSet result) {
        List<Study> bes = new ArrayList<>();
        while (result.hasNext()) {
            QuerySolution solution = (QuerySolution) result.next();
            Study s = getStudyFromBindingSet(solution);
            bes.add(s);
        }
        return bes;

    }

    private Study getStudyFromBindingSet(QuerySolution solution) {
        Resource studyUriValue = solution.getResource(QueryVariables.STUDY_ID.toString());
        URI studyUri = URI.create(studyUriValue.getURI());

        Literal studyLabelValue = solution.getLiteral(QueryVariables.STUDY_LABEL.toString());
        String studyLabel = studyLabelValue.getLexicalForm();

        return new SimpleStudy(studyUri, studyLabel, getTypes(studyUri));
    }


    @Override
    public Collection<Study> readBySemanticTags(URI... semanticTags) {
        return readBySemanticTags(false, semanticTags);
    }

    @Override
    public Collection<Study> readBySemanticTags(boolean useInference, URI... semanticTags) {
        String query;
        if (useInference) {
            query = getQueryManager().getSparqlQuery("Study.bySemanticTagInferred");
        }
        else {
            query = getQueryManager().getSparqlQuery("Study.bySemanticTag");
        }
        return _readBySemanticTags(query, useInference, semanticTags);
    }

    @Override public Collection<Study> readByAccession(String accession) {
        throw new UnsupportedOperationException("Study accession lookup is not yet implemented");
    }

    private Collection<Study> _readBySemanticTags(String query, boolean useInference, URI... semanticTags) {

        Graph g = getQueryService().getDefaultGraph();
        Query q1 = QueryFactory.create(query, Syntax.syntaxARQ);
        List<Study> bes = new ArrayList<>();

        QuerySolutionMap initialBinding = new QuerySolutionMap();
        QueryExecution execute = null;
        try {
            for (URI uri : semanticTags) {
                initialBinding.add(QueryVariables.SEMANTIC_TAG.toString(), new ResourceImpl(uri.toString()));
                execute = getQueryService().getQueryExecution(g, q1.toString(), initialBinding, useInference);
                ResultSet results = execute.execSelect();
                bes.addAll(evaluateQueryResults(results));
                execute.close();
            }

        }
        catch (LodeException e) {
            throw new SPARQLQueryException("Failed to retrieve annotation", e);
        }
        finally {
            if (g != null) {
                g.close();
            }
        }

        HashSet<Study> hs = new HashSet<>();
        hs.addAll(bes);
        bes.clear();
        bes.addAll(hs);
        return bes;
    }

    private Collection<URI> getTypes(URI resource) {

        if (resource == null) {
            return Collections.<URI>emptySet();
        }

        String query = getQueryManager().getSparqlQuery("Types");

        Graph g = getQueryService().getDefaultGraph();
        Query q1 = QueryFactory.create(query, Syntax.syntaxARQ);

        QuerySolutionMap initialBinding = new QuerySolutionMap();
        initialBinding.add(QueryVariables.RESOURCE.toString(), new ResourceImpl(resource.toString()));

        QueryExecution execute = null;
        Set<URI> types = new HashSet<URI>();
        try {
            execute = getQueryService().getQueryExecution(g, q1.toString(), initialBinding, false);
            ResultSet results = execute.execSelect();
            while (results.hasNext()) {
                QuerySolution solution = results.nextSolution();
                Resource r = solution.getResource(QueryVariables.RESOURCE_TYPE.toString());
                if (r != null) {
                    types.add(URI.create(r.getURI()));
                }
            }
        }
        catch (LodeException e) {
            throw new SPARQLQueryException("Failed to retrieve types for biological entity types", e);
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
}
