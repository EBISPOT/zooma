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
import uk.ac.ebi.fgpt.zooma.service.QueryVariables;
import uk.ac.ebi.fgpt.zooma.service.SesameRepositoryManager;
import uk.ac.ebi.fgpt.zooma.util.URIBindingUtils;
import uk.ac.ebi.fgpt.zooma.util.URIUtils;

import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A zooma annotation DAO implementation that uses a SPARQL endpoint to expose annotations in response to canned
 * queries.
 *
 * @author Simon Jupp
 * @author Tony Burdett
 * @date 03/04/12
 */
public class SparqlAnnotationDAO implements AnnotationDAO {

    private StudyDAO studyDAO;
    private SparqlBiologicalEntityDAO biologicalEntityDAO;

    private SesameRepositoryManager manager;

    private int queryCounter = 1;

    private Logger log = LoggerFactory.getLogger(getClass());

    private static SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    protected Logger getLog() {
        return log;
    }

    public StudyDAO getStudyDAO() {
        return studyDAO;
    }

    public void setStudyDAO(StudyDAO studyDAO) {
        this.studyDAO = studyDAO;
    }

    public SparqlBiologicalEntityDAO getBiologicalEntityDAO() {
        return biologicalEntityDAO;
    }

    public void setBiologicalEntityDAO(SparqlBiologicalEntityDAO biologicalEntityDAO) {
        this.biologicalEntityDAO = biologicalEntityDAO;
    }

    public SesameRepositoryManager getManager() {
        return manager;
    }

    public void setManager(SesameRepositoryManager manager) {
        this.manager = manager;
    }

    @Override public int count() {
        // TODO - placeholder, more efficient to implement a separate count() SPARQL query
        return read().size();
    }

    @Override public void create(Annotation annotation) throws ResourceAlreadyExistsException {
        throw new UnsupportedOperationException("Annotation creation is not supported in the current implementation");
    }

    @Override public void update(Annotation annotation) throws NoSuchResourceException {
        throw new UnsupportedOperationException("Annotation updating is not supported in the current implementation");
    }

    @Override public void delete(Annotation annotation) throws NoSuchResourceException {
        throw new UnsupportedOperationException("Annotation deletion is not supported in the current implementation");
    }

    @Override public Collection<Annotation> read() {
        String query = getManager().getQueryManager().getSparqlQuery("ANNOTATIONS.read");
        TupleQueryResult result = manager.evaluateQuery(query);
        return evaluateQueryResults(result);
    }

    @Override public List<Annotation> read(int size, int start) {
        String query = getManager().getQueryManager().getSparqlQuery("ANNOTATIONS.read");
        query += "\nLIMIT " + size + " OFFSET " + start;
        TupleQueryResult result = manager.evaluateQuery(query);
        return evaluateQueryResults(result);
    }

    @Override public Annotation read(URI uri) {
        String query = getManager().getQueryManager().getSparqlQuery("ANNOTATIONS.read");

        Map<String, Value> bindingMap = new HashMap<>();
        ValueFactory factory = getManager().getValueFactory();
        bindingMap.put(QueryVariables.ANNOTATION_ID.toString(), factory.createURI(uri.toString()));

        TupleQueryResult result = manager.evaluateQuery(query, bindingMap);
        List<Annotation> annos = evaluateQueryResults(result);
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

    @Override public String getDatasourceName() {
        return "zooma";
    }

    @Override public Collection<Annotation> readByStudy(Study study) {
        String query = getManager().getQueryManager().getSparqlQuery("ANNOTATIONS.read");

        Map<String, Value> bindingMap = new HashMap<>();
        ValueFactory factory = getManager().getValueFactory();
        bindingMap.put(QueryVariables.STUDY_ID.toString(), factory.createURI(study.getURI().toString()));
        TupleQueryResult result = manager.evaluateQuery(query, bindingMap);
        return evaluateQueryResults(result);
    }

    @Override public Collection<Annotation> readByBiologicalEntity(BiologicalEntity biologicalEntity) {
        String query = getManager().getQueryManager().getSparqlQuery("ANNOTATIONS_BIOENTITY.read");

        Map<String, Value> bindingMap = new HashMap<>();
        ValueFactory factory = getManager().getValueFactory();
        bindingMap.put(QueryVariables.BIOLOGICAL_ENTITY.toString(),
                       factory.createURI(biologicalEntity.getURI().toString()));
        TupleQueryResult result = manager.evaluateQuery(query, bindingMap);
        return evaluateQueryResults(result);
    }

    @Override public Collection<Annotation> readByProperty(Property property) {
        String query = getManager().getQueryManager().getSparqlQuery("ANNOTATIONS.read");

        Map<String, Value> bindingMap = new HashMap<>();
        ValueFactory factory = getManager().getValueFactory();
        if (property.getURI() != null) {
            bindingMap.put(QueryVariables.PROPERTY_VALUE_ID.toString(),
                           factory.createURI(property.getURI().toString()));
        }
        else {
            if (property instanceof TypedProperty) {
                bindingMap.put(QueryVariables.PROPERTY_NAME.toString(),
                               factory.createLiteral(((TypedProperty) property).getPropertyType()));
            }
            bindingMap.put(QueryVariables.PROPERTY_VALUE.toString(),
                           factory.createLiteral(property.getPropertyValue()));
        }

        TupleQueryResult result = manager.evaluateQuery(query, bindingMap);
        return evaluateQueryResults(result);
    }

    @Override public Collection<Annotation> readBySemanticTag(URI semanticTagURI) {
        String query = getManager().getQueryManager().getSparqlQuery("ANNOTATIONS.read");

        Map<String, Value> bindingMap = new HashMap<>();
        ValueFactory factory = getManager().getValueFactory();
        bindingMap.put(QueryVariables.SEMANTIC_TAG.toString(), factory.createURI(semanticTagURI.toString()));
        TupleQueryResult result = manager.evaluateQuery(query, bindingMap);
        return evaluateQueryResults(result);
    }

    public List<Annotation> evaluateQueryResults(TupleQueryResult result) {
        try {
            Map<URI, Annotation> annotationMap = new HashMap<>();
            while (result.hasNext()) {
                BindingSet bindingSet = result.next();
                Annotation a = getAnnotationFromBindingSet(annotationMap, bindingSet);
                annotationMap.put(a.getURI(), a);
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
        catch (QueryEvaluationException e) {
            throw new SPARQLQueryException("Failed to retrieve annotation", e);
        }
    }

    public Annotation getAnnotationFromBindingSet(Map<URI, Annotation> annotationMap, BindingSet bindingSet) {
        Value annotationIdValue = bindingSet.getValue(QueryVariables.ANNOTATION_ID.toString());
        Value beIdValue = bindingSet.getValue(QueryVariables.BIOLOGICAL_ENTITY.toString());
        Value propertyValueIdValue = bindingSet.getValue(QueryVariables.PROPERTY_VALUE_ID.toString());
        Value propertyNameValue = bindingSet.getValue(QueryVariables.PROPERTY_NAME.toString());
        Value propertyValueValue = bindingSet.getValue(QueryVariables.PROPERTY_VALUE.toString());
        Value semanticTag = bindingSet.getValue(QueryVariables.SEMANTIC_TAG.toString());
        Value database = bindingSet.getValue(QueryVariables.DATABASEID.toString());
        Value sourceType = bindingSet.getValue(QueryVariables.SOURCETYPE.toString());
        Value sourceName = bindingSet.getValue(QueryVariables.SOURCENAME.toString());
        Value evidence = bindingSet.getValue(QueryVariables.EVIDENCE.toString());
        Value generator = bindingSet.getValue(QueryVariables.GENERATOR.toString());
        Value generated = bindingSet.getValue(QueryVariables.GENERATED.toString());
        Value annotator = bindingSet.getValue(QueryVariables.ANNOTATOR.toString());
        Value annotated = bindingSet.getValue(QueryVariables.ANNOTATED.toString());

        URI annotationUri = URI.create(annotationIdValue.stringValue());
        URI beUri = null;
        if (beIdValue != null) {
            beUri = URI.create(beIdValue.stringValue());
        }
        URI pvUri = URI.create(propertyValueIdValue.stringValue());
        URI ontoUri;
        if (semanticTag != null) {
            ontoUri = URI.create(semanticTag.stringValue());
        }
        else {
            ontoUri = null;
            getLog().debug("Missing semantic tag for annotation <" + annotationUri.toString() + ">");
        }


        if (annotationMap.containsKey(annotationUri)) {
            Annotation anno = annotationMap.get(annotationUri);
            if (beUri != null) {
                anno.getAnnotatedBiologicalEntities().add(getBiologicalEntityDAO().read(beUri));
            }
        }
        else {
            Set<BiologicalEntity> beSet = new HashSet<>();
            if (beUri != null) {
                BiologicalEntity be = getBiologicalEntityDAO().read(beUri);
                if (be != null) {
                    beSet.add(be);
                }
                else {
                    getLog().debug("Missing biological entity for annotation <" + annotationUri.toString() + ">");
                }
            }
            else {
                getLog().debug("Missing biological entity for annotation " + annotationUri.toString() + ">");
            }
            Property p =
                    new SimpleTypedProperty(pvUri, propertyNameValue.stringValue(), propertyValueValue.stringValue());

            AnnotationProvenance prov = null;
            if (database != null && evidence != null) {
                URI evidenceUri = URI.create(evidence.toString());

                String shortFrom = URIUtils.extractFragment(evidenceUri);
                AnnotationProvenance.Evidence ev;
                try {
                    ev = AnnotationProvenance.Evidence.lookup(evidenceUri.toString());
                }
                catch (IllegalArgumentException e) {
                    ev = AnnotationProvenance.Evidence.NON_TRACEABLE;
                    getLog().warn("SPARQL query returned evidence '" + shortFrom + "' " +
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
                    generator = manager.getValueFactory().createLiteral("UNKNOWN");
                }

                if (annotator == null) {
                    annotator = manager.getValueFactory().createLiteral("UNKNOWN");
                }


                Date generatedDate = null;
                if (generated == null) {
                    getLog().warn("No generated date for annotation <" + annotationUri.toString() + ">");
                }
                else {
                    String dateStr = generated.stringValue();
                    try {
                        generatedDate = dateformat.parse(dateStr);
                    }
                    catch (ParseException e) {
                        getLog().error("Can't parse generation date '" + dateStr + "' " +
                                               "for annotation <" + annotationUri.toString() + ">", e);
                    }
                    catch (NumberFormatException e) {
                        getLog().error("Can't read generation date '" + dateStr + "' " +
                                               "for annotation <" + annotationUri.toString() + ">", e);
                    }
                }


                Date annotatedDate = null;
                if (annotated != null) {
                    String dateStr = annotated.stringValue();
                    try {
                        annotatedDate = dateformat.parse(dateStr);
                    }
                    catch (ParseException e) {
                        getLog().error("Can't parse annotation date '" + dateStr + "' " +
                                               "for annotation <" + annotationUri.toString() + ">", e);
                    }
                    catch (NumberFormatException e) {
                        getLog().error("Can't read annotation date '" + dateStr + "' " +
                                               "for annotation <" + annotationUri.toString() + ">", e);
                    }
                }

                AnnotationSource source = null;
                if (sourceType != null) {
                    URI sourceAsUri = URI.create(sourceType.stringValue());
                    String name = URIBindingUtils.getName(sourceAsUri);
                    AnnotationSource.Type sourceT = AnnotationSource.Type.lookup(name);

                    if (sourceT == AnnotationSource.Type.ONTOLOGY) {
                        source = new SimpleOntologyAnnotationSource(URI.create(database.toString()), sourceName.stringValue());
                    }
                    else if (sourceT == AnnotationSource.Type.DATABASE) {
                        source = new SimpleDatabaseAnnotationSource(URI.create(database.toString()), sourceName.stringValue());
                    }
                }

                if (source == null) {
                    throw new RuntimeException("Data error - attempting to create provenance with unrecognised annotation source type");
                }


                prov = new SimpleAnnotationProvenance(source,
                                                      ev,
                                                      AnnotationProvenance.Accuracy.NOT_SPECIFIED,
                                                      generator.stringValue(),
                                                      generatedDate,
                                                      annotator.stringValue(),
                                                      annotatedDate);
            }
            Annotation newAnno = new SimpleAnnotation(annotationUri, beSet, p, prov, ontoUri);
            annotationMap.put(newAnno.getURI(), newAnno);
        }
        return annotationMap.get(annotationUri);
    }

    private String getLabelFromBindingSet(BindingSet bindingSet) {
        Value labelValue = bindingSet.getValue(QueryVariables.SEMANTIC_TAG_LABEL.toString());
        if (labelValue != null) {
            return labelValue.stringValue();
        }
        else {
            return "";
        }
    }
}
