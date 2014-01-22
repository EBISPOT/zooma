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
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.lode.exception.LodeException;
import uk.ac.ebi.fgpt.lode.service.JenaQueryExecutionService;
import uk.ac.ebi.fgpt.zooma.Namespaces;
import uk.ac.ebi.fgpt.zooma.exception.NoSuchResourceException;
import uk.ac.ebi.fgpt.zooma.exception.ResourceAlreadyExistsException;
import uk.ac.ebi.fgpt.zooma.exception.SPARQLQueryException;
import uk.ac.ebi.fgpt.zooma.exception.TooManyResultsException;
import uk.ac.ebi.fgpt.zooma.exception.ZoomaSerializationException;
import uk.ac.ebi.fgpt.zooma.io.ZoomaSerializer;
import uk.ac.ebi.fgpt.zooma.model.Annotation;
import uk.ac.ebi.fgpt.zooma.model.AnnotationProvenance;
import uk.ac.ebi.fgpt.zooma.model.AnnotationSource;
import uk.ac.ebi.fgpt.zooma.model.BiologicalEntity;
import uk.ac.ebi.fgpt.zooma.model.Property;
import uk.ac.ebi.fgpt.zooma.model.SimpleAnnotation;
import uk.ac.ebi.fgpt.zooma.model.SimpleAnnotationProvenance;
import uk.ac.ebi.fgpt.zooma.model.SimpleDatabaseAnnotationSource;
import uk.ac.ebi.fgpt.zooma.model.SimpleOntologyAnnotationSource;
import uk.ac.ebi.fgpt.zooma.model.SimpleTypedProperty;
import uk.ac.ebi.fgpt.zooma.model.Study;
import uk.ac.ebi.fgpt.zooma.model.TypedProperty;
import uk.ac.ebi.fgpt.zooma.service.QueryManager;
import uk.ac.ebi.fgpt.zooma.service.QueryVariables;
import uk.ac.ebi.fgpt.zooma.util.URIBindingUtils;
import virtuoso.jena.driver.VirtDataSetGraph;

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
import java.util.Set;

/**
 * @author Simon Jupp
 * @date 06/08/2013 Functional Genomics Group EMBL-EBI
 */
public class SparqlAnnotationDAO implements AnnotationDAO {

    private JenaQueryExecutionService queryService;

    private Logger log = LoggerFactory.getLogger(getClass());

    private BiologicalEntityDAO biologicalEntityDAO;

    private ZoomaSerializer<Annotation, OWLOntology, OWLNamedIndividual> annotationZoomaSerializer;

    private int queryCounter = 1;

    private QueryManager queryManager;

    private static DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    protected Logger getLog() {
        return log;
    }

    public ZoomaSerializer<Annotation, OWLOntology, OWLNamedIndividual> getAnnotationZoomaSerializer() {
        return annotationZoomaSerializer;
    }

    public void setAnnotationZoomaSerializer(ZoomaSerializer<Annotation, OWLOntology, OWLNamedIndividual> annotationZoomaSerializer) {
        this.annotationZoomaSerializer = annotationZoomaSerializer;
    }

    public JenaQueryExecutionService getQueryService() {
        return queryService;
    }

    public void setQueryService(JenaQueryExecutionService queryService) {
        this.queryService = queryService;
    }

    public BiologicalEntityDAO getBiologicalEntityDAO() {
        return biologicalEntityDAO;
    }

    public void setBiologicalEntityDAO(BiologicalEntityDAO biologicalEntityDAO) {
        this.biologicalEntityDAO = biologicalEntityDAO;
    }

    public QueryManager getQueryManager() {
        return queryManager;
    }

    public void setQueryManager(QueryManager queryManager) {
        this.queryManager = queryManager;
    }

    @Override public void create(Annotation annotation) throws ResourceAlreadyExistsException {
        getLog().debug("Triggered annotation create request...\n\n" + annotation.toString());

        if (read(annotation.getURI()) != null) {
            throw new ResourceAlreadyExistsException(
                    "Can't create new annotation with " + annotation.getURI() + ", URI already exists");
        }

        try {
            final PipedInputStream pis = new PipedInputStream();
            PipedOutputStream pos = new PipedOutputStream(pis);

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {

                    Graph g = getQueryService().getNamedGraph("http://rdf.ebi.ac.uk/dataset/zooma");
                    Model m = ModelFactory.createModelForGraph(g);
                    m.read(pis, "http://rdf.ebi.ac.uk/dataset/zooma");
                    m.close();
                    g.close();
                }
            });
            thread.start();
            getAnnotationZoomaSerializer().serialize(getDatasourceName(), Collections.singleton(annotation), pos);
        }
        catch (IOException e) {
            log.error("Couldn't create annotation " + annotation.toString(), e);
        }
        catch (ZoomaSerializationException e) {
            log.error("Couldn't create annotation " + annotation.toString(), e);
        }
    }

    @Override public void update(Annotation annotation) throws NoSuchResourceException {
        getLog().debug("Triggered annotation update request...\n\n" + annotation.toString());
        if (read(annotation.getURI()) == null) {
            throw new NoSuchResourceException(
                    "Can't update annotation with URI " + annotation.getURI() + " no such annotation exists");
        }

//        Graph g = getQueryService().getDefaultGraph();
//        Model m = ModelFactory.createModelForGraph(g);
//        m.removeAll(new ResourceImpl(annotation.getURI().toString()), null, null);

        try {
            final PipedInputStream pis = new PipedInputStream();
            PipedOutputStream pos = new PipedOutputStream(pis);

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Graph g = getQueryService().getNamedGraph("http://rdf.ebi.ac.uk/dataset/zooma");
                    Model m = ModelFactory.createModelForGraph(g);
                    m.read(pis, "http://rdf.ebi.ac.uk/dataset/zooma");
                    m.close();
                    g.close();
                }
            });
            thread.start();
            getAnnotationZoomaSerializer().serialize(getDatasourceName(), Collections.singleton(annotation), pos);
        }
        catch (IOException e) {
            log.error("Couldn't create annotation " + annotation.toString(), e);
        }
        catch (ZoomaSerializationException e) {
            log.error("Couldn't create annotation " + annotation.toString(), e);
        }

    }

    @Override public void delete(Annotation annotation) throws NoSuchResourceException {
        if (read(annotation.getURI()) == null) {
            throw new NoSuchResourceException(
                    "Can't delete annotation with URI " + annotation.getURI() + " no such annotation exists");
        }

        getLog().debug("Triggered annotation delete request...\n\n" + annotation.toString());
        Graph g = getQueryService().getNamedGraph("http://rdf.ebi.ac.uk/dataset/zooma");
        Model m = ModelFactory.createModelForGraph(g);
        m.removeAll(new ResourceImpl(annotation.getURI().toString()), null, null);
        m.close();
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

            List<Annotation> annos = new ArrayList<Annotation>();
            for (URI uri : getAllAnnotationURIs(size, start)) {
                 annos.add(read(uri));
            }
            return annos;
//            q1.setOffset(start);
//            q1.addOrderBy(underscore + QueryVariables.ANNOTATION_ID.toString(), Query.ORDER_DEFAULT);
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
        String query = getQueryManager().getSparqlQuery("ANNOTATIONS.read");
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
        String query = getQueryManager().getSparqlQuery("ANNOTATIONS_STUDY.read");
        Graph g = getQueryService().getDefaultGraph();
        Query q1 = QueryFactory.create(query, Syntax.syntaxARQ);

        QuerySolutionMap initialBinding = new QuerySolutionMap();
        initialBinding.add(QueryVariables.STUDY_ID.toString(), new ResourceImpl(study.getURI().toString()));
        ParameterizedSparqlString queryString = new ParameterizedSparqlString(q1.toString(), initialBinding);

        QueryExecution execute = null;
        try {
            execute = getQueryService().getQueryExecution(g, queryString.asQuery(), false);
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


    @Override public Collection<Annotation> readByBiologicalEntity(BiologicalEntity biologicalEntity) {
        String query = getQueryManager().getSparqlQuery("ANNOTATIONS_BIOENTITY.read");

        Graph g = getQueryService().getDefaultGraph();
        Query q1 = QueryFactory.create(query, Syntax.syntaxARQ);

        QuerySolutionMap initialBinding = new QuerySolutionMap();
        initialBinding.add(QueryVariables.BIOLOGICAL_ENTITY.toString(),
                new ResourceImpl(biologicalEntity.getURI().toString()));
//        ParameterizedSparqlString queryString = new ParameterizedSparqlString(q1.toString(), initialBinding);

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
            if (execute != null) {
                execute.close();
                if (g != null) {
                    g.close();
                }
            }
        }
    }

    @Override public Collection<Annotation> readByProperty(Property property) {
        String query = getQueryManager().getSparqlQuery("ANNOTATIONS.read");

        Graph g = getQueryService().getDefaultGraph();
        Query q1 = QueryFactory.create(query, Syntax.syntaxARQ);

        QuerySolutionMap initialBinding = new QuerySolutionMap();
        if (property.getURI() != null) {
            initialBinding.add(QueryVariables.PROPERTY_VALUE_ID.toString(),
                    new ResourceImpl(property.getURI().toString()));
        }
        else {
            Model m = ModelFactory.createDefaultModel();
            if (property instanceof TypedProperty) {
                initialBinding.add(QueryVariables.PROPERTY_NAME.toString(),
                        m.createLiteral(((TypedProperty) property).getPropertyType()));
            }
            initialBinding.add(QueryVariables.PROPERTY_VALUE.toString(),
                    m.createLiteral((property.getPropertyValue())));
        }

        ParameterizedSparqlString queryString = new ParameterizedSparqlString(q1.toString(), initialBinding);

        QueryExecution execute = null;
        try {
            execute = getQueryService().getQueryExecution(g, queryString.asQuery(), false);
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

    @Override public Collection<Annotation> readBySemanticTag(URI semanticTagURI) {
        String query = getQueryManager().getSparqlQuery("ANNOTATIONS.read");
        Graph g = getQueryService().getDefaultGraph();
        Query q1 = QueryFactory.create(query, Syntax.syntaxARQ);

        QuerySolutionMap initialBinding = new QuerySolutionMap();
        initialBinding.add(QueryVariables.SEMANTIC_TAG.toString(), new ResourceImpl(semanticTagURI.toString()));
        ParameterizedSparqlString queryString = new ParameterizedSparqlString(q1.toString(), initialBinding);

        QueryExecution execute = null;
        try {
            execute = getQueryService().getQueryExecution(g, queryString.asQuery(), false);
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

    public List<Annotation> evaluateQueryResults(ResultSet result) {
        Map<URI, Annotation> annotationMap = new HashMap<>();
        while (result.hasNext()) {
            QuerySolution solution = result.nextSolution();
            Annotation a = getAnnotationFromBindingSet(annotationMap, solution);
            if (a != null) {
                annotationMap.put(a.getURI(), a);
            }
        }

        List<Annotation> annotationList = new ArrayList<>();
        annotationList.addAll(annotationMap.values());
        Collections.sort(annotationList, new Comparator<Annotation>() {
            @Override public int compare(Annotation o1, Annotation o2) {
                return o1.getURI().toString().compareTo(o2.getURI().toString());
            }
        });
        return annotationList;
    }

    @Override public String getDatasourceName() {
        return "zooma";
    }


    final static String underscore = "_";

    public Annotation getAnnotationFromBindingSet(Map<URI, Annotation> annotationMap, QuerySolution solution) {

        Resource annotationIdValue = solution.getResource(underscore + QueryVariables.ANNOTATION_ID.toString());
        Resource beIdValue = solution.getResource(underscore + QueryVariables.BIOLOGICAL_ENTITY.toString());
        Resource propertyValueIdValue = solution.getResource(underscore + QueryVariables.PROPERTY_VALUE_ID.toString());
        Literal propertyNameValue = solution.getLiteral(underscore + QueryVariables.PROPERTY_NAME.toString());
        Literal propertyValueValue = solution.getLiteral(underscore + QueryVariables.PROPERTY_VALUE.toString());
        Resource semanticTag = solution.getResource(underscore + QueryVariables.SEMANTIC_TAG.toString());
        Resource database = solution.getResource(QueryVariables.DATABASEID.toString());
        Resource sourceType = solution.getResource(QueryVariables.SOURCETYPE.toString());

        // bit of an optimisation hack to avoid slow SPARQL filters, we never want the source type to be an OWLIndividual, so return null at this point
        if (sourceType.getURI().equals(OWLRDFVocabulary.OWL_NAMED_INDIVIDUAL.getIRI().toString())) { return null; }

        Resource replaces = solution.getResource(QueryVariables.REPLACES.toString());
        Resource replacedBy = solution.getResource(QueryVariables.REPLACEDBY.toString());
        Literal sourceName = solution.getLiteral(QueryVariables.SOURCENAME.toString());
        Resource evidence = solution.getResource(QueryVariables.EVIDENCE.toString());
        Literal generator = solution.getLiteral(QueryVariables.GENERATOR.toString());
        Literal generated = solution.getLiteral(QueryVariables.GENERATED.toString());
        Literal annotator = solution.getLiteral(QueryVariables.ANNOTATOR.toString());
        Literal annotated = solution.getLiteral(QueryVariables.ANNOTATED.toString());

        URI annotationUri = URI.create(annotationIdValue.getURI());
        URI beUri = null;
        if (beIdValue != null) {
            beUri = URI.create(beIdValue.getURI());
        }
        URI pvUri = URI.create(propertyValueIdValue.getURI());
        URI ontoUri = null;
        if (semanticTag != null) {
            ontoUri = URI.create(semanticTag.getURI());
        }

        // get biological entities
        Map<URI, BiologicalEntity> biologicalEntityMap = new HashMap<>();
        Map<URI, Study> studyMap = new HashMap<>();

        if (annotationMap.containsKey(annotationUri)) {
            Annotation anno = annotationMap.get(annotationUri);

            if (beUri != null) {
                // we need to store the current bioentities, get any updates and addthem back to the annotation.
                for (BiologicalEntity biologicalEntity : anno.getAnnotatedBiologicalEntities()) {
                    biologicalEntityMap.put(biologicalEntity.getURI(), biologicalEntity);
                    for (Study study : biologicalEntity.getStudies()) {
                        studyMap.put(study.getURI(), study);
                    }
                }

                // no clear all bioenities from the annotation object
                anno.getAnnotatedBiologicalEntities().clear();

                // get any updates to the bioentities map
                ((SparqlBiologicalEntityDAO)getBiologicalEntityDAO()).getBiologicalEntityFromBindingSet(
                        biologicalEntityMap,
                        studyMap,
                        solution
                );

                // add all the updated values back to the annotation
                anno.getAnnotatedBiologicalEntities().addAll(biologicalEntityMap.values());
            }

            // add any new semantic tags
            if (ontoUri != null) {
                anno.getSemanticTags().add(ontoUri);
            }
        }
        else {


            Set<BiologicalEntity> beSet = new HashSet<>();
            if (beUri != null) {
                // get bio entity form binding map
                BiologicalEntity entity = ((SparqlBiologicalEntityDAO)getBiologicalEntityDAO()).getBiologicalEntityFromBindingSet(
                        new HashMap<URI, BiologicalEntity>(),
                        new HashMap<URI, Study>(),
                        solution
                );
                if (entity != null) {
                    beSet.add(entity);
                }
            }
            Property p =
                    new SimpleTypedProperty(pvUri,
                            propertyNameValue.getLexicalForm(),
                            propertyValueValue.getLexicalForm());

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
                        source = new SimpleOntologyAnnotationSource(URI.create(database.toString()),
                                sourceName.getLexicalForm());
                    }
                    else if (sourceT == AnnotationSource.Type.DATABASE) {
                        source = new SimpleDatabaseAnnotationSource(URI.create(database.toString()),
                                sourceName.getLexicalForm());
                    }

                }

                if (source == null) {
                    throw new RuntimeException(
                            "Data error - attempting to create provenance with unrecognised annotation source type");
                }

                prov = new SimpleAnnotationProvenance(source,
                        ev,
                        AnnotationProvenance.Accuracy.NOT_SPECIFIED,
                        generator.getLexicalForm(),
                        generatedDate != null ? generatedDate.toDate() : null,
                        annotator.getLexicalForm(),
                        annotatedDate != null ? annotatedDate.toDate() : null);

            }
            Annotation newAnno = null;
            if (ontoUri == null) {
              newAnno = new SimpleAnnotation(annotationUri, beSet, p, prov);
            }
            else {
                newAnno = new SimpleAnnotation(annotationUri, beSet, p, prov, ontoUri);
            }
            annotationMap.put(newAnno.getURI(), newAnno);
        }

        // set getReplaces and replaced
        if (replaces != null) {
            URI replacesUri = URI.create(replaces.getURI());
            annotationMap.get(annotationUri).getReplaces().add(replacesUri);
        }
        if (replacedBy != null) {
            URI replacedByUri = URI.create(replacedBy.getURI());
            annotationMap.get(annotationUri).getReplacedBy().add(replacedByUri);
        }

        return annotationMap.get(annotationUri);

    }


}
