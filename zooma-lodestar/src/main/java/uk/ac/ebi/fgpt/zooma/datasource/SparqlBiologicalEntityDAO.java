package uk.ac.ebi.fgpt.zooma.datasource;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.query.ParameterizedSparqlString;
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
import uk.ac.ebi.fgpt.zooma.model.BiologicalEntity;
import uk.ac.ebi.fgpt.zooma.model.SimpleBiologicalEntity;
import uk.ac.ebi.fgpt.zooma.model.SimpleStudy;
import uk.ac.ebi.fgpt.zooma.model.Study;
import uk.ac.ebi.fgpt.zooma.service.QueryManager;
import uk.ac.ebi.fgpt.zooma.service.QueryVariables;
import uk.ac.ebi.fgpt.zooma.util.URIBindingUtils;
import uk.ac.ebi.fgpt.zooma.util.URIUtils;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * @author Simon Jupp
 * @date 06/08/2013 Functional Genomics Group EMBL-EBI
 */
public class SparqlBiologicalEntityDAO implements BiologicalEntityDAO {

    private JenaQueryExecutionService queryService;

    private Logger log = LoggerFactory.getLogger(getClass());

    private QueryManager queryManager;

    private ZoomaSerializer<BiologicalEntity, OWLOntology, OWLNamedIndividual> biologicalEntityZoomaSerializer;

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

    public ZoomaSerializer<BiologicalEntity, OWLOntology, OWLNamedIndividual> getBiologicalEntityZoomaSerializer() {
        return biologicalEntityZoomaSerializer;
    }

    public void setBiologicalEntityZoomaSerializer(ZoomaSerializer<BiologicalEntity, OWLOntology, OWLNamedIndividual> biologicalEntityZoomaSerializer) {
        this.biologicalEntityZoomaSerializer = biologicalEntityZoomaSerializer;
    }

    @Override public String getDatasourceName() {
        return "zooma.bioentities";
    }

    @Override public Collection<BiologicalEntity> read() {
        return read(-1, -1);
    }

    @Override public List<BiologicalEntity> read(int size, int start) {
        String query = getQueryManager().getSparqlQuery("BiologicalEntity.read");
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

    @Override public BiologicalEntity read(URI uri) {
        if (uri == null) {
            throw new RuntimeException("Can't read biological entity of null URI");
        }

        String query = getQueryManager().getSparqlQuery("BiologicalEntity.read");
        Graph g = getQueryService().getDefaultGraph();
        Query q1 = QueryFactory.create(query, Syntax.syntaxARQ);


        QuerySolutionMap initialBinding = new QuerySolutionMap();
        initialBinding.add(QueryVariables.BIOLOGICAL_ENTITY.toString(), new ResourceImpl(uri.toString()));
        QueryExecution execute = null;
        try {
            ParameterizedSparqlString queryString = new ParameterizedSparqlString(q1.toString(), initialBinding);
            execute = getQueryService().getQueryExecution(g, queryString.asQuery(), false);
            ResultSet results = execute.execSelect();
            List<BiologicalEntity> bes = evaluateQueryResults(results);

            if (bes.size() > 1) {
                getLog().error("Too many results looking for biological entity <" + uri.toString() + ">");
                throw new TooManyResultsException("Expected one result, got " + bes.size() + " for <" + uri + ">");
            }
            else {
                if (bes.size() == 0) {
                    getLog().warn("Couldn't find bioentity target with id " + uri.toString());
                    return null;
                }
                else {
                    return bes.iterator().next();
                }
            }

        }
        catch (LodeException e) {
            throw new SPARQLQueryException("Failed to retrieve bioentity", e);
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

    @Override public int count() {
        String query = getQueryManager().getSparqlCountQuery("BiologicalEntity.read");
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
            throw new SPARQLQueryException("Failed to retrieve count for Biological entites / targets", e);
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


    @Override public void create(BiologicalEntity biologicalEntity) throws ResourceAlreadyExistsException {
        getLog().debug("Triggered biological entity create request...\n\n" + biologicalEntity.toString());

        if (read(biologicalEntity.getURI()) != null) {
            throw new ResourceAlreadyExistsException(
                    "Can't create new biological entity with " + biologicalEntity.getURI() + ", URI already exists");
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
            getBiologicalEntityZoomaSerializer().serialize(getDatasourceName(),
                                                           Collections.singleton(biologicalEntity),
                                                           pos);
        }
        catch (IOException e) {
            log.error("Couldn't create biological entity " + biologicalEntity.toString(), e);
        }
        catch (ZoomaSerializationException e) {
            log.error("Couldn't create biological entity " + biologicalEntity.toString(), e);
        }
    }

    @Override public void update(BiologicalEntity biologicalEntity) throws NoSuchResourceException {
        getLog().debug("Triggered biological entity update request...\n\n" + biologicalEntity.toString());
        if (read(biologicalEntity.getURI()) == null) {
            throw new NoSuchResourceException("Can't update biological entity with URI " + biologicalEntity.getURI() +
                                                      " no such biological entity exists");
        }

        Graph g = getQueryService().getDefaultGraph();
        Model m = ModelFactory.createModelForGraph(g);
        m.remove(new ResourceImpl(biologicalEntity.getURI().toString()), null, null);

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
            getBiologicalEntityZoomaSerializer().serialize(getDatasourceName(),
                                                           Collections.singleton(biologicalEntity),
                                                           pos);
        }
        catch (IOException e) {
            log.error("Couldn't create biological entity " + biologicalEntity.toString(), e);
        }
        catch (ZoomaSerializationException e) {
            log.error("Couldn't create biological entity " + biologicalEntity.toString(), e);
        }

    }

    @Override public void delete(BiologicalEntity biologicalEntity) throws NoSuchResourceException {
        if (read(biologicalEntity.getURI()) == null) {
            throw new NoSuchResourceException("Can't delete biological entity with URI " + biologicalEntity.getURI() +
                                                      " no such biological entity exists");
        }

        getLog().debug("Triggered biological entity delete request...\n\n" + biologicalEntity.toString());
        Graph g = getQueryService().getDefaultGraph();
        Model m = ModelFactory.createModelForGraph(g);
        m.removeAll(new ResourceImpl(biologicalEntity.getURI().toString()), null, null);
    }


    @Override
    public Collection<BiologicalEntity> readBySemanticTags(URI... semanticTags) {
        return readBySemanticTags(false, semanticTags);
    }

    @Override
    public Collection<BiologicalEntity> readBySemanticTags(boolean useInference, URI... semanticTags) {
        String query;
        if (useInference) {
            query = getQueryManager().getSparqlQuery("BiologicalEntity.bySemanticTagInferred");
        }
        else {
            query = getQueryManager().getSparqlQuery("BiologicalEntity.bySemanticTag");
        }
        return _readBySemanticTags(query, useInference, semanticTags);
    }

    @Override public Collection<BiologicalEntity> readByStudyAndName(Study study, String bioentityName) {
        throw new UnsupportedOperationException("Biological Entity lookup by study and name is not yet implemented");
    }

    @Override
    public Collection<BiologicalEntity> readByStudy(Study study) {
        throw new UnsupportedOperationException("Biological Entity lookup by study is not yet implemented");
    }

    private Collection<BiologicalEntity> _readBySemanticTags(String query, boolean inference, URI... semanticTags) {
        Graph g = getQueryService().getDefaultGraph();
        Query q1 = QueryFactory.create(query, Syntax.syntaxARQ);
        QueryExecution execute = null;
        List<BiologicalEntity> bes = new ArrayList<>();

        for (URI uri : semanticTags) {
            QuerySolutionMap initialBinding = new QuerySolutionMap();
            initialBinding.add(QueryVariables.SEMANTIC_TAG.toString(), new ResourceImpl(uri.toString()));
            try {
                execute = getQueryService().getQueryExecution(g, q1.toString(), initialBinding, inference);
                ResultSet results = execute.execSelect();
                bes.addAll(evaluateQueryResults(results));
            }
            catch (LodeException e) {
                throw new SPARQLQueryException("Failed to retrieve bio entity", e);
            }

            execute.close();

        }
        g.close();

        HashSet<BiologicalEntity> hs = new HashSet<>();
        hs.addAll(bes);
        bes.clear();
        bes.addAll(hs);
        return bes;
    }

    public List<BiologicalEntity> evaluateQueryResults(ResultSet result) {
        Map<URI, BiologicalEntity> beMap = new HashMap<>();
        Map<URI, Study> studyMap = new HashMap<>();

        while (result.hasNext()) {
            QuerySolution solution = result.nextSolution();
            BiologicalEntity a = getBiologicalEntityFromBindingSet(beMap, studyMap, solution);
            if (a != null) {
                beMap.put(a.getURI(), a);
            }
        }
        List<BiologicalEntity> beList = new ArrayList<>();
        beList.addAll(beMap.values());
        Collections.sort(beList, new Comparator<BiologicalEntity>() {
            @Override public int compare(BiologicalEntity o1, BiologicalEntity o2) {
                return o1.getURI().toString().compareTo(o2.getURI().toString());
            }
        });
        return beList;
    }

    final static String underscore = "_";

    public BiologicalEntity getBiologicalEntityFromBindingSet(
            Map<URI, BiologicalEntity> biologicalEntityMap,
            Map<URI, Study> studyMap,
            QuerySolution solution) {

        Resource studyTypeValue = solution.getResource(underscore + QueryVariables.STUDY_TYPE.toString());
        Resource sampleUriValue = solution.getResource(underscore + QueryVariables.BIOLOGICAL_ENTITY.toString());
        URI uri = URI.create(sampleUriValue.getURI());
        Resource sampleTypeUriValue =
                solution.getResource(underscore + QueryVariables.BIOLOGICAL_ENTITY_TYPE.toString());

        URI sampleTypeUri = null;
//        Resource sampleTypeUriValue =
//                solution.getResource(underscore + QueryVariables.BIOLOGICAL_ENTITY_TYPE.toString());
        if (sampleTypeUriValue != null) {
            sampleTypeUri = URI.create(sampleTypeUriValue.getURI());
        }


        Literal sampleLabelValue = solution.getLiteral(QueryVariables.BIOLOGICAL_ENTITY_NAME.toString());
        String label = "";
        if (sampleLabelValue != null) {
            label = sampleLabelValue.getLexicalForm();
        }

        Resource studyIdValue = solution.getResource(underscore + QueryVariables.STUDY_ID.toString());
//        Resource studyTypeValue = solution.getResource(underscore + QueryVariables.STUDY_TYPE.toString());
        URI studyUri = null;
        URI studyType = null;
        String studyLabel = "";


        if (studyIdValue != null) {
            studyUri = URI.create(studyIdValue.getURI());
            studyLabel = URIUtils.extractFragment(studyUri);

            Literal studyLabelValue = solution.getLiteral(QueryVariables.STUDY_LABEL.toString());
            if (sampleLabelValue != null) {
                studyLabel = studyLabelValue.getLexicalForm();
            }

            if (!studyMap.containsKey(studyUri)) {
                studyMap.put(studyUri, new SimpleStudy(studyUri, studyLabel));
            }

            if (studyTypeValue != null) {
                studyType = URI.create(studyTypeValue.getURI());
                studyMap.get(studyUri).getTypes().add(studyType);
            }
        }


        if (!biologicalEntityMap.containsKey(uri)) {
            BiologicalEntity newBe = new SimpleBiologicalEntity(uri, label);
            biologicalEntityMap.put(newBe.getURI(), newBe);
        }
        if (sampleTypeUri != null) {
            biologicalEntityMap.get(uri).getTypes().add(sampleTypeUri);
        }
        if (studyMap.containsKey(studyUri)) {
            biologicalEntityMap.get(uri).getStudies().add(studyMap.get(studyUri));
        }

        return biologicalEntityMap.get(uri);
    }
}
