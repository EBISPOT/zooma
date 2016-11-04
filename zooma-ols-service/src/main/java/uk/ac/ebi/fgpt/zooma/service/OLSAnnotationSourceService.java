package uk.ac.ebi.fgpt.zooma.service;

import uk.ac.ebi.fgpt.zooma.Initializable;
import uk.ac.ebi.fgpt.zooma.model.AnnotationSource;
import uk.ac.ebi.fgpt.zooma.model.SimpleOntologyAnnotationSource;
import uk.ac.ebi.pride.utilities.ols.web.service.model.Ontology;

import java.net.URI;
import java.util.*;

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
        List<Ontology> ontologies = olsSearchService.getAllOntologies();

        if (ontologies!= null && !ontologies.isEmpty()) {
            for (Ontology ontology : ontologies) {
                annotationSourceMap.put(ontology.getConfig().getPreferredPrefix(), new SimpleOntologyAnnotationSource(URI.create(ontology.getId()), ontology.getConfig().getPreferredPrefix(), ontology.getName(), ontology.getDescription()));
            }
        }

        if (!annotationSourceMap.isEmpty()){
            return annotationSourceMap.values();
        } else {
            return new ArrayList<>();
        }
    }
    @Override
    public AnnotationSource getAnnotationSource(String sourceName) {

        //saving preferred prefix, which is upper case
        sourceName = sourceName.toUpperCase();

        if (annotationSourceMap.containsKey(sourceName)){
            return annotationSourceMap.get(sourceName);
        }

        Ontology ontology = olsSearchService.getOntology(sourceName);
        if (ontology != null) {
            AnnotationSource annotationSource = new SimpleOntologyAnnotationSource(URI.create(ontology.getId()), ontology.getConfig().getPreferredPrefix(), ontology.getName(), ontology.getDescription());
            annotationSourceMap.put(sourceName, annotationSource);
            return annotationSource;
        }

        return null;

    }

    @Override
    public AnnotationSource getAnnotationSource(URI uri) {
        return null;
    }
}
