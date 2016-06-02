package uk.ac.ebi.fgpt.zooma.service;

import uk.ac.ebi.fgpt.zooma.model.AnnotationSummary;
import uk.ac.ebi.fgpt.zooma.model.SimpleAnnotationSummary;
import uk.ac.ebi.pride.utilities.ols.web.service.model.Term;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by olgavrou on 19/05/2016.
 */
public class OLSAnnotationSummaryMapper {

    public AnnotationSummary mapOLSTermToAnnotation(Term term){

        Collection<URI> semanticTags;
        Collection<URI> source;
        Collection<URI> annotationUris = null;
        URI propertyUri = null;
        float score;
        AnnotationSummary annotationSummary;

            semanticTags = new ArrayList<URI>();
            semanticTags.add(URI.create(term.getIri().getIdentifier()));
            source = new ArrayList<URI>();
            source.add(URI.create(term.getOntologyIri()));
            score = Float.valueOf(term.getScore());
            annotationSummary = new SimpleAnnotationSummary("OLS", propertyUri, null, term.getLabel(), semanticTags, annotationUris, score, source);

            return (AnnotationSummary) annotationSummary;

    }


}
