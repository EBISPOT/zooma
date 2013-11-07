package uk.ac.ebi.fgpt.zooma.datasource;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.query.*;
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
import uk.ac.ebi.fgpt.zooma.datasource.BiologicalEntityDAO;
import uk.ac.ebi.fgpt.zooma.exception.*;
import uk.ac.ebi.fgpt.zooma.io.ZoomaSerializer;
import uk.ac.ebi.fgpt.zooma.model.*;
import uk.ac.ebi.fgpt.zooma.service.QueryManager;
import uk.ac.ebi.fgpt.zooma.service.QueryVariables;
import uk.ac.ebi.fgpt.zooma.util.URIUtils;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Simon Jupp
 * @date 06/08/2013
 * Functional Genomics Group EMBL-EBI
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
        return read (-1,-1);
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
        } catch (LodeException e) {
            throw new SPARQLQueryException("Failed to retrieve annotation", e);
        }
        finally {
            if (execute !=  null)  {
                execute.close();
                if (g != null ) {
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
            execute = getQueryService().getQueryExecution(g, q1.toString(), initialBinding, false);
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

        } catch (LodeException e) {
            throw new SPARQLQueryException("Failed to retrieve bioentity", e);
        }
        finally {
            if (execute !=  null)  {
                execute.close();
                if (g != null ) {
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

        } catch (LodeException e) {
            throw new SPARQLQueryException("Failed to retrieve count for Biological entites / targets", e);
        }
        finally {
            if (execute !=  null)  {
                execute.close();
                if (g != null ) {
                    g.close();
                }
            }
        }
        return c;
    }


    @Override public void create(BiologicalEntity biologicalEntity) throws ResourceAlreadyExistsException {
        getLog().debug("Triggered biological entity create request...\n\n" + biologicalEntity.toString());

        if (read(biologicalEntity.getURI()) != null) {
            throw new ResourceAlreadyExistsException("Can't create new biological entity with " + biologicalEntity.getURI() + ", URI already exists");
        }

        try {
            final PipedInputStream pis =new PipedInputStream();
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
            getBiologicalEntityZoomaSerializer().serialize(getDatasourceName(), Collections.singleton(biologicalEntity), pos);
        } catch (IOException e) {
            log.error("Couldn't create biological entity " + biologicalEntity.toString(), e);
        } catch (ZoomaSerializationException e) {
            log.error("Couldn't create biological entity " + biologicalEntity.toString(), e);
        }
    }

    @Override public void update(BiologicalEntity biologicalEntity) throws NoSuchResourceException {
        getLog().debug("Triggered biological entity update request...\n\n" + biologicalEntity.toString());
        if (read(biologicalEntity.getURI()) == null) {
            throw new NoSuchResourceException("Can't update biological entity with URI " + biologicalEntity.getURI() + " no such biological entity exists");
        }

        Graph g = getQueryService().getDefaultGraph();
        Model m = ModelFactory.createModelForGraph(g);
        m.remove(new ResourceImpl(biologicalEntity.getURI().toString()), null, null);

        try {
            final PipedInputStream pis =new PipedInputStream();
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
            getBiologicalEntityZoomaSerializer().serialize(getDatasourceName(), Collections.singleton(biologicalEntity), pos);
        } catch (IOException e) {
            log.error("Couldn't create biological entity " + biologicalEntity.toString(), e);
        } catch (ZoomaSerializationException e) {
            log.error("Couldn't create biological entity " + biologicalEntity.toString(), e);
        }

    }

    @Override public void delete(BiologicalEntity biologicalEntity) throws NoSuchResourceException {
        if (read(biologicalEntity.getURI()) == null) {
            throw new NoSuchResourceException("Can't delete biological entity with URI " + biologicalEntity.getURI() + " no such biological entity exists");
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
        throw new UnsupportedOperationException("Biological Entity accession/name lookup is not yet implemented");
    }

    @Override
    public Collection<BiologicalEntity> readByStudy(Study study) {
        //ToDo Impelement the actual sparql query
        throw new UnsupportedOperationException("The method readByStudy needs to be implemented in Sparql");
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
            } catch (LodeException e) {
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

        while (result.hasNext()) {
            QuerySolution solution =  result.nextSolution();
            BiologicalEntity a = getBiologicalEntityFromBindingSet(beMap, solution);
            beMap.put(a.getURI(), a);
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

    public BiologicalEntity getBiologicalEntityFromBindingSet(Map<URI, BiologicalEntity> biologicalEntityMap,
                                                              QuerySolution solution) {
        Resource sampleUriValue = solution.getResource(underscore + QueryVariables.BIOLOGICAL_ENTITY.toString());
        URI uri = URI.create(sampleUriValue.getURI());

        Literal sampleLabelValue = solution.getLiteral(QueryVariables.BIOLOGICAL_ENTITY_NAME.toString());
        String label = "";
        if (sampleLabelValue != null) {
            label = sampleLabelValue.getLexicalForm();
        }

        Resource studyIdValue = solution.getResource(QueryVariables.STUDY_ID.toString());
        Study study = null;
        URI studyUri = null;
        String studyLabel = "";

        if (studyIdValue != null) {
            studyUri = URI.create(studyIdValue.getURI());
            studyLabel = URIUtils.extractFragment(studyUri);

            Literal studyLabelValue = solution.getLiteral(QueryVariables.STUDY_LABEL.toString());
            if (sampleLabelValue != null) {
               studyLabel = studyLabelValue.getLexicalForm();
            }
            study = new SimpleStudy(studyUri, studyLabel, getTypes(studyUri));
        }


        if (biologicalEntityMap.containsKey(uri) && study != null) {
            biologicalEntityMap.get(uri).getStudies().add(study);
        }
        else {
            BiologicalEntity newBe = new SimpleBiologicalEntity(uri, label, getTypes(uri),study);
            biologicalEntityMap.put(newBe.getURI(), newBe);
        }
        return biologicalEntityMap.get(uri);
    }

    private Collection<URI> getTypes (URI resource) {

        if (resource == null) {
            return Collections.<URI>emptySet();
        }

        String query = getQueryManager().getSparqlQuery("Types");

        Graph g = getQueryService().getDefaultGraph();
        Query q1 = QueryFactory.create(query, Syntax.syntaxARQ);

        QuerySolutionMap initialBinding = new QuerySolutionMap();
        initialBinding.add(QueryVariables.RESOURCE.toString(), new ResourceImpl(resource.toString()));

        QueryExecution execute = null;
        Set<URI> types= new HashSet<URI>();
        try {
            execute = getQueryService().getQueryExecution(g, q1.toString(), initialBinding, false);
            ResultSet results = execute.execSelect();
            while (results.hasNext()) {
                QuerySolution solution =  results.nextSolution();
                Resource r = solution.getResource(QueryVariables.RESOURCE_TYPE.toString());
                if (r !=null) {
                    types.add(URI.create(r.getURI()));
                }
            }
        } catch (LodeException e) {
            throw new SPARQLQueryException("Failed to retrieve types for biological entity types", e);
        }
        finally {
            if (execute !=  null)  {
                execute.close();
                if (g != null ) {
                    g.close();
                }
            }
        }
        return types;
    }

}
