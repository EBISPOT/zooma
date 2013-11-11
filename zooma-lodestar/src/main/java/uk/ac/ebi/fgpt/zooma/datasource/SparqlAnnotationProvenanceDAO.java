package uk.ac.ebi.fgpt.zooma.datasource;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import uk.ac.ebi.fgpt.lode.exception.LodeException;
import uk.ac.ebi.fgpt.lode.service.JenaQueryExecutionService;
import uk.ac.ebi.fgpt.zooma.exception.NoSuchResourceException;
import uk.ac.ebi.fgpt.zooma.exception.ResourceAlreadyExistsException;
import uk.ac.ebi.fgpt.zooma.exception.SPARQLQueryException;
import uk.ac.ebi.fgpt.zooma.exception.TooManyResultsException;
import uk.ac.ebi.fgpt.zooma.model.*;
import uk.ac.ebi.fgpt.zooma.service.QueryManager;
import uk.ac.ebi.fgpt.zooma.service.QueryVariables;
import uk.ac.ebi.fgpt.zooma.util.URIBindingUtils;

import java.net.URI;
import java.util.*;

/**
 * @author Simon Jupp
 * @date 11/11/2013
 * Functional Genomics Group EMBL-EBI
 */
public class SparqlAnnotationProvenanceDAO implements AnnotationProvenanceDAO {

    private JenaQueryExecutionService queryService;

    private Logger log = LoggerFactory.getLogger(getClass());

    private QueryManager queryManager;

    private static DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    @Override
    public String getDatasourceName() {
        return "zooma";
    }

    @Override
    public int count() {
        return 0;
    }

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


    @Override
    public AnnotationProvenance read(URI uri) {
        String query = getQueryManager().getSparqlQuery("AnnotationProvenance.read");
        Graph g = getQueryService().getDefaultGraph();
        Query q1 = QueryFactory.create(query, Syntax.syntaxARQ);

        QuerySolutionMap initialBinding = new QuerySolutionMap();
        initialBinding.add(QueryVariables.ANNOTATION_ID.toString(), new ResourceImpl(uri.toString()));

        QueryExecution execute = null;
        try {
            execute = getQueryService().getQueryExecution(g, q1.toString(), initialBinding, false);
            ResultSet results = execute.execSelect();
            List<AnnotationProvenance> annos = evaluateQueryResults(results);
            if (annos.size() > 1) {
                getLog().error("Too many results looking for annotation provenance for annotation <" + uri.toString() + ">");
                throw new TooManyResultsException("Expected one result, got " + annos.size() + " for <" + uri + ">");
            }
            else {
                if (annos.size() == 0) {
                    return null;
                }
                else {
                    return annos.get(0);
                }
            }

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

    public List<AnnotationProvenance> evaluateQueryResults(ResultSet result) {
        List<AnnotationProvenance> annotationProvs = new ArrayList<>();
        while (result.hasNext()) {
            QuerySolution solution = (QuerySolution) result.next();
            AnnotationProvenance prov = getAnnotationProvenanceFromBindingSet(solution);
            if (prov != null) {
                annotationProvs.add(prov);
            }
        }
        return annotationProvs;
    }

    final static String underscore = "_";
    public AnnotationProvenance getAnnotationProvenanceFromBindingSet (QuerySolution solution) {

        Resource annotationIdValue = solution.getResource(underscore + QueryVariables.ANNOTATION_ID.toString());
        Resource database = solution.getResource(QueryVariables.DATABASEID.toString());
        Resource sourceType = solution.getResource(QueryVariables.SOURCETYPE.toString());
        Literal sourceName = solution.getLiteral(QueryVariables.SOURCENAME.toString());
        Resource evidence = solution.getResource(QueryVariables.EVIDENCE.toString());
        Literal generator = solution.getLiteral(QueryVariables.GENERATOR.toString());
        Literal generated = solution.getLiteral(QueryVariables.GENERATED.toString());
        Literal annotator = solution.getLiteral(QueryVariables.ANNOTATOR.toString());
        Literal annotated = solution.getLiteral(QueryVariables.ANNOTATED.toString());

        URI annotationUri = URI.create(annotationIdValue.getURI());


        AnnotationProvenance prov = null;
        if (database != null && evidence != null) {
            URI evidenceUri = URI.create(evidence.toString());
            AnnotationProvenance.Evidence ev;
            String name = null;
            try {
                name = URIBindingUtils.getName(evidenceUri);
                ev = AnnotationProvenance.Evidence.lookup(name);
            }
            catch (IllegalArgumentException e) {
                ev = AnnotationProvenance.Evidence.NON_TRACEABLE;
                getLog().warn("SPARQL query returned evidence '" + name + "' " +
                        "(" + evidenceUri + ") but this is not a valid evidence type.  " +
                        "Setting evidence to " + ev);
            }
            catch (NullPointerException e) {
                ev = AnnotationProvenance.Evidence.NON_TRACEABLE;
                getLog().warn("No traceable evidence (" + e.getMessage() + ") for annotation " +
                        "<" + annotationUri.toString() + ">.  Setting evidence to " + ev);
            }

            if (generator == null) {
                getLog().warn("No generator for annotation <" + annotationUri.toString() + ">");
                Model m = ModelFactory.createDefaultModel();
                generator = m.createLiteral("UNKNOWN");
            }

            if (annotator == null) {
                Model m = ModelFactory.createDefaultModel();
                annotator = m.createLiteral("UNKNOWN");
            }


            DateTime generatedDate = null;
            if (generated == null) {
                getLog().warn("No generated date for annotation <" + annotationUri.toString() + ">");
            }
            else {
                // handle cases where virtuoso returns .0 for timezone
                String dateStr = generated.getLexicalForm().replace(".0", "Z");
                try {
                    generatedDate = fmt.parseDateTime(dateStr);
                }
                catch (NumberFormatException e) {
                    getLog().error("Can't read generation date '" + dateStr + "' " +
                            "for annotation <" + annotationUri.toString() + ">", e);

                }
            }


            DateTime annotatedDate = null;
            if (annotated != null) {
                String dateStr = annotated.getLexicalForm().replace(".0", "Z");
                try {
                    // handle cases where virtuoso returns .0 for timezone
                    annotatedDate = fmt.parseDateTime(dateStr);
                }
                catch (NumberFormatException e) {
                    getLog().error("Can't read annotation date '" + dateStr + "' " +
                            "for annotation <" + annotationUri.toString() + ">", e);
                }
            }

            AnnotationSource source = null;
            if (sourceType != null) {
                URI sourceAsUri = URI.create(sourceType.getURI());
                String sourceTypeName = URIBindingUtils.getName(sourceAsUri);
                AnnotationSource.Type sourceT = AnnotationSource.Type.lookup(sourceTypeName);

                if (sourceT == AnnotationSource.Type.ONTOLOGY) {
                    source = new SimpleOntologyAnnotationSource(URI.create(database.toString()), sourceName.getLexicalForm());
                }
                else if (sourceT == AnnotationSource.Type.DATABASE) {
                    source = new SimpleDatabaseAnnotationSource(URI.create(database.toString()), sourceName.getLexicalForm());
                }
            }

            if (source == null) {
                throw new RuntimeException("Data error - attempting to create provenance with unrecognised annotation source type");
            }


            prov = new SimpleAnnotationProvenance(source,
                    ev,
                    AnnotationProvenance.Accuracy.NOT_SPECIFIED,
                    generator.getLexicalForm(),
                    generatedDate != null ? generatedDate.toDate(): new Date(),
                    annotator.getLexicalForm(),
                    annotatedDate !=null ? annotatedDate.toDate() : new Date());

        }
        return prov;

    }

    @Override
    public void create(AnnotationProvenance identifiable) throws ResourceAlreadyExistsException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<AnnotationProvenance> read() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<AnnotationProvenance> read(int size, int start) {
        throw new UnsupportedOperationException();
    }



    @Override
    public void update(AnnotationProvenance object) throws NoSuchResourceException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete(AnnotationProvenance object) throws NoSuchResourceException {
        throw new UnsupportedOperationException();
    }
}
