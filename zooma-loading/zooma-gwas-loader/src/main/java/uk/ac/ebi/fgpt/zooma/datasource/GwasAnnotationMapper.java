package uk.ac.ebi.fgpt.zooma.datasource;

import org.semanticweb.owlapi.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import uk.ac.ebi.fgpt.zooma.util.OntologyAccessionUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URL;

/**
 * Javadocs go here!
 *
 * @author Dani Welter
 * @date 06/11/12
 */
public class GwasAnnotationMapper extends DefaultJdbcAnnotationMapper {
    public GwasAnnotationMapper(AnnotationFactory annotationFactory) {
        this(annotationFactory, "http://www.ebi.ac.uk/efo/efo.owl");
    }

    public GwasAnnotationMapper(AnnotationFactory annotationFactory, Resource efoResource) {
        super(annotationFactory);
        try {
            OntologyAccessionUtils.loadOntology(efoResource.getURL());
        }
        catch (IOException e) {
            throw new RuntimeException("Unexpected error loading EFO from resource " + efoResource.toString(), e);
        }
    }

    public GwasAnnotationMapper(AnnotationFactory annotationFactory, String efoURL) {
        super(annotationFactory);
        try {
            URL efo = URI.create(efoURL).toURL();
            OntologyAccessionUtils.loadOntology(efo);
        }
        catch (IOException e) {
            throw new RuntimeException("Unexpected error forming EFO URL", e);
        }
    }

    @Override protected URI convertSemanticTagToURI(String semanticTag) {
        if (semanticTag == null) {
            getLog().debug("Received null semantic tag result from database query");
            return null;
        }
        else {
            IRI iri = OntologyAccessionUtils.getIRIFromAccession(semanticTag);
            if (iri != null) {
                return iri.toURI();
            }
            else {
                getLog().debug("Unable to lookup semantic tag for '" + semanticTag + "', leaving as null");
                return null;
            }
        }
    }
}
