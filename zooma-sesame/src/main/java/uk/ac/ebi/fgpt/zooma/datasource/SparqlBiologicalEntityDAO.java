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
import uk.ac.ebi.fgpt.zooma.model.BiologicalEntity;
import uk.ac.ebi.fgpt.zooma.model.SimpleBiologicalEntity;
import uk.ac.ebi.fgpt.zooma.model.SimpleStudy;
import uk.ac.ebi.fgpt.zooma.model.Study;
import uk.ac.ebi.fgpt.zooma.service.QueryVariables;
import uk.ac.ebi.fgpt.zooma.service.SesameRepositoryManager;

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
 * A zooma biological entity DAO implementation that uses a SPARQL endpoint to expose biological entities in response to
 * canned queries.
 *
 * @author Simon Jupp
 * @author Tony Burdett
 * @date 03/04/12
 */
public class SparqlBiologicalEntityDAO implements BiologicalEntityDAO {
    private SesameRepositoryManager manager;

    private Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public SesameRepositoryManager getManager() {
        return manager;
    }

    public void setManager(SesameRepositoryManager manager) {
        this.manager = manager;
    }

    @Override public String getDatasourceName() {
        return "zooma.bioentities";
    }

    @Override public Collection<BiologicalEntity> read() {
        String query = getManager().getQueryManager().getSparqlQuery("BiologicalEntity.read");
        TupleQueryResult result = manager.evaluateQuery(query);
        return evaluateQueryResults(result);
    }

    @Override public List<BiologicalEntity> read(int size, int start) {
        String query = getManager().getQueryManager().getSparqlQuery("BiologicalEntity.read");
        query += "\nLIMIT " + size + " OFFSET " + start;
        TupleQueryResult result = manager.evaluateQuery(query);
        return evaluateQueryResults(result);
    }

    @Override public BiologicalEntity read(URI uri) {
        if (uri == null) {
            return null;
        }

        String query = getManager().getQueryManager().getSparqlQuery("BiologicalEntity.read");

        Map<String, Value> bindingMap = new HashMap<>();
        ValueFactory factory = getManager().getValueFactory();
        bindingMap.put(QueryVariables.BIOLOGICAL_ENTITY.toString(), factory.createURI(uri.toString()));

        TupleQueryResult result = manager.evaluateQuery(query, bindingMap);
        List<BiologicalEntity> bes = evaluateQueryResults(result);

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

    public List<BiologicalEntity> evaluateQueryResults(TupleQueryResult result) {
        try {
            Map<URI, BiologicalEntity> beMap = new HashMap<>();
            while (result.hasNext()) {
                BindingSet bindingSet = result.next();
                BiologicalEntity a = getBiologicalEntityFromBindingSet(beMap, bindingSet);
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
        catch (QueryEvaluationException e) {
            throw new SPARQLQueryException("Failed to retrieve biological entity", e);
        }
    }

    public BiologicalEntity getBiologicalEntityFromBindingSet(Map<URI, BiologicalEntity> biologicalEntityMap,
                                                              BindingSet bindingSet) {
        Value sampleUriValue = bindingSet.getValue(QueryVariables.BIOLOGICAL_ENTITY.toString());
        URI uri = URI.create(sampleUriValue.stringValue());

        Value sampleLabelValue = bindingSet.getValue(QueryVariables.BIOLOGICAL_ENTITY_NAME.toString());
        String label = null;
        if (sampleLabelValue != null) {
            label = sampleLabelValue.stringValue();
        }

        Value studyIdValue = bindingSet.getValue(QueryVariables.STUDY_ID.toString());
        if (studyIdValue != null) {
            URI studyUri = URI.create(studyIdValue.stringValue());

            Value studyLabelValue = bindingSet.getValue(QueryVariables.STUDY_LABEL.toString());
            String studyLabel = studyLabelValue.stringValue();

            Study study = new SimpleStudy(studyUri, studyLabel);
            if (biologicalEntityMap.containsKey(uri)) {
                biologicalEntityMap.get(uri).getStudies().add(study);
            }
            else {
                BiologicalEntity newBe = new SimpleBiologicalEntity(uri, label, study);
                biologicalEntityMap.put(newBe.getURI(), newBe);
            }
        }
        return biologicalEntityMap.get(uri);
    }

    @Override public int count() {
        // TODO - placeholder, more efficient to implement a separate count() SPARQL query
        return read().size();
    }

    @Override public void create(BiologicalEntity biologicalEntity) throws ResourceAlreadyExistsException {
        throw new UnsupportedOperationException(
                "Biological Entity creation is not supported in the current implementation");
    }

    @Override public void update(BiologicalEntity biologicalEntity) throws NoSuchResourceException {
        throw new UnsupportedOperationException(
                "Biological Entity updating is not supported in the current implementation");
    }

    @Override public void delete(BiologicalEntity biologicalEntity) throws NoSuchResourceException {
        throw new UnsupportedOperationException(
                "Biological Entity deletion is not supported in the current implementation");
    }

    @Override
    public Collection<BiologicalEntity> readBySemanticTags(URI... semanticTags) {
        return readBySemanticTags(false, semanticTags);
    }

    @Override
    public Collection<BiologicalEntity> readBySemanticTags(boolean useInference, URI... semanticTags) {
        String query;
        if (useInference) {
            query = getManager().getQueryManager().getSparqlQuery("BiologicalEntity.bySemanticTagInferred");
        }
        else {
            query = getManager().getQueryManager().getSparqlQuery("BiologicalEntity.bySemanticTag");
        }
        return _readBySemanticTags(query, semanticTags);
    }

    @Override public Collection<BiologicalEntity> readByStudyAndName(Study study, String bioentityName) {
        throw new UnsupportedOperationException("Biological Entity accession/name lookup is not yet implemented");
    }

    @Override
    public Collection<BiologicalEntity> readByStudy(Study study) {
        //TODO - Implement the actual sparql query
        throw new UnsupportedOperationException("The method readByStudy needs to be implemented in Sparql");
    }

    private Collection<BiologicalEntity> _readBySemanticTags(String query, URI... semanticTags) {
        Map<String, Value> bindingMap = new HashMap<>();
        ValueFactory factory = getManager().getValueFactory();
        List<BiologicalEntity> bes = new ArrayList<>();
        for (URI uri : semanticTags) {
            bindingMap.put(QueryVariables.SEMANTIC_TAG.toString(), factory.createURI(uri.toString()));
            TupleQueryResult result = manager.evaluateQuery(query, bindingMap);
            bes.addAll(evaluateQueryResults(result));
        }
        HashSet<BiologicalEntity> hs = new HashSet<>();
        hs.addAll(bes);
        bes.clear();
        bes.addAll(hs);
        return bes;
    }
}
