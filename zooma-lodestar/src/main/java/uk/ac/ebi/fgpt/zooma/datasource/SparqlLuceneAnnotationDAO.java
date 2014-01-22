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
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.lode.exception.LodeException;
import uk.ac.ebi.fgpt.lode.service.JenaQueryExecutionService;
import uk.ac.ebi.fgpt.zooma.Namespaces;
import uk.ac.ebi.fgpt.zooma.exception.NoSuchResourceException;
import uk.ac.ebi.fgpt.zooma.exception.ResourceAlreadyExistsException;
import uk.ac.ebi.fgpt.zooma.exception.SPARQLQueryException;
import uk.ac.ebi.fgpt.zooma.exception.TooManyResultsException;
import uk.ac.ebi.fgpt.zooma.model.AbstractIdentifiable;
import uk.ac.ebi.fgpt.zooma.model.Annotation;
import uk.ac.ebi.fgpt.zooma.model.AnnotationProvenance;
import uk.ac.ebi.fgpt.zooma.model.AnnotationSource;
import uk.ac.ebi.fgpt.zooma.model.BiologicalEntity;
import uk.ac.ebi.fgpt.zooma.model.Property;
import uk.ac.ebi.fgpt.zooma.model.SimpleAnnotationProvenance;
import uk.ac.ebi.fgpt.zooma.model.SimpleDatabaseAnnotationSource;
import uk.ac.ebi.fgpt.zooma.model.SimpleTypedProperty;
import uk.ac.ebi.fgpt.zooma.model.Study;
import uk.ac.ebi.fgpt.zooma.service.QueryManager;
import uk.ac.ebi.fgpt.zooma.service.QueryVariables;
import uk.ac.ebi.fgpt.zooma.util.URIBindingUtils;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * @author Simon Jupp
 * @date 06/08/2013 Functional Genomics Group EMBL-EBI
 * <p/>
 * This partial implementation of the AnnotationDAO is designed specifically to support the efficient creation of the
 * Zooma lucene indexes
 */
public class SparqlLuceneAnnotationDAO implements AnnotationDAO {

    private JenaQueryExecutionService queryService;

    private Logger log = LoggerFactory.getLogger(getClass());

    private static DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    private int queryCounter = 1;

    private QueryManager queryManager;

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

    @Override public void create(Annotation annotation) throws ResourceAlreadyExistsException {
        throw new UnsupportedOperationException("Read only DAO for optimized zooma lucene index building");
    }

    @Override public void update(Annotation annotation) throws NoSuchResourceException {
        getLog().debug("Triggered annotation update request...\n\n" + annotation.toString());
        throw new UnsupportedOperationException("Read only DAO for optimized zooma lucene index building");
    }

    @Override public void delete(Annotation annotation) throws NoSuchResourceException {
        throw new UnsupportedOperationException("Read only DAO for optimized zooma lucene index building");
    }

    @Override public Collection<Annotation> read() {
        return read(-1, -1);
    }

    @Override public int count() {
        return getAllAnnotationURIs(-1, -1).size();
    }

    @Override public List<Annotation> read(int size, int start) {

        String query = getQueryManager().getSparqlQuery("ANNOTATIONS.read");
        Graph g = getQueryService().getDefaultGraph();
        Query q1 = QueryFactory.create(query, Syntax.syntaxARQ);
        if (size > -1) {
            q1.setLimit(size);
        }
        if (start > -1) {
            q1.setOffset(start);
            q1.addOrderBy(underscore + QueryVariables.ANNOTATION_ID.toString(), Query.ORDER_DEFAULT);
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


    public List<URI> getAllAnnotationURIs() {
        return getAllAnnotationURIs(-1, -1);
    }

    private List<URI> getAllAnnotationURIs(int size, int start) {

        String query = getQueryManager().getSparqlQuery("Instance");
        Graph g = getQueryService().getDefaultGraph();
        Query q1 = QueryFactory.create(query, Syntax.syntaxARQ);
        if (size > -1) {
            q1.setLimit(size);
        }
        if (start > -1) {
            q1.setOffset(start);
            q1.addOrderBy(underscore + QueryVariables.RESOURCE.toString(), Query.ORDER_DEFAULT);
        }
        QuerySolutionMap initialBinding = new QuerySolutionMap();
        initialBinding.add(QueryVariables.RESOURCE_TYPE.toString(),
                           new ResourceImpl(Namespaces.OAC.getURI() + "DataAnnotation"));

        QueryExecution execute = null;
        List<URI> uris = new ArrayList<URI>();
        try {
            execute = getQueryService().getQueryExecution(g, q1.toString(), initialBinding, false);
            ResultSet results = execute.execSelect();

            while (results.hasNext()) {
                QuerySolution sol = results.next();
                uris.add(URI.create(sol.getResource(QueryVariables.RESOURCE.toString()).getURI()));
            }

        }
        catch (LodeException e) {
            throw new SPARQLQueryException("Failed to retrieve all annotation URIs", e);
        }
        finally {
            if (execute != null) {
                execute.close();
                if (g != null) {
                    g.close();
                }
            }
        }
        getLog().info("Read " + uris.size() + " annotation URIs");
        return uris;
    }

    @Override public Annotation read(URI uri) {
        String query = getQueryManager().getSparqlQuery("ANNOTATIONS.lucene.read");
        Graph g = getQueryService().getDefaultGraph();
        Query q1 = QueryFactory.create(query, Syntax.syntaxARQ);

        QuerySolutionMap initialBinding = new QuerySolutionMap();
        initialBinding.add(QueryVariables.ANNOTATION_ID.toString(), new ResourceImpl(uri.toString()));

        QueryExecution execute = null;
        try {
            execute = getQueryService().getQueryExecution(g, q1.toString(), initialBinding, false);
            ResultSet results = execute.execSelect();
            List<Annotation> annos = evaluateQueryResults(results);
            getLog().trace("SPARQL query " + queryCounter++ + " complete");
            if (annos.size() > 1) {
                getLog().error("Too many results looking for annotation <" + uri.toString() + ">");
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


    @Override public Collection<Annotation> readByStudy(Study study) {
        throw new UnsupportedOperationException("Read only DAO for optimized zooma lucene index building");

    }


    @Override public Collection<Annotation> readByBiologicalEntity(BiologicalEntity biologicalEntity) {
        throw new UnsupportedOperationException("Read only DAO for optimized zooma lucene index building");

    }

    @Override public Collection<Annotation> readByProperty(Property property) {
        throw new UnsupportedOperationException("Read only DAO for optimized zooma lucene index building");

    }

    @Override public Collection<Annotation> readBySemanticTag(URI semanticTagURI) {
        throw new UnsupportedOperationException("Read only DAO for optimized zooma lucene index building");

    }

    public List<Annotation> evaluateQueryResults(ResultSet result) {
        Map<URI, Annotation> annotationMap = new HashMap<>();
        while (result.hasNext()) {
            QuerySolution solution = (QuerySolution) result.next();
            Annotation a = getAnnotationFromBindingSet(annotationMap, solution);
            annotationMap.put(a.getURI(), a);
        }
        List<Annotation> annotationList = new ArrayList<>();
        annotationList.addAll(annotationMap.values());

        return annotationList;
    }

    @Override public String getDatasourceName() {
        return "zooma";
    }


    final static String underscore = "_";

    public Annotation getAnnotationFromBindingSet(Map<URI, Annotation> annotationMap, QuerySolution solution) {

        Resource annotationIdValue = solution.getResource(underscore + QueryVariables.ANNOTATION_ID.toString());
        Resource propertyValueIdValue = solution.getResource(underscore + QueryVariables.PROPERTY_VALUE_ID.toString());
        Literal propertyNameValue = solution.getLiteral(underscore + QueryVariables.PROPERTY_NAME.toString());
        Literal propertyValueValue = solution.getLiteral(underscore + QueryVariables.PROPERTY_VALUE.toString());
        Resource semanticTag = solution.getResource(underscore + QueryVariables.SEMANTIC_TAG.toString());

        Literal generated = solution.getLiteral(QueryVariables.GENERATED.toString());
        Resource evidence = solution.getResource(QueryVariables.EVIDENCE.toString());
        Resource database = solution.getResource(QueryVariables.DATABASEID.toString());


        URI annotationUri = URI.create(annotationIdValue.getURI());
        URI pvUri = URI.create(propertyValueIdValue.getURI());
        URI ontoUri;
        if (semanticTag != null) {
            ontoUri = URI.create(semanticTag.getURI());
        }
        else {
            ontoUri = null;
            getLog().debug("Missing semantic tag for annotation <" + annotationUri.toString() + ">");
        }

        Property p =
                new SimpleTypedProperty(pvUri, propertyNameValue.getLexicalForm(), propertyValueValue.getLexicalForm());

        if (!annotationMap.containsKey(annotationUri)) {
            Annotation newAnno = new SimpleLuceneAnnotation(annotationUri, p);
            annotationMap.put(newAnno.getURI(), newAnno);
            if (ontoUri != null) {
                annotationMap.get(annotationUri).getSemanticTags().add(ontoUri);
            }
        }

        if (ontoUri != null) {
            annotationMap.get(annotationUri).getSemanticTags().add(ontoUri);
        }

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

            AnnotationSource source = new SimpleDatabaseAnnotationSource(URI.create(database.getURI()), null);
            prov = new SimpleAnnotationProvenance(source,
                                                  ev,
                                                  "zooma",
                                                  generatedDate != null ? generatedDate.toDate() : new Date());
        }

        ((SimpleLuceneAnnotation) annotationMap.get(annotationUri)).setProvenance(prov);

        return annotationMap.get(annotationUri);

    }

    private class SimpleLuceneAnnotation extends AbstractIdentifiable implements Annotation {

        private Collection<URI> semanticTags;
        private Property property;
        private AnnotationProvenance provenance;

        private SimpleLuceneAnnotation(URI annotationUri, Property property) {
            super(annotationUri);
            this.property = property;
            this.semanticTags = new HashSet<>();
        }

        public void setProvenance(AnnotationProvenance prov) {
            this.provenance = prov;
        }

        @Override
        public Collection<BiologicalEntity> getAnnotatedBiologicalEntities() {
            throw new UnsupportedOperationException("Read only DAO for optimized zooma lucene index building");
        }

        @Override
        public Property getAnnotatedProperty() {
            return property;
        }

        @Override
        public Collection<URI> getSemanticTags() {
            return semanticTags;
        }

        @Override
        public AnnotationProvenance getProvenance() {
            return provenance;
        }

        @Override
        public Collection<URI> getReplacedBy() {
            throw new UnsupportedOperationException("Read only DAO for optimized zooma lucene index building");
        }

        @Override
        public void setReplacedBy(URI... replacedBy) {
            throw new UnsupportedOperationException("Read only DAO for optimized zooma lucene index building");

        }

        @Override
        public Collection<URI> getReplaces() {
            throw new UnsupportedOperationException("Read only DAO for optimized zooma lucene index building");
        }

        @Override
        public void setReplaces(URI... replaces) {
            throw new UnsupportedOperationException("Read only DAO for optimized zooma lucene index building");

        }

        @Override
        public String toString() {
            return "SimpleLuceneAnnotation{" +
                    ", uri=" + getURI().toString() +
                    "semanticTags=" + semanticTags +
                    ", property=" + property +
                    ", prov=" + provenance.toString() +
                    '}';
        }
    }
}
