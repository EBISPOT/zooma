package uk.ac.ebi.fgpt.zooma.datasource;

import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.zooma.exception.NoSuchResourceException;
import uk.ac.ebi.fgpt.zooma.exception.RDFRepositoryException;
import uk.ac.ebi.fgpt.zooma.exception.ResourceAlreadyExistsException;
import uk.ac.ebi.fgpt.zooma.exception.SPARQLQueryException;
import uk.ac.ebi.fgpt.zooma.exception.TooManyResultsException;
import uk.ac.ebi.fgpt.zooma.model.SimpleStudy;
import uk.ac.ebi.fgpt.zooma.model.Study;
import uk.ac.ebi.fgpt.zooma.service.QueryVariables;
import uk.ac.ebi.fgpt.zooma.service.SesameRepositoryManager;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * A zooma study DAO implementation that uses a SPARQL endpoint to expose studies in response to canned queries.
 *
 * @author Simon Jupp
 * @author Tony Burdett
 * @date 03/04/12
 */
public class SparqlStudyDAO implements StudyDAO {

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
        return "zooma.studies";
    }

    @Override public int count() {
        return read().size();
    }

    @Override public void create(Study study) throws ResourceAlreadyExistsException {
        throw new UnsupportedOperationException("Study creation is not supported in the current implementation");
    }

    @Override public void update(Study study) throws NoSuchResourceException {
        throw new UnsupportedOperationException("Study updating is not supported in the current implementation");
    }

    @Override public void delete(Study study) throws NoSuchResourceException {
        throw new UnsupportedOperationException("Study deletion is not supported in the current implementation");
    }

    @Override public Collection<Study> read() {
        String query = getManager().getQueryManager().getSparqlQuery("Study.read");
        TupleQueryResult result = manager.evaluateQuery(query);
        return evaluateQueryResults(result);
    }

    @Override public List<Study> read(int size, int start) {
        String query = getManager().getQueryManager().getSparqlQuery("Study.read");
        query += "\nLIMIT " + size + " OFFSET " + start;
        TupleQueryResult result = manager.evaluateQuery(query);
        return evaluateQueryResults(result);
    }

    @Override public Study read(URI uri) {
        String query = getManager().getQueryManager().getSparqlQuery("Study.read");

        Map<String, Value> bindingMap = new HashMap<>();
        ValueFactory factory;
        try {
            factory = getManager().getValueFactory();
            bindingMap.put(QueryVariables.STUDY_ID.toString(), factory.createURI(uri.toString()));

            TupleQueryResult result = manager.evaluateQuery(query, bindingMap);
            List<Study> studies = evaluateQueryResults(result);

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
        catch (RDFRepositoryException e) {
            getLog().error(e.getLocalizedMessage());
            return null;
        }


    }

    private List<Study> evaluateQueryResults(TupleQueryResult result) {
        try {
            List<Study> bes = new ArrayList<>();
            while (result.hasNext()) {
                BindingSet bindingSet = result.next();
                Study s = getStudyFromBindingSet(bindingSet);
                bes.add(s);
            }
            return bes;
        }
        catch (QueryEvaluationException e) {
            throw new SPARQLQueryException("Failed to retrieve study", e);
        }
    }

    private Study getStudyFromBindingSet(BindingSet bindingSet) {
        Value studyUriValue = bindingSet.getValue(QueryVariables.STUDY_ID.toString());
        URI studyUri = URI.create(studyUriValue.stringValue());

        Value studyLabelValue = bindingSet.getValue(QueryVariables.STUDY_LABEL.toString());
        String studyLabel = studyLabelValue.stringValue();

        return new SimpleStudy(studyUri, studyLabel);
    }


    @Override
    public Collection<Study> readBySemanticTags(URI... semanticTags) {
        return readBySemanticTags(false, semanticTags);
    }

    @Override
    public Collection<Study> readBySemanticTags(boolean useInference, URI... semanticTags) {
        String query;
        if (useInference) {
            query = getManager().getQueryManager().getSparqlQuery("Study.bySemanticTagInferred");
        }
        else {
            query = getManager().getQueryManager().getSparqlQuery("Study.bySemanticTag");
        }
        return _readBySemanticTags(query, semanticTags);
    }

    @Override public Collection<Study> readByAccession(String accession) {
        throw new UnsupportedOperationException("Study accession lookup is not yet implemented");
    }

    private Collection<Study> _readBySemanticTags(String query, URI... semanticTags) {
        Map<String, Value> bindingMap = new HashMap<>();
        ValueFactory factory = getManager().getValueFactory();
        List<Study> bes = new ArrayList<>();
        for (URI uri : semanticTags) {
            bindingMap.put(QueryVariables.SEMANTIC_TAG.toString(), factory.createURI(uri.toString()));
            TupleQueryResult result = manager.evaluateQuery(query, bindingMap);
            bes.addAll(evaluateQueryResults(result));
        }
        HashSet<Study> hs = new HashSet<>();
        hs.addAll(bes);
        bes.clear();
        bes.addAll(hs);
        return bes;
    }
}
