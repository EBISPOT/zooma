package uk.ac.ebi.fgpt.zooma.service;

import uk.ac.ebi.fgpt.zooma.Initializable;
import uk.ac.ebi.fgpt.zooma.exception.SearchResourcesUnavailableException;
import uk.ac.ebi.fgpt.zooma.model.*;
import uk.ac.ebi.fgpt.zooma.util.SearchStringProcessorProvider;
import uk.ac.ebi.pride.utilities.ols.web.service.model.Term;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by olgavrou on 19/05/2016.
 */
public class OLSAnnotationSummarySearchService extends Initializable implements AnnotationSummarySearchService  {

    private SearchStringProcessorProvider searchStringProcessorProvider;

    private OLSAnnotationSummaryMapper mapper;

    private OLSSearchService olsSearchService;

    public OLSAnnotationSummaryMapper getMapper() {
        return mapper;
    }

    public void setOlsSearchService(OLSSearchService olsSearchService) {
        this.olsSearchService = olsSearchService;
    }


    @Override
    public Collection<AnnotationSummary> search(String propertyValuePattern, URI... sources) {
        try {
            initOrWait();
            return doSearch(getMapper(), propertyValuePattern);
        }
        catch (InterruptedException e) {
            throw new SearchResourcesUnavailableException("Failed to perform query - indexing process was interrupted",
                    e);
        }
    }

    @Override
    public Collection<AnnotationSummary> search(String propertyType, String propertyValuePattern, URI... sources) {
        try {
            initOrWait();
            return doSearch(getMapper(), propertyType, propertyValuePattern);
        }
        catch (InterruptedException e) {
            throw new SearchResourcesUnavailableException("Failed to perform query - indexing process was interrupted",
                    e);
        }
    }


    @Override
    public Collection<AnnotationSummary> searchByPrefix(String propertyValuePrefix, URI... sources) {
        try {
            initOrWait();
            return doSearch(getMapper(), propertyValuePrefix);
        }
        catch (InterruptedException e) {
            throw new SearchResourcesUnavailableException("Failed to perform query - indexing process was interrupted",
                    e);
        }
    }

    @Override
    public Collection<AnnotationSummary> searchByPrefix(String propertyType, String propertyValuePrefix, URI... sources) {
        try {
            initOrWait();
            return doSearch(getMapper(), propertyType, propertyValuePrefix);
        }
        catch (InterruptedException e) {
            throw new SearchResourcesUnavailableException("Failed to perform query - indexing process was interrupted",
                    e);
        }
    }

    @Override
    public Collection<AnnotationSummary> searchBySemanticTags(String... semanticTagShortnames) {
        return new ArrayList<>(); ///TODO: what should be the implementation of this one?
    }

    @Override
    public Collection<AnnotationSummary> searchBySemanticTags(URI... semanticTags) {
        return new ArrayList<>(); ///TODO: what should be the implementation of this one?
    }

    @Override
    public Collection<AnnotationSummary> searchByPreferredSources(String propertyValuePattern, List<URI> preferredSources, URI... requiredSources) {
        try {
            initOrWait();
            return doSearch(getMapper(), propertyValuePattern);
        }
        catch (InterruptedException e) {
            throw new SearchResourcesUnavailableException("Failed to perform query - indexing process was interrupted",
                    e);
        }
    }

    @Override
    public Collection<AnnotationSummary> searchByPreferredSources(String propertyType, String propertyValuePattern, List<URI> preferredSources, URI... requiredSources) {
        try {
            initOrWait();
            return doSearch(getMapper(), propertyType, propertyValuePattern);
        }
        catch (InterruptedException e) {
            throw new SearchResourcesUnavailableException("Failed to perform query - indexing process was interrupted",
                    e);
        }
    }

    @Override
    protected void doInitialization() throws Exception {
        this.mapper = new OLSAnnotationSummaryMapper();

    }

    @Override
    protected void doTermination() throws Exception {
    }

    protected Collection<AnnotationSummary> doSearch(OLSAnnotationSummaryMapper mapper,
                                                     String propertyValuePattern) throws InterruptedException {

        Collection<AnnotationSummary> annotationSummaries = new ArrayList<>();
        List <Term> terms = new ArrayList<>();

        terms = olsSearchService.getTermsByName(propertyValuePattern);

       for (Term term : terms){
           annotationSummaries.add((AnnotationSummary) mapper.mapOLSTermToAnnotation(term));
       }

        return annotationSummaries;
    }

    protected Collection<AnnotationSummary> doSearch(OLSAnnotationSummaryMapper mapper, String propertyType, String propertyValuePattern) {
        Collection<AnnotationSummary> annotationSummaries = new ArrayList<>();
        List <Term> terms = new ArrayList<>();

        //get the parent uris from propertyType
        List<Term> parentTerms = olsSearchService.getTermsByName(propertyType);
        if (parentTerms != null && !parentTerms.isEmpty()) {
            StringBuilder childrenOf = new StringBuilder();
            for (Term parent : parentTerms) {
                if (!childrenOf.toString().contains(parent.getIri().getIdentifier())) {
                    childrenOf.append(parent.getIri().getIdentifier() + ",");
                }
            }
            terms = olsSearchService.getTermsByNameFromParent(propertyValuePattern, childrenOf.toString());

            for (Term term : terms) {
                annotationSummaries.add((AnnotationSummary) mapper.mapOLSTermToAnnotation(term));
            }
        }

        return annotationSummaries;
    }

    /******************* doSearch-es including the sources *****************************/
    /**********************************************************************************/


    protected Collection<AnnotationSummary> doSearch(OLSAnnotationSummaryMapper mapper,
                                                     String propertyValuePattern,
                                                     URI... sources) throws InterruptedException {

        Collection<AnnotationSummary> annotationSummaries = new ArrayList<>();
        List <Term> terms = new ArrayList<>();

        if (sources != null && !(sources.length == 0) ){
            terms = olsSearchService.getTermsByName(propertyValuePattern, cleanSources(sources));
        }

        for (Term term : terms){
            annotationSummaries.add((AnnotationSummary) mapper.mapOLSTermToAnnotation(term));
        }

        return annotationSummaries;
    }

    protected Collection<AnnotationSummary> doSearch(OLSAnnotationSummaryMapper mapper, String propertyType, String propertyValuePattern, URI[] sources) {
        Collection<AnnotationSummary> annotationSummaries = new ArrayList<>();
        List <Term> terms = new ArrayList<>();

        if (sources != null && !(sources.length == 0) ) {
            //clean the sources
            ArrayList<String> cleanSources = cleanSources(sources);
            //get the parent uris from propertyType
            List<Term> parentTerms = olsSearchService.getTermsByName(propertyType, cleanSources);
            if (parentTerms != null && !parentTerms.isEmpty()) {

                StringBuilder childrenOf = new StringBuilder();
                for (Term parent : parentTerms) {
                    if (!childrenOf.toString().contains(parent.getIri().getIdentifier())) {
                        childrenOf.append(parent.getIri().getIdentifier());
                    }
                }

                terms = olsSearchService.getTermsByNameFromParent(propertyValuePattern, cleanSources, childrenOf.toString());
            }

            for (Term term : terms) {
                annotationSummaries.add((AnnotationSummary) mapper.mapOLSTermToAnnotation(term));
            }
        }

        return annotationSummaries;
    }

    private ArrayList<String> cleanSources(URI[] sources){

        ArrayList<String> cleanSources = new ArrayList<>();

        //NOTE: sources for ontologies is going to be the whole uri or just the name?
        //if whole uri then need to strip it
        for (URI source : sources){
            //clean the source
            if (source.toString().contains("/")){
                String [] sourceConc = source.toString().split("/");
                String s = sourceConc[sourceConc.length - 1 ].toLowerCase();
                if (s.contains(".")){
                    s = s.split("\\.")[0];
                }
                cleanSources.add(s);
            } else {
                String s = source.toString();
                if (s.contains(".")){
                    s = source.toString().split("\\.")[0];
                }
                cleanSources.add(s.toLowerCase()); //Need to look into this better maybe
            }

        }

        return cleanSources;
    }

    }
