package uk.ac.ebi.fgpt.zooma.service;

import javafx.util.Pair;
import uk.ac.ebi.fgpt.zooma.Initializable;
import uk.ac.ebi.fgpt.zooma.model.AnnotationSource;
import uk.ac.ebi.fgpt.zooma.model.SimpleOntologyAnnotationSource;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Implements the {@link AnnotationSourceService} for adding ontology sources
 * from the Ontology Lookup Service. Keeps the {@link AnnotationSource}s in a HashMap
 *
 * Created by olgavrou on 09/06/2016.
 */
public class OLSAnnotationSourceService extends Initializable implements AnnotationSourceService {

    private OLSSearchService olsSearchService;

    //holds all the <ontologyNamespace, createdAnnotationSource> mappings
    private Map<String, AnnotationSource> annotationSourceMap;

    public void setOlsSearchService(OLSSearchService olsSearchService) {
        this.olsSearchService = olsSearchService;
    }

    @Override
    protected void doInitialization() throws Exception {
        annotationSourceMap = new HashMap<>();
    }

    @Override
    protected void doTermination() throws Exception {

    }

    /*
     * Creates new AnnotationSources for all the ontologies found in OLS
     * Stores them into the annotationSourceMap so we can look them up
     * instead of creating new source objects each time
     */
    @Override
    public Collection<AnnotationSource> getAnnotationSources() {
        Map<String, String> ontologies = olsSearchService.getAllOntologies();
        for (String ontology : ontologies.keySet()) {
            annotationSourceMap.put(ontologies.get(ontology), new SimpleOntologyAnnotationSource(URI.create(ontology), ontologies.get(ontology)));
        }
        return annotationSourceMap.values();
    }
    @Override
    public AnnotationSource getAnnotationSource(String sourceName) {

        if (annotationSourceMap.containsKey(sourceName)){
            return annotationSourceMap.get(sourceName);
        }

        Pair<String, String> ontology = olsSearchService.getOntology(sourceName);
        AnnotationSource annotationSource = new SimpleOntologyAnnotationSource(URI.create(ontology.getKey()), ontology.getValue());
        annotationSourceMap.put(sourceName, annotationSource);
        return annotationSource;
    }

    @Override
    public AnnotationSource getAnnotationSource(URI uri) {
        return null;
    }
}
