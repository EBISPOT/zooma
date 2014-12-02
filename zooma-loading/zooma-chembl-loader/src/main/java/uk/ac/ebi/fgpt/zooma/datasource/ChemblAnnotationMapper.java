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
 * Created by dwelter on 28/05/14.
 */
public class ChemblAnnotationMapper extends DefaultJdbcAnnotationMapper {
    private final Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    public ChemblAnnotationMapper(AnnotationFactory chemblAnnotationFactory) {
        this(chemblAnnotationFactory, "http://www.ebi.ac.uk/efo/efo.owl");
    }

    public ChemblAnnotationMapper(AnnotationFactory chemblAnnotationFactory, Resource efoResource) {
        super(chemblAnnotationFactory);
        try {
            OntologyAccessionUtils.loadOntology(efoResource.getURL());
        }
        catch (IOException e) {
            throw new RuntimeException("Unexpected error loading EFO from resource " + efoResource.toString(), e);
        }
    }

    public ChemblAnnotationMapper(AnnotationFactory chemblAnnotationFactory, String efoURL) {
        super(chemblAnnotationFactory);
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
